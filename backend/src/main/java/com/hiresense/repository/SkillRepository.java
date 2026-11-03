package com.hiresense.repository;

import com.hiresense.domain.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
    Page<Skill> findAllByCategoryIgnoreCase(String category, Pageable pageable);
}
