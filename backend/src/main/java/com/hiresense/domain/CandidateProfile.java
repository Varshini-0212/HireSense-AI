package com.hiresense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "candidate_profiles", uniqueConstraints = @UniqueConstraint(name = "uk_candidate_profile_user", columnNames = "user_id"))
public class CandidateProfile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "location", length = 160)
    private String location;

    @Column(name = "headline", length = 180)
    private String headline;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "years_experience", precision = 4, scale = 1)
    private BigDecimal yearsExperience;

    @Column(name = "highest_education", length = 120)
    private String highestEducation;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;
}
