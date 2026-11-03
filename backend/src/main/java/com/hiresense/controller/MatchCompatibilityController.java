package com.hiresense.controller;

import com.hiresense.dto.MatchDtos.MatchRequest;
import com.hiresense.dto.MatchDtos.MatchResponse;
import com.hiresense.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MatchCompatibilityController {
    private final MatchService matches;

    public MatchCompatibilityController(MatchService matches) { this.matches = matches; }

    @PostMapping("/match")
    public MatchResponse calculate(Authentication authentication, @Valid @RequestBody MatchRequest request) {
        return matches.scoreForUser(authentication.getName(), request.resumeId(), request.jobId());
    }
}
