package com.hiresense.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresense.dto.JobDtos.JobResponse;
import com.hiresense.dto.MatchDtos.MatchResponse;
import com.hiresense.domain.Resume;
import com.hiresense.service.JobService;
import com.hiresense.service.MatchService;
import com.hiresense.service.ResumeService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobInsightsController {
    private final MatchService matches;
    private final JobService jobs;
    private final ResumeService resumes;
    private final ObjectMapper mapper;

    public JobInsightsController(MatchService matches, JobService jobs, ResumeService resumes, ObjectMapper mapper) {
        this.matches = matches;
        this.jobs = jobs;
        this.resumes = resumes;
        this.mapper = mapper;
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/recommended")
    public List<Map<String, Object>> recommended(Authentication authentication,
                                                  @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> output = new ArrayList<>();
        for (MatchResponse match : matches.recommendations(authentication.getName(), limit)) {
            JobResponse job = jobs.getActiveResponse(match.jobId());
            Map<String, Object> item = mapper.convertValue(job, new TypeReference<>() {});
            item.put("matchScore", match.overallScore());
            item.put("matchResultId", match.id());
            output.add(item);
        }
        return output;
    }

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @GetMapping("/{id}/candidates")
    public Page<Map<String, Object>> candidates(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        Page<MatchResponse> ranked = matches.ranked(id, page, size);
        return ranked.map(match -> {
            Map<String, Object> item = mapper.convertValue(match, new TypeReference<>() {});
            item.putAll(resumes.candidateIdentity(match.resumeId()));
            return item;
        });
    }
}
