package com.hiresense.controller;

import com.hiresense.dto.ProfileDtos.ProfileRequest;
import com.hiresense.dto.ProfileDtos.ProfileResponse;
import com.hiresense.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping({"", "/me"})
    public ProfileResponse get(Authentication authentication) { return service.get(authentication.getName()); }

    @PutMapping({"", "/me"})
    public ProfileResponse update(Authentication authentication, @Valid @RequestBody ProfileRequest request) {
        return service.update(authentication.getName(), request);
    }

    @GetMapping("/resumes")
    public Object resumes(Authentication authentication) { return service.resumes(authentication.getName()); }
}
