package com.hiresense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "match_results")
public class MatchResult extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private CandidateApplication application;

    @Column(name = "overall_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "semantic_score", precision = 5, scale = 2)
    private BigDecimal semanticScore;

    @Column(name = "skill_score", precision = 5, scale = 2)
    private BigDecimal skillScore;

    @Column(name = "experience_score", precision = 5, scale = 2)
    private BigDecimal experienceScore;

    @Column(name = "education_score", precision = 5, scale = 2)
    private BigDecimal educationScore;

    @Column(name = "matched_skills_json", nullable = false, columnDefinition = "json")
    private String matchedSkillsJson = "[]";

    @Column(name = "missing_skills_json", nullable = false, columnDefinition = "json")
    private String missingSkillsJson = "[]";

    @Column(name = "feedback_json", nullable = false, columnDefinition = "json")
    private String feedbackJson = "{}";

    @Column(name = "improvement_tips_json", nullable = false, columnDefinition = "json")
    private String improvementTipsJson = "[]";
}
