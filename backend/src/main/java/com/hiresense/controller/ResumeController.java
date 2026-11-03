package com.hiresense.controller;

import com.hiresense.dto.ResumeDtos.ResumeResponse;
import com.hiresense.service.ResumeService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService service;

    public ResumeController(ResumeService service) { this.service = service; }

    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
    @PostMapping(path = {"", "/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> upload(Authentication authentication,
                                                  @RequestPart("file") @NotNull MultipartFile file,
                                                  @RequestParam(required = false) Long candidateProfileId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(authentication.getName(), candidateProfileId, file));
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping
    public List<ResumeResponse> list(Authentication authentication) { return service.listFor(authentication.getName()); }

    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
    @GetMapping("/{id}")
    public ResumeResponse get(Authentication authentication, @PathVariable Long id) { return service.getResponse(id, authentication.getName()); }
}
