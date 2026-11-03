package com.hiresense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parsed_resume_data")
public class ParsedResumeData extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    @Column(name = "full_text", columnDefinition = "LONGTEXT")
    private String fullText;

    @Column(name = "extracted_name", length = 160)
    private String extractedName;

    @Column(name = "extracted_email", length = 190)
    private String extractedEmail;

    @Column(name = "extracted_phone", length = 40)
    private String extractedPhone;

    @Column(name = "years_experience", precision = 4, scale = 1)
    private BigDecimal yearsExperience;

    @Column(name = "skills_json", nullable = false, columnDefinition = "json")
    private String skillsJson = "[]";

    @Column(name = "education_json", columnDefinition = "json")
    private String educationJson;

    @Column(name = "certifications_json", columnDefinition = "json")
    private String certificationsJson;

    @Column(name = "projects_json", columnDefinition = "json")
    private String projectsJson;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "parsed_resume_skills",
            joinColumns = @JoinColumn(name = "parsed_resume_data_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();
}
