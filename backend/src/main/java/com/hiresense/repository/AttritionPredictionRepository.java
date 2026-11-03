package com.hiresense.repository;

import com.hiresense.domain.AttritionPrediction;
import com.hiresense.domain.enums.AttritionRiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttritionPredictionRepository extends JpaRepository<AttritionPrediction, Long> {
    Optional<AttritionPrediction> findFirstByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    Page<AttritionPrediction> findAllByRiskLevel(AttritionRiskLevel riskLevel, Pageable pageable);
}
