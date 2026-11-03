package com.hiresense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class MatchDtos {
    private MatchDtos() {}

    public record MatchResponse(Long id, Long resumeId, Long jobId, BigDecimal overallScore,
                                BigDecimal semanticScore, BigDecimal skillScore, BigDecimal experienceScore,
                                BigDecimal educationScore, List<String> matchedSkills, List<String> missingSkills,
                                Map<String, Object> sectionFeedback, List<String> improvementTips) {}

    public record MatchRequest(@NotNull @Positive Long resumeId, @NotNull @Positive Long jobId) {}
    public record ApplicationRequest(@NotNull Long resumeId) {}
    public record StatusRequest(@NotNull String status) {}
}
