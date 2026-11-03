package com.hiresense.dto;

import java.time.LocalDateTime;

public final class ActivityLogDtos {
    private ActivityLogDtos() {}
    public record ActivityResponse(Long id, String actorName, String actorEmail, String action,
                                   String entityType, String entityId, String ipAddress,
                                   LocalDateTime createdAt) {}
}
