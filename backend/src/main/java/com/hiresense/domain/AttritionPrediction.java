package com.hiresense.domain;

import com.hiresense.domain.enums.AttritionRiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "attrition_predictions")
public class AttritionPrediction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attrition_probability", nullable = false, precision = 6, scale = 5)
    private BigDecimal attritionProbability;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 12)
    private AttritionRiskLevel riskLevel;

    @Column(name = "top_factors_json", nullable = false, columnDefinition = "json")
    private String topFactorsJson = "[]";

    @Column(name = "suggested_actions_json", nullable = false, columnDefinition = "json")
    private String suggestedActionsJson = "[]";

    @Column(name = "model_version", length = 80)
    private String modelVersion;
}
