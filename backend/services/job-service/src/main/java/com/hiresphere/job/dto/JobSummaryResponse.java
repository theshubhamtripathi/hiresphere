package com.hiresphere.job.dto;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.job.domain.JobStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Lightweight projection for list endpoints. */
public record JobSummaryResponse(
        Long id,
        String title,
        Long companyId,
        Long categoryId,
        JobType jobType,
        WorkMode workMode,
        ExperienceLevel experienceLevel,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        String location,
        JobStatus status,
        boolean featured,
        long applicationsCount,
        LocalDate deadline,
        List<String> skills,
        Instant createdAt) {
}
