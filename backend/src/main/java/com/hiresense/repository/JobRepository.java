package com.hiresense.repository;

import com.hiresense.domain.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findAllByActiveTrue(Pageable pageable);
    Page<Job> findAllByCreatedById(Long userId, Pageable pageable);
}
