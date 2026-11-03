package com.hiresense.service;

import com.hiresense.domain.ActivityLog;
import com.hiresense.dto.ActivityLogDtos.ActivityResponse;
import com.hiresense.repository.ActivityLogRepository;
import com.hiresense.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {
    private final ActivityLogRepository logs;
    private final AppUserRepository users;
    public ActivityLogService(ActivityLogRepository logs, AppUserRepository users) { this.logs = logs; this.users = users; }

    @Transactional
    public void record(String email, String method, String path, String remoteAddress, int status) {
        ActivityLog entry = new ActivityLog();
        if (email != null && !email.isBlank()) users.findByEmailIgnoreCase(email).ifPresent(entry::setActor);
        entry.setAction(method + " " + path);
        entry.setEntityType("API_REQUEST");
        entry.setEntityId(path);
        entry.setIpAddress(remoteAddress);
        entry.setDetailsJson("{\"status\":" + status + "}");
        logs.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> recent(int page, int size) {
        return logs.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)))
                .map(entry -> new ActivityResponse(entry.getId(),
                        entry.getActor() == null ? null : entry.getActor().getFullName(),
                        entry.getActor() == null ? null : entry.getActor().getEmail(),
                        entry.getAction(), entry.getEntityType(), entry.getEntityId(),
                        entry.getIpAddress(), entry.getCreatedAt()));
    }
}
