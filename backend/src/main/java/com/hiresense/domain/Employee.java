package com.hiresense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_department", columnList = "department"),
        @Index(name = "idx_employee_attrition", columnList = "attrition")
})
public class Employee extends BaseEntity {
    @Column(name = "employee_code", nullable = false, unique = true, length = 40)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "job_role", nullable = false, length = 120)
    private String jobRole;

    @Column(name = "business_travel", length = 40)
    private String businessTravel;

    @Column(name = "monthly_income", precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "years_at_company")
    private Integer yearsAtCompany;

    @Column(name = "total_experience_years")
    private Integer totalExperienceYears;

    @Column(name = "job_satisfaction")
    private Integer jobSatisfaction;

    @Column(name = "work_life_balance")
    private Integer workLifeBalance;

    @Column(name = "performance_rating")
    private Integer performanceRating;

    @Column(name = "overtime", nullable = false)
    private boolean overtime;

    @Column(name = "distance_from_home_km")
    private Integer distanceFromHomeKm;

    @Column(name = "promotions")
    private Integer promotions;

    @Column(name = "training_times_last_year")
    private Integer trainingTimesLastYear;

    @Column(name = "years_in_current_role")
    private Integer yearsInCurrentRole;

    @Column(name = "years_since_last_promotion")
    private Integer yearsSinceLastPromotion;

    @Column(name = "years_with_current_manager")
    private Integer yearsWithCurrentManager;

    @Column(name = "num_companies_worked")
    private Integer numCompaniesWorked;

    @Column(name = "stock_option_level")
    private Integer stockOptionLevel;

    @Column(name = "percent_salary_hike")
    private Integer percentSalaryHike;

    @Column(name = "attrition")
    private Boolean attrition;
}
