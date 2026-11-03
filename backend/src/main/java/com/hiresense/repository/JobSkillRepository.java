package com.hiresense.repository;

import com.hiresense.domain.JobSkill;
import com.hiresense.domain.enums.JobSkillType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    List<JobSkill> findAllByJobId(Long jobId);
    List<JobSkill> findAllByJobIdAndSkillType(Long jobId, JobSkillType skillType);
}
