package com.hiresense.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresense.domain.AttritionPrediction;
import com.hiresense.domain.Employee;
import com.hiresense.domain.enums.AttritionRiskLevel;
import com.hiresense.dto.AttritionDtos.DashboardResponse;
import com.hiresense.dto.AttritionDtos.DepartmentRisk;
import com.hiresense.dto.AttritionDtos.HighRiskEmployee;
import com.hiresense.dto.AttritionDtos.PredictionResponse;
import com.hiresense.exception.ApiException;
import com.hiresense.repository.AttritionPredictionRepository;
import com.hiresense.repository.EmployeeRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttritionService {
    private final EmployeeRepository employees;
    private final AttritionPredictionRepository predictions;
    private final MlClient ml;
    private final ObjectMapper json;

    public AttritionService(EmployeeRepository employees, AttritionPredictionRepository predictions, MlClient ml, ObjectMapper json) {
        this.employees = employees;
        this.predictions = predictions;
        this.ml = ml;
        this.json = json;
    }

    @Transactional
    public PredictionResponse predict(Long id) {
        Employee employee = employees.findById(id).orElseThrow(() -> ApiException.notFound("Employee not found"));
        Map<String, Object> output = ml.attrition(features(employee));
        double probability = output.isEmpty() ? fallback(employee) : number(output.get("probability"), fallback(employee));
        probability = Math.max(0, Math.min(1, probability));
        String risk = String.valueOf(output.getOrDefault("risk_level", riskFor(probability))).toUpperCase(Locale.ROOT);
        List<String> factors = list(output.get("top_factors"));
        if (factors.isEmpty()) factors = factors(employee);
        List<String> actions = list(output.get("suggested_actions"));
        if (actions.isEmpty()) actions = List.of(probability > .65
                ? "Schedule a confidential retention conversation"
                : "Maintain regular career development check-ins");
        String version = String.valueOf(output.getOrDefault("model_version", "fallback-v1"));

        AttritionPrediction prediction = new AttritionPrediction();
        prediction.setEmployee(employee);
        prediction.setAttritionProbability(BigDecimal.valueOf(probability).setScale(5, RoundingMode.HALF_UP));
        try { prediction.setRiskLevel(AttritionRiskLevel.valueOf(risk)); }
        catch (IllegalArgumentException ex) { prediction.setRiskLevel(AttritionRiskLevel.valueOf(riskFor(probability))); }
        prediction.setTopFactorsJson(write(factors));
        prediction.setSuggestedActionsJson(write(actions));
        prediction.setModelVersion(version);
        predictions.save(prediction);
        return new PredictionResponse(id, prediction.getAttritionProbability(), prediction.getRiskLevel().name(), factors, actions, version);
    }

    public Map<String, Object> train() {
        Map<String, Object> result = ml.train(Map.of("source", "employees"));
        if (result.isEmpty()) return Map.of("status", "fallback", "model_version", "fallback-v1",
                "metrics", Map.of("accuracy", 0.0, "message", "ML service unavailable; no model was trained"));
        return result;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<Employee> employeeRows = employees.findAll();
        Map<Long, AttritionPrediction> latest = new HashMap<>();
        for (AttritionPrediction prediction : predictions.findAll()) {
            Long employeeId = prediction.getEmployee().getId();
            latest.merge(employeeId, prediction, (left, right) ->
                    left.getCreatedAt().isAfter(right.getCreatedAt()) ? left : right);
        }

        long high = 0, medium = 0, low = 0, knownAttrition = 0;
        Map<String, long[]> byDepartment = new TreeMap<>();
        for (Employee employee : employeeRows) {
            if (Boolean.TRUE.equals(employee.getAttrition())) knownAttrition++;
            String department = employee.getDepartment() == null || employee.getDepartment().isBlank() ? "Unspecified" : employee.getDepartment();
            long[] counts = byDepartment.computeIfAbsent(department, ignored -> new long[5]);
            counts[0]++;
            AttritionPrediction prediction = latest.get(employee.getId());
            if (prediction == null) counts[4]++;
            else if (prediction.getRiskLevel() == AttritionRiskLevel.HIGH) { high++; counts[1]++; }
            else if (prediction.getRiskLevel() == AttritionRiskLevel.MEDIUM) { medium++; counts[2]++; }
            else { low++; counts[3]++; }
        }
        List<DepartmentRisk> departmentRisks = byDepartment.entrySet().stream()
                .map(entry -> new DepartmentRisk(entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3], entry.getValue()[4]))
                .toList();
        List<HighRiskEmployee> highRiskEmployees = latest.values().stream()
                .filter(p -> p.getRiskLevel() == AttritionRiskLevel.HIGH)
                .sorted(Comparator.comparing(AttritionPrediction::getAttritionProbability).reversed())
                .limit(25)
                .map(p -> {
                    Employee e = p.getEmployee();
                    return new HighRiskEmployee(e.getId(), e.getEmployeeCode(), e.getFullName(), e.getDepartment(), e.getJobRole(), p.getAttritionProbability(), p.getCreatedAt());
                }).toList();
        return new DashboardResponse(employeeRows.size(), high, medium, low, knownAttrition, departmentRisks, highRiskEmployees);
    }

    private Map<String, Object> features(Employee e) {
        Map<String, Object> values = new LinkedHashMap<>();
        double satisfaction = scale(e.getJobSatisfaction(), 3);
        double balance = scale(e.getWorkLifeBalance(), 3);
        values.put("satisfaction_level", e.getJobSatisfaction() == null && e.getWorkLifeBalance() == null ? .65 : .7 * satisfaction + .3 * balance);
        values.put("last_evaluation", e.getPerformanceRating() == null ? .7 : Math.max(.25, Math.min(1, e.getPerformanceRating() / 4.0)));
        values.put("number_project", 4);
        values.put("average_monthly_hours", e.isOvertime() ? 250 : 180);
        values.put("time_spend_company", e.getYearsAtCompany() == null ? 0 : e.getYearsAtCompany());
        values.put("work_accident", 0);
        values.put("promotion_last_5years", e.getPromotions() != null && e.getPromotions() > 0 && (e.getYearsSinceLastPromotion() == null || e.getYearsSinceLastPromotion() <= 5) ? 1 : 0);
        values.put("department", department(e.getDepartment()));
        values.put("salary", salaryBand(e.getMonthlyIncome()));
        values.put("training_hours", e.getTrainingTimesLastYear() == null ? 0 : e.getTrainingTimesLastYear() * 8);
        values.put("absenteeism_days", 0);
        values.put("remote_work_days", 0);
        return values;
    }

    private double scale(Integer value, int max) { return value == null ? .65 : Math.max(0, Math.min(1, (value - 1.0) / max)); }
    private String riskFor(double probability) { return probability > .65 ? "HIGH" : probability >= .35 ? "MEDIUM" : "LOW"; }
    private String salaryBand(BigDecimal income) { if (income == null) return "medium"; if (income.compareTo(BigDecimal.valueOf(5000)) < 0) return "low"; if (income.compareTo(BigDecimal.valueOf(10000)) < 0) return "medium"; return "high"; }
    private String department(String value) {
        if (value == null || value.isBlank()) return "technical";
        String d = value.trim().toLowerCase(Locale.ROOT);
        if (d.contains("engineer") || d.equals("it")) return "technical";
        if (d.contains("support") || d.contains("success")) return "support";
        if (d.contains("research") || d.equals("r&d")) return "RandD";
        if (d.contains("human") || d.equals("hr")) return "hr";
        if (d.contains("sale")) return "sales";
        if (d.contains("market")) return "marketing";
        if (d.contains("product")) return "product";
        if (d.contains("finance")) return "finance";
        if (d.contains("account")) return "accounting";
        if (d.contains("manage")) return "management";
        return d;
    }
    private double fallback(Employee e) {
        double score = .1;
        if (e.isOvertime()) score += .25;
        if (e.getJobSatisfaction() != null && e.getJobSatisfaction() <= 2) score += .2;
        if (e.getYearsSinceLastPromotion() != null && e.getYearsSinceLastPromotion() > 4) score += .2;
        if (e.getDistanceFromHomeKm() != null && e.getDistanceFromHomeKm() > 20) score += .15;
        if (e.getStockOptionLevel() != null && e.getStockOptionLevel() == 0) score += .15;
        return Math.min(.95, score);
    }
    private List<String> factors(Employee e) {
        List<String> result = new ArrayList<>();
        if (e.isOvertime()) result.add("overtime");
        if (e.getJobSatisfaction() != null && e.getJobSatisfaction() <= 2) result.add("low job satisfaction");
        if (e.getYearsSinceLastPromotion() != null && e.getYearsSinceLastPromotion() > 4) result.add("time since last promotion");
        if (result.isEmpty()) result.add("baseline workforce indicators");
        return result;
    }
    private double number(Object value, double fallback) { try { return Double.parseDouble(String.valueOf(value)); } catch (Exception e) { return fallback; } }
    private List<String> list(Object value) { return value instanceof List<?> items ? items.stream().map(String::valueOf).toList() : List.of(); }
    private String write(Object value) { try { return json.writeValueAsString(value); } catch (Exception e) { return "[]"; } }
}
