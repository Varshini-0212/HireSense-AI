package com.hiresense.repository;

import com.hiresense.domain.CandidateApplication;
import com.hiresense.domain.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {
    Page<CandidateApplication> findAllByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);
    List<CandidateApplication> findAllByCandidateProfileIdOrderByAppliedAtDesc(Long candidateProfileId);
    boolean existsByCandidateProfileIdAndJobId(Long candidateProfileId, Long jobId);
}
