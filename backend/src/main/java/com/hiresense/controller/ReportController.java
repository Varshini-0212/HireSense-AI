package com.hiresense.controller;

import com.hiresense.service.MatchService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final MatchService matches;
    public ReportController(MatchService matches) { this.matches = matches; }

    @GetMapping("/match/{matchId}/pdf")
    public ResponseEntity<byte[]> matchPdf(Authentication authentication, @PathVariable Long matchId) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hiresense-match-" + matchId + ".pdf")
                .body(matches.reportForUser(authentication.getName(), matchId));
    }
}
