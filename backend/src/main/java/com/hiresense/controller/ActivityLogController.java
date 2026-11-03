package com.hiresense.controller;

import com.hiresense.dto.ActivityLogDtos.ActivityResponse;
import com.hiresense.service.ActivityLogService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/activity")
@PreAuthorize("hasRole('ADMIN')")
public class ActivityLogController {
    private final ActivityLogService service;
    public ActivityLogController(ActivityLogService service) { this.service = service; }

    @GetMapping
    public Page<ActivityResponse> recent(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        return service.recent(page, size);
    }
}
