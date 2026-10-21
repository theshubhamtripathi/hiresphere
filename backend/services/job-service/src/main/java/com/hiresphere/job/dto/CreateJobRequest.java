package com.hiresphere.job.dto;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateJobRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 20000) String description,
        @Size(max = 10000) String responsibilities,
        @Size(max = 10000) String requirements,
        @Size(max = 10000) String benefits,
        Long categoryId,
        @NotNull JobType jobType,
        @NotNull WorkMode workMode,
        @NotNull ExperienceLevel experienceLevel,
        Integer minExperience,
        Integer maxExperience,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        @Size(max = 3) String currency,
        @Size(max = 160) String location,
        @Positive Integer openings,
        LocalDate deadline,
        List<String> skills,
        List<String> tags,
        Boolean publish) {
}
