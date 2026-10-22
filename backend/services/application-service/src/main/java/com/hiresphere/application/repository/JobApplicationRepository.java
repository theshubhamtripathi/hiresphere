package com.hiresphere.application.repository;

import com.hiresphere.application.domain.JobApplication;
import com.hiresphere.common.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobIdAndApplicantId(Long jobId, Long applicantId);

    Optional<JobApplication> findByJobIdAndApplicantId(Long jobId, Long applicantId);

    Page<JobApplication> findByApplicantId(Long applicantId, Pageable pageable);

    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);

    Page<JobApplication> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);

    Page<JobApplication> findByCompanyId(Long companyId, Pageable pageable);

    Page<JobApplication> findByCompanyIdAndStatus(Long companyId, ApplicationStatus status, Pageable pageable);
}
