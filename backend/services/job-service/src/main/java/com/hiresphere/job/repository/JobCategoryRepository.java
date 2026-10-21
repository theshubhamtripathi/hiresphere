package com.hiresphere.job.repository;

import com.hiresphere.job.domain.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    Optional<JobCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
