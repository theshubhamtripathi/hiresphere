package com.hiresphere.job.dto;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.job.domain.JobStatus;

import java.math.BigDecimal;
import java.util.List;

/** Mutable holder for the (many optional) job-search filters. */
public record JobSearchCriteria(
        String q,
        Long categoryId,
        String location,
        JobType jobType,
        WorkMode workMode,
        ExperienceLevel experienceLevel,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        List<String> skills,
        List<String> tags,
        Long companyId,
        Integer postedWithinDays,
        JobStatus status) {
}
