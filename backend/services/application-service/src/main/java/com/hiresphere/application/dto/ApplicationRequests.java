package com.hiresphere.application.dto;

import com.hiresphere.common.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class ApplicationRequests {

    private ApplicationRequests() {
    }

    public record Apply(
            @NotNull Long jobId,
            @NotNull Long resumeId,
            @Size(max = 8000) String coverLetter,
            BigDecimal expectedSalary,
            @Size(max = 60) String noticePeriod,
            LocalDate availableFrom,
            Map<String, String> answers) {
    }

    public record StatusChange(
            @NotNull ApplicationStatus status,
            @Size(max = 2000) String note) {
    }

    public record NoteRequest(@NotBlank @Size(max = 4000) String content) {
    }

    public record AiScreeningResult(
            Integer matchScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            String summary,
            String recommendation) {
    }
}
