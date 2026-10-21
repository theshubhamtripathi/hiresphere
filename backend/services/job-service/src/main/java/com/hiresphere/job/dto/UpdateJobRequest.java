package com.hiresphere.job.dto;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateJobRequest(
        @Size(max = 160) String title,
        @Size(max = 20000) String description,
        @Size(max = 10000) String responsibilities,
        @Size(max = 10000) String requirements,
        @Size(max = 10000) String benefits,
        Long categoryId,
        JobType jobType,
        WorkMode workMode,
        ExperienceLevel experienceLevel,
        Integer minExperience,
        Integer maxExperience,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        @Size(max = 3) String currency,
        @Size(max = 160) String location,
        Integer openings,
        LocalDate deadline,
        List<String> skills,
        List<String> tags) {
}
