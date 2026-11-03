package com.hiresense.repository;

import com.hiresense.domain.MatchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findAllByResumeIdAndJobIdOrderByCreatedAtDesc(Long resumeId, Long jobId);
    Page<MatchResult> findAllByJobIdOrderByOverallScoreDesc(Long jobId, Pageable pageable);
}
