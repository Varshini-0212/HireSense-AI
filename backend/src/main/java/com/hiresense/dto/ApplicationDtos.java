package com.hiresense.dto;

import java.time.LocalDateTime;

public final class ApplicationDtos {
    private ApplicationDtos() {}
    public record ApplicationResponse(Long id, Long jobId, String jobTitle, Long candidateProfileId,
                                      String candidateName, String candidateEmail, Long resumeId,
                                      String status, LocalDateTime appliedAt) {}
}
