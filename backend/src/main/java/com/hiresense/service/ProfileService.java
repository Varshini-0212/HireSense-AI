package com.hiresense.service;

import com.hiresense.domain.AppUser;
import com.hiresense.domain.CandidateProfile;
import com.hiresense.dto.ProfileDtos.ProfileRequest;
import com.hiresense.dto.ProfileDtos.ProfileResponse;
import com.hiresense.exception.ApiException;
import com.hiresense.repository.AppUserRepository;
import com.hiresense.repository.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {
    private final AppUserRepository users;
    private final CandidateProfileRepository profiles;
    private final ResumeService resumes;

    public ProfileService(AppUserRepository users, CandidateProfileRepository profiles, ResumeService resumes) {
        this.users = users;
        this.profiles = profiles;
        this.resumes = resumes;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(String email) {
        AppUser user = user(email);
        return map(user, profiles.findByUserId(user.getId()));
    }

    @Transactional
    public ProfileResponse update(String email, ProfileRequest request) {
        AppUser user = user(email);
        if (request.fullName() != null && !request.fullName().isBlank()) user.setFullName(request.fullName().trim());
        Optional<CandidateProfile> optional = profiles.findByUserId(user.getId());
        if (optional.isPresent()) {
            CandidateProfile profile = optional.get();
            profile.setPhone(request.phone());
            profile.setLocation(request.location());
            profile.setHeadline(request.headline());
            profile.setSummary(request.summary());
            profile.setYearsExperience(request.yearsExperience());
            profile.setHighestEducation(request.highestEducation());
            profile.setPortfolioUrl(request.portfolioUrl());
            profile.setLinkedinUrl(request.linkedinUrl());
            profiles.save(profile);
        }
        return map(user, optional);
    }

    @Transactional(readOnly = true)
    public Object resumes(String email) {
        AppUser user = user(email);
        return profiles.findByUserId(user.getId()).map(profile -> resumes.list(profile.getId())).orElseGet(java.util.List::of);
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    private ProfileResponse map(AppUser user, Optional<CandidateProfile> optional) {
        if (optional.isEmpty()) return new ProfileResponse(user.getId(), user.getFullName(), user.getEmail(), null, null, null, null, null, null, null, null);
        CandidateProfile profile = optional.get();
        return new ProfileResponse(profile.getId(), user.getFullName(), user.getEmail(), profile.getPhone(), profile.getLocation(),
                profile.getHeadline(), profile.getSummary(), profile.getYearsExperience(), profile.getHighestEducation(),
                profile.getPortfolioUrl(), profile.getLinkedinUrl());
    }
}
