package com.hiresense.repository;

import com.hiresense.domain.ParsedResumeData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParsedResumeDataRepository extends JpaRepository<ParsedResumeData, Long> {
    Optional<ParsedResumeData> findByResumeId(Long resumeId);
}
