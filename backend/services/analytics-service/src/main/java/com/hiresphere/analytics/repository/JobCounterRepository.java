package com.hiresphere.analytics.repository;

import com.hiresphere.analytics.domain.JobCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobCounterRepository extends JpaRepository<JobCounter, Long> {

    Optional<JobCounter> findByJobId(Long jobId);

    List<JobCounter> findTop5ByCompanyIdOrderByApplicationsDesc(Long companyId);
}
