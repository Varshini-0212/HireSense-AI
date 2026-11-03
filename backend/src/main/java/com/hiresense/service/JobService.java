package com.hiresense.service;

import com.hiresense.domain.AppUser;
import com.hiresense.domain.CandidateApplication;
import com.hiresense.domain.CandidateProfile;
import com.hiresense.domain.Job;
import com.hiresense.domain.JobSkill;
import com.hiresense.domain.Resume;
import com.hiresense.domain.Skill;
import com.hiresense.domain.enums.ApplicationStatus;
import com.hiresense.domain.enums.JobSkillType;
import com.hiresense.dto.ApplicationDtos.ApplicationResponse;
import com.hiresense.dto.JobDtos.JobRequest;
import com.hiresense.dto.JobDtos.JobResponse;
import com.hiresense.dto.JobDtos.JobSkillTypeValue;
import com.hiresense.dto.JobDtos.SkillRequest;
import com.hiresense.dto.JobDtos.SkillResponse;
import com.hiresense.exception.ApiException;
import com.hiresense.repository.AppUserRepository;
import com.hiresense.repository.CandidateApplicationRepository;
import com.hiresense.repository.CandidateProfileRepository;
import com.hiresense.repository.JobRepository;
import com.hiresense.repository.JobSkillRepository;
import com.hiresense.repository.ResumeRepository;
import com.hiresense.repository.SkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class JobService {
    private final JobRepository jobs;
    private final JobSkillRepository jobSkills;
    private final SkillRepository skills;
    private final AppUserRepository users;
    private final CandidateProfileRepository profiles;
    private final CandidateApplicationRepository applications;
    private final ResumeRepository resumes;

    public JobService(JobRepository jobs, JobSkillRepository jobSkills, SkillRepository skills,
                      AppUserRepository users, CandidateProfileRepository profiles,
                      CandidateApplicationRepository applications, ResumeRepository resumes) {
        this.jobs = jobs;
        this.jobSkills = jobSkills;
        this.skills = skills;
        this.users = users;
        this.profiles = profiles;
        this.applications = applications;
        this.resumes = resumes;
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> list(int page, int size, boolean active) {
        Pageable request = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        return (active ? jobs.findAllByActiveTrue(request) : jobs.findAll(request)).map(this::map);
    }

    @Transactional
    public JobResponse create(String email, JobRequest request) {
        Job job = new Job();
        job.setCreatedBy(user(email));
        copy(job, request);
        job = jobs.save(job);
        replaceSkills(job, request.requiredSkills(), request.preferredSkills());
        return map(job);
    }

    @Transactional
    public JobResponse update(Long id, JobRequest request) {
        Job job = get(id);
        copy(job, request);
        job = jobs.save(job);
        replaceSkills(job, request.requiredSkills(), request.preferredSkills());
        return map(job);
    }

    @Transactional
    public void delete(Long id) {
        Job job = get(id);
        job.setActive(false);
        jobs.save(job);
    }

    @Transactional
    public List<SkillResponse> setSkills(Long id, List<SkillRequest> requested) {
        Job job = get(id);
        jobSkills.deleteAll(jobSkills.findAllByJobId(id));
        List<JobSkill> created = new ArrayList<>();
        for (SkillRequest request : requested) {
            Skill skill = skills.findById(request.skillId()).orElseThrow(() -> ApiException.notFound("Skill not found: " + request.skillId()));
            JobSkill association = new JobSkill();
            association.setJob(job);
            association.setSkill(skill);
            association.setSkillType(request.skillType() == JobSkillTypeValue.REQUIRED ? JobSkillType.REQUIRED : JobSkillType.PREFERRED);
            association.setImportance(request.importance());
            created.add(association);
        }
        return jobSkills.saveAll(created).stream()
                .map(item -> new SkillResponse(item.getSkill().getId(), item.getSkill().getName(), item.getSkill().getCategory(), item.getSkillType().name(), item.getImportance()))
                .toList();
    }

    @Transactional
    public ApplicationResponse apply(String email, Long jobId, Long resumeId) {
        CandidateProfile profile = profiles.findByUserId(user(email).getId()).orElseThrow(() -> ApiException.notFound("Candidate profile not found"));
        Job job = get(jobId);
        if (!job.isActive()) throw ApiException.bad("Job is inactive");
        if (applications.existsByCandidateProfileIdAndJobId(profile.getId(), jobId)) throw new ApiException(HttpStatus.CONFLICT, "Already applied");
        Resume resume = null;
        if (resumeId != null) {
            resume = resumes.findById(resumeId).orElseThrow(() -> ApiException.notFound("Resume not found"));
            if (!resume.getCandidateProfile().getId().equals(profile.getId())) throw ApiException.forbidden("You can only apply with your own resume");
        }
        CandidateApplication application = new CandidateApplication();
        application.setCandidateProfile(profile);
        application.setJob(job);
        application.setResume(resume);
        application.setAppliedAt(LocalDateTime.now());
        return mapApplication(applications.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> applicationsFor(String email) {
        CandidateProfile profile = profiles.findByUserId(user(email).getId()).orElseThrow(() -> ApiException.notFound("Candidate profile not found"));
        return applications.findAllByCandidateProfileIdOrderByAppliedAtDesc(profile.getId()).stream().map(this::mapApplication).toList();
    }

    @Transactional
    public ApplicationResponse status(Long id, ApplicationStatus status) {
        CandidateApplication application = applications.findById(id).orElseThrow(() -> ApiException.notFound("Application not found"));
        application.setStatus(status);
        return mapApplication(applications.save(application));
    }

    public Job get(Long id) { return jobs.findById(id).orElseThrow(() -> ApiException.notFound("Job not found")); }
    @Transactional(readOnly = true)
    public JobResponse getActiveResponse(Long id) {
        Job job = get(id);
        if (!job.isActive()) throw ApiException.notFound("Job not found");
        return map(job);
    }
    public List<JobSkill> skillEntities(Long id) { return jobSkills.findAllByJobId(id); }

    private AppUser user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> ApiException.notFound("User not found")); }

    private void copy(Job job, JobRequest request) {
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setMinExperienceYears(request.minExperienceYears());
        job.setEducationLevel(request.educationLevel());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setLocation(request.location());
        if (request.active() != null) job.setActive(request.active());
    }

    private void replaceSkills(Job job, List<String> required, List<String> preferred) {
        if (required == null && preferred == null) return;
        jobSkills.deleteAll(jobSkills.findAllByJobId(job.getId()));
        List<JobSkill> associations = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        addSkills(job, required, JobSkillType.REQUIRED, seen, associations);
        addSkills(job, preferred, JobSkillType.PREFERRED, seen, associations);
        jobSkills.saveAll(associations);
    }

    private void addSkills(Job job, List<String> names, JobSkillType type, Set<String> seen, List<JobSkill> target) {
        if (names == null) return;
        for (String value : names) {
            String name = value.trim();
            String key = name.toLowerCase(Locale.ROOT);
            if (name.isBlank() || !seen.add(key)) continue;
            Skill skill = skills.findByNameIgnoreCase(name).orElseGet(() -> {
                Skill created = new Skill();
                created.setName(name);
                created.setCategory("Custom");
                created.setDescription("Recruiter-defined job skill");
                return skills.save(created);
            });
            JobSkill association = new JobSkill();
            association.setJob(job);
            association.setSkill(skill);
            association.setSkillType(type);
            association.setImportance(1);
            target.add(association);
        }
    }

    public JobResponse map(Job job) {
        List<SkillResponse> jobSkills = this.jobSkills.findAllByJobId(job.getId()).stream()
                .map(item -> new SkillResponse(item.getSkill().getId(), item.getSkill().getName(), item.getSkill().getCategory(), item.getSkillType().name(), item.getImportance()))
                .toList();
        return new JobResponse(job.getId(), job.getTitle(), job.getDescription(), job.getMinExperienceYears(), job.getEducationLevel(),
                job.getSalaryMin(), job.getSalaryMax(), job.getLocation(), job.isActive(), jobSkills);
    }

    private ApplicationResponse mapApplication(CandidateApplication application) {
        CandidateProfile candidate = application.getCandidateProfile();
        AppUser applicant = candidate.getUser();
        return new ApplicationResponse(application.getId(), application.getJob().getId(), application.getJob().getTitle(),
                candidate.getId(), applicant.getFullName(), applicant.getEmail(),
                application.getResume() == null ? null : application.getResume().getId(),
                application.getStatus().name(), application.getAppliedAt());
    }
}
