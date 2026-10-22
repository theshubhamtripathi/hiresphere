package com.hiresphere.application.dto;

import com.hiresphere.common.enums.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class ApplicationResponses {

    private ApplicationResponses() {
    }

    public record Summary(
            Long id,
            Long jobId,
            Long applicantId,
            Long companyId,
            ApplicationStatus status,
            Integer aiMatchScore,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record Detail(
            Long id,
            Long jobId,
            Long applicantId,
            Long companyId,
            Long resumeId,
            String resumeSnapshot,
            String coverLetter,
            BigDecimal expectedSalary,
            String noticePeriod,
            LocalDate availableFrom,
            Map<String, String> answers,
            ApplicationStatus status,
            Integer aiMatchScore,
            List<String> aiMatchedSkills,
            List<String> aiMissingSkills,
            String aiSummary,
            String aiRecommendation,
            List<StatusHistoryItem> history,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record StatusHistoryItem(
            ApplicationStatus fromStatus,
            ApplicationStatus toStatus,
            Long changedBy,
            String note,
            Instant at) {
    }

    public record Note(Long id, Long authorId, String content, Instant createdAt) {
    }

    public record Check(boolean applied, Long applicationId, ApplicationStatus status) {
    }
}
