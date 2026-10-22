package com.hiresphere.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AiRequests {

    private AiRequests() {
    }

    public record Summary(
            @NotBlank @Size(max = 160) String role,
            List<String> skills,
            @Size(max = 2000) String experience) {
    }

    public record ImproveBullet(
            @NotBlank @Size(max = 600) String bullet,
            @Size(max = 160) String role) {
    }

    public record Analyze(@NotNull Long resumeId) {
    }

    public record JobDescription(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 120) String companyName,
            List<String> skills,
            @Size(max = 40) String seniority,
            @Size(max = 2000) String extraContext) {
    }

    public record SkillsSuggest(
            @NotBlank @Size(max = 160) String title,
            @Size(max = 8000) String description) {
    }

    public record CoverLetter(
            @NotNull Long resumeId,
            @NotNull Long jobId) {
    }

    public record Search(@NotBlank @Size(max = 500) String query) {
    }

    /** Payload sent to application-service to persist screening results. */
    public record ScreeningPersist(Integer matchScore, List<String> matchedSkills,
                                   List<String> missingSkills, String summary, String recommendation) {
    }
}
