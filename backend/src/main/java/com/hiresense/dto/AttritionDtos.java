package com.hiresense.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class AttritionDtos {
    private AttritionDtos() {}
    public record PredictionResponse(Long employeeId, BigDecimal probability, String riskLevel,
                                    List<String> topFactors, List<String> suggestedActions, String modelVersion) {}
    public record DepartmentRisk(String department, long employees, long highRisk, long mediumRisk,
                                 long lowRisk, long unscored) {}
    public record HighRiskEmployee(Long id, String employeeCode, String fullName, String department,
                                   String jobRole, BigDecimal probability, LocalDateTime predictedAt) {}
    public record DashboardResponse(long totalEmployees, long highRisk, long mediumRisk, long lowRisk,
                                    long knownAttrition, List<DepartmentRisk> departmentRisks,
                                    List<HighRiskEmployee> highRiskEmployees) {}
}
