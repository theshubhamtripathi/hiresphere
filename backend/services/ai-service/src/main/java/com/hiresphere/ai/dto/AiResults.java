package com.hiresphere.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

public final class AiResults {

    private AiResults() {
    }

    public record TextResult(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResumeAnalysis(int score, List<String> strengths, List<String> gaps, List<String> atsTips) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Screening(int matchScore, List<String> matchedSkills, List<String> missingSkills,
                            String summary, String recommendation) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchFilters(String keywords, String location, String jobType, String workMode,
                                String experienceLevel, BigDecimal salaryMin, List<String> skills) {
    }

    public record SkillsResult(List<String> skills) {
    }

    public record SearchResponse(SearchFilters filters, Object results) {
    }
}
