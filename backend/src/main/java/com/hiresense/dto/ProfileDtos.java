package com.hiresense.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ProfileDtos {
    private ProfileDtos() {}

    public record ProfileRequest(@Size(max = 120) String fullName,
                                 @Size(max = 32) String phone,
                                 @Size(max = 160) String location,
                                 @Size(max = 180) String headline,
                                 @Size(max = 10000) String summary,
                                 @DecimalMin("0") @DecimalMax("80") BigDecimal yearsExperience,
                                 @Size(max = 120) String highestEducation,
                                 @Size(max = 500) String portfolioUrl,
                                 @Size(max = 500) String linkedinUrl) {}

    public record ProfileResponse(Long id, String fullName, String email, String phone, String location,
                                  String headline, String summary, BigDecimal yearsExperience,
                                  String highestEducation, String portfolioUrl, String linkedinUrl) {}
}
