package com.hiresphere.job.dto;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.job.domain.JobStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Serializable so it can be cached in Redis. */
public record JobResponse(
        Long id,
        String title,
        String description,
        String responsibilities,
        String requirements,
        String benefits,
        Long companyId,
        Long postedBy,
        Long categoryId,
        JobType jobType,
        WorkMode workMode,
        ExperienceLevel experienceLevel,
        Integer minExperience,
        Integer maxExperience,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        String location,
        Integer openings,
        LocalDate deadline,
        JobStatus status,
        long viewsCount,
        long applicationsCount,
        boolean featured,
        List<String> skills,
        List<String> tags,
        Instant createdAt) implements Serializable {
}
