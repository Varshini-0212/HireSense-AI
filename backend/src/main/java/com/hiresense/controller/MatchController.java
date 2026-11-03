package com.hiresense.controller;

import com.hiresense.dto.MatchDtos.MatchResponse;
import com.hiresense.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matches;

    public MatchController(MatchService matches) { this.matches = matches; }

    @PostMapping("/score")
    public MatchResponse score(Authentication authentication, @RequestParam Long resumeId, @RequestParam Long jobId) {
        return matches.scoreForUser(authentication.getName(), resumeId, jobId);
    }

    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @GetMapping("/jobs/{jobId}")
    public Page<MatchResponse> ranked(@PathVariable Long jobId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return matches.ranked(jobId, page, size);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/recommendations")
    public List<MatchResponse> recommendations(Authentication authentication,
                                                @RequestParam(defaultValue = "10") int size) {
        return matches.recommendations(authentication.getName(), size);
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> report(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=match-report-" + id + ".pdf")
                .body(matches.reportForUser(authentication.getName(), id));
    }
}
