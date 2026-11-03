package com.hiresense.dto;

import com.hiresense.domain.enums.JobSkillType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public final class JobDtos {
    private JobDtos() {}

    public record JobRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 50000) String description,
            @DecimalMin("0") BigDecimal minExperienceYears,
            @Size(max = 120) String educationLevel,
            @DecimalMin("0") BigDecimal salaryMin,
            @DecimalMin("0") BigDecimal salaryMax,
            @Size(max = 160) String location,
            Boolean active,
            @Size(max = 100) List<@NotBlank @Size(max = 100) String> requiredSkills,
            @Size(max = 100) List<@NotBlank @Size(max = 100) String> preferredSkills) {}

    public record SkillRequest(@NotNull Long skillId, @NotNull JobSkillTypeValue skillType,
                               @Min(1) @Max(10) int importance) {}
    public enum JobSkillTypeValue { REQUIRED, PREFERRED }
    public record JobResponse(Long id, String title, String description, BigDecimal minExperienceYears,
                              String educationLevel, BigDecimal salaryMin, BigDecimal salaryMax,
                              String location, boolean active, List<SkillResponse> skills) {}
    public record SkillResponse(Long id, String name, String category, String type, int importance) {}
}
