package com.hiresphere.preference.dto;

import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.preference.domain.AlertFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PreferenceDtos {

    private PreferenceDtos() {
    }

    public record PreferenceRequest(
            List<String> desiredTitles,
            List<String> locations,
            List<JobType> jobTypes,
            List<WorkMode> workModes,
            BigDecimal minSalary,
            List<Long> categoryIds,
            List<String> skills,
            Boolean alertsEnabled,
            AlertFrequency alertFrequency) {
    }

    public record PreferenceResponse(
            Long userId,
            List<String> desiredTitles,
            List<String> locations,
            List<JobType> jobTypes,
            List<WorkMode> workModes,
            BigDecimal minSalary,
            List<Long> categoryIds,
            List<String> skills,
            boolean alertsEnabled,
            AlertFrequency alertFrequency) {
    }

    public record SavedJobResponse(
            Long id,
            Long jobId,
            String title,
            Long companyId,
            String location,
            String jobType,
            String workMode,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String currency,
            String status,
            Instant savedAt) {
    }
}
