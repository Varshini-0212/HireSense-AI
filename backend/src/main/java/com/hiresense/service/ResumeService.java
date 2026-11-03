package com.hiresense.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresense.domain.AppUser;
import com.hiresense.domain.CandidateProfile;
import com.hiresense.domain.ParsedResumeData;
import com.hiresense.domain.Resume;
import com.hiresense.domain.Role;
import com.hiresense.domain.Skill;
import com.hiresense.domain.enums.ResumeStatus;
import com.hiresense.domain.enums.RoleName;
import com.hiresense.dto.ResumeDtos.ResumeResponse;
import com.hiresense.exception.ApiException;
import com.hiresense.repository.AppUserRepository;
import com.hiresense.repository.CandidateProfileRepository;
import com.hiresense.repository.ParsedResumeDataRepository;
import com.hiresense.repository.ResumeRepository;
import com.hiresense.repository.SkillRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ResumeService {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final List<String> COMMON_SKILLS = List.of("Java", "Spring Boot", "SQL", "Python", "FastAPI", "React", "Docker", "Machine Learning", "Analytics", "Communication", "Leadership");

    private final ResumeRepository resumes;
    private final CandidateProfileRepository profiles;
    private final AppUserRepository users;
    private final ParsedResumeDataRepository parsed;
    private final SkillRepository skillRepository;
    private final ObjectMapper json;
    private final MlClient ml;
    private final Path storage;

    public ResumeService(ResumeRepository resumes, CandidateProfileRepository profiles, AppUserRepository users,
                         ParsedResumeDataRepository parsed, SkillRepository skillRepository, ObjectMapper json,
                         MlClient ml, @Value("${resume.storage-path:./data/resumes}") String storagePath) {
        this.resumes = resumes;
        this.profiles = profiles;
        this.users = users;
        this.parsed = parsed;
        this.skillRepository = skillRepository;
        this.json = json;
        this.ml = ml;
        this.storage = Paths.get(storagePath);
    }

    @Transactional
    public ResumeResponse upload(String email, MultipartFile file) {
        return upload(email, null, file);
    }

    @Transactional
    public ResumeResponse upload(String email, Long targetProfileId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw ApiException.bad("Resume file is required");
        if (file.getSize() > MAX_BYTES) throw ApiException.bad("Resume exceeds 5MB");
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("resume");
        String normalizedName = originalName.toLowerCase(Locale.ROOT);
        boolean pdf = normalizedName.endsWith(".pdf");
        boolean docx = normalizedName.endsWith(".docx");
        if (!pdf && !docx) throw ApiException.bad("Only PDF and DOCX resumes are supported");

        AppUser actor = users.findByEmailIgnoreCase(email).orElseThrow(() -> ApiException.notFound("User not found"));
        CandidateProfile profile;
        if (targetProfileId == null) {
            profile = profiles.findByUserId(actor.getId()).orElseThrow(() -> ApiException.notFound("Candidate profile not found"));
        } else {
            boolean isCandidate = hasRole(actor, RoleName.CANDIDATE);
            if (isCandidate && !profiles.findByUserId(actor.getId()).map(CandidateProfile::getId).filter(targetProfileId::equals).isPresent()) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Candidates may only upload resumes to their own profile");
            }
            if (!isCandidate && !hasRole(actor, RoleName.HR) && !hasRole(actor, RoleName.ADMIN)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized to upload for another candidate");
            }
            profile = profiles.findById(targetProfileId).orElseThrow(() -> ApiException.notFound("Candidate profile not found"));
        }

        byte[] bytes;
        try { bytes = file.getBytes(); }
        catch (IOException e) { throw ApiException.bad("Could not read upload"); }
        String text = extract(bytes, pdf);
        Resume resume = new Resume();
        resume.setCandidateProfile(profile);
        resume.setOriginalFileName(originalName);
        resume.setContentType(Optional.ofNullable(file.getContentType()).orElse(pdf ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        resume.setFileSizeBytes(bytes.length);
        resume.setSha256(sha256(bytes));
        resume.setUploadedAt(LocalDateTime.now());
        resume.setStatus(ResumeStatus.PROCESSING);
        String safeName = originalName.replaceAll("[^A-Za-z0-9._-]", "_");
        Path output = storage.resolve(resume.getSha256() + "-" + safeName);
        try {
            Files.createDirectories(storage);
            Files.write(output, bytes, StandardOpenOption.CREATE_NEW);
            resume.setStoragePath(output.toAbsolutePath().toString());
        } catch (FileAlreadyExistsException e) {
            resume.setStoragePath(output.toAbsolutePath().toString());
        } catch (IOException e) {
            throw ApiException.bad("Could not persist resume");
        }

        resume = resumes.save(resume);
        Map<String, Object> extracted = ml.parse(text);
        List<String> skillNames = stringList(extracted.get("skills"));
        if (skillNames.isEmpty()) skillNames = detectSkills(text);
        ParsedResumeData data = new ParsedResumeData();
        data.setResume(resume);
        data.setFullText(text);
        data.setExtractedName(asString(extracted.get("extracted_name")));
        data.setExtractedEmail(firstString(extracted.get("email"), extracted.get("extracted_email"), profile.getUser().getEmail()));
        data.setExtractedPhone(firstString(extracted.get("phone"), extracted.get("extracted_phone"), null));
        data.setYearsExperience(decimal(extracted.get("years_experience")));
        data.setSkillsJson(toJson(skillNames));
        data.setEducationJson(toJson(stringList(extracted.get("education"))));
        data.setCertificationsJson(toJson(stringList(extracted.get("certifications"))));
        data.setProjectsJson(toJson(stringList(extracted.get("projects"))));

        Set<Skill> entities = new HashSet<>();
        for (String skillName : skillNames) {
            if (skillName.isBlank()) continue;
            Skill skill = skillRepository.findByNameIgnoreCase(skillName).orElseGet(() -> {
                Skill created = new Skill();
                created.setName(skillName);
                created.setCategory("TECHNICAL");
                return skillRepository.save(created);
            });
            entities.add(skill);
        }
        data.setSkills(entities);
        parsed.save(data);
        resume.setStatus(ResumeStatus.PROCESSED);
        return toResponse(resumes.save(resume));
    }

    public Long profileId(String email) {
        AppUser user = users.findByEmailIgnoreCase(email).orElseThrow(() -> ApiException.notFound("User not found"));
        return profiles.findByUserId(user.getId()).orElseThrow(() -> ApiException.notFound("Candidate profile not found")).getId();
    }

    public List<ResumeResponse> list(Long profileId) {
        return resumes.findByCandidateProfileIdOrderByUploadedAtDesc(profileId).stream().map(this::toResponse).toList();
    }

    public List<Resume> listEntities(Long profileId) {
        return resumes.findByCandidateProfileIdOrderByUploadedAtDesc(profileId);
    }

    public List<ResumeResponse> listFor(String email) {
        return list(profileId(email));
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResponse(Long id, String email) {
        Resume resume = get(id);
        AppUser actor = users.findByEmailIgnoreCase(email).orElseThrow(() -> ApiException.notFound("User not found"));
        if (hasRole(actor, RoleName.CANDIDATE)) {
            Long ownProfileId = profiles.findByUserId(actor.getId()).map(CandidateProfile::getId).orElse(null);
            if (!resume.getCandidateProfile().getId().equals(ownProfileId)) throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized to access this resume");
        } else if (!hasRole(actor, RoleName.HR) && !hasRole(actor, RoleName.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized to access resumes");
        }
        return toResponse(resume);
    }

    public Resume get(Long id) { return resumes.findById(id).orElseThrow(() -> ApiException.notFound("Resume not found")); }
    @Transactional(readOnly = true)
    public Map<String, String> candidateIdentity(Long resumeId) {
        Resume resume = get(resumeId);
        return Map.of("candidateName", resume.getCandidateProfile().getUser().getFullName(),
                "candidateEmail", resume.getCandidateProfile().getUser().getEmail());
    }
    public ParsedResumeData data(Long id) { return parsed.findByResumeId(id).orElseThrow(() -> ApiException.notFound("Parsed resume not found")); }

    private String extract(byte[] bytes, boolean pdf) {
        try {
            if (pdf) {
                try (PDDocument document = Loader.loadPDF(bytes)) { return new PDFTextStripper().getText(document); }
            }
            try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes)); XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        } catch (Exception e) { throw ApiException.bad("Could not extract resume text: " + e.getMessage()); }
    }

    private boolean hasRole(AppUser user, RoleName name) { return user.getRoles().stream().map(Role::getName).anyMatch(name::equals); }

    private List<String> detectSkills(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        return COMMON_SKILLS.stream().filter(skill -> normalized.contains(skill.toLowerCase(Locale.ROOT))).toList();
    }

    private String sha256(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder output = new StringBuilder();
            for (byte value : hash) output.append(String.format("%02x", value));
            return output.toString();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    private String asString(Object value) { return value == null ? null : String.valueOf(value); }
    private String firstString(Object first, Object second, String fallback) { return first != null ? asString(first) : second != null ? asString(second) : fallback; }
    private BigDecimal decimal(Object value) { try { return value == null ? null : new BigDecimal(String.valueOf(value)); } catch (Exception e) { return null; } }
    private List<String> stringList(Object value) { if (value instanceof List<?> list) return list.stream().map(String::valueOf).toList(); if (value instanceof String string) return List.of(string); return List.of(); }
    private String toJson(Object value) { try { return json.writeValueAsString(value); } catch (Exception e) { return "[]"; } }
    private List<String> fromJson(String value) { try { return json.readValue(value == null ? "[]" : value, new TypeReference<>() {}); } catch (Exception e) { return List.of(); } }

    private ResumeResponse toResponse(Resume resume) {
        ParsedResumeData data = parsed.findByResumeId(resume.getId()).orElse(null);
        return new ResumeResponse(resume.getId(), resume.getOriginalFileName(), resume.getContentType(), resume.getFileSizeBytes(), resume.getStatus().name(), data == null ? null : data.getId(), data == null ? null : data.getExtractedEmail(), data == null ? null : data.getYearsExperience(), data == null ? List.of() : fromJson(data.getSkillsJson()));
    }
}
