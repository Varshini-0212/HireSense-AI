package com.hiresense.repository;

import com.hiresense.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByCandidateProfileIdOrderByUploadedAtDesc(Long candidateProfileId);
}
