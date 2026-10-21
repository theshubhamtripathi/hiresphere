package com.hiresphere.job.repository;

import com.hiresphere.job.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Skill> findByNameInIgnoreCase(List<String> names);

    List<Skill> findAllByOrderByNameAsc();
}
