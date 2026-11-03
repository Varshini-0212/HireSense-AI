package com.hiresense.controller;

import com.hiresense.domain.Job;
import com.hiresense.domain.enums.ApplicationStatus;
import com.hiresense.dto.ApplicationDtos.ApplicationResponse;
import com.hiresense.dto.JobDtos.JobRequest;
import com.hiresense.dto.JobDtos.JobResponse;
import com.hiresense.dto.JobDtos.SkillRequest;
import com.hiresense.dto.JobDtos.SkillResponse;
import com.hiresense.dto.MatchDtos.ApplicationRequest;
import com.hiresense.dto.MatchDtos.StatusRequest;
import com.hiresense.exception.ApiException;
import com.hiresense.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService service;
    public JobController(JobService service) { this.service = service; }

    @GetMapping
    public Page<JobResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size, true);
    }

    @GetMapping("/{id}")
    public JobResponse one(@PathVariable Long id) { return service.getActiveResponse(id); }

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PostMapping
    public JobResponse create(Authentication authentication, @Valid @RequestBody JobRequest request) {
        return service.create(authentication.getName(), request);
    }

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PutMapping("/{id}")
    public JobResponse update(@PathVariable Long id, @Valid @RequestBody JobRequest request) { return service.update(id, request); }

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PutMapping("/{id}/skills")
    public List<SkillResponse> skills(@PathVariable Long id, @Valid @RequestBody List<SkillRequest> request) { return service.setSkills(id, request); }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/{id}/applications")
    public ApplicationResponse apply(Authentication authentication, @PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
        return service.apply(authentication.getName(), id, request.resumeId());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/applications/me")
    public List<ApplicationResponse> mine(Authentication authentication) { return service.applicationsFor(authentication.getName()); }

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PatchMapping("/applications/{applicationId}/status")
    public ApplicationResponse status(@PathVariable Long applicationId, @Valid @RequestBody StatusRequest request) {
        try { return service.status(applicationId, ApplicationStatus.valueOf(request.status().toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException e) { throw ApiException.bad("Invalid application status"); }
    }
}
