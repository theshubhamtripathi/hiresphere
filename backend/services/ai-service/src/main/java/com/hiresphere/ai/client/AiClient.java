package com.hiresphere.ai.client;

import com.hiresphere.ai.dto.AiRequests;
import com.hiresphere.ai.dto.AiResults;

import java.util.List;

/**
 * Abstraction over the AI provider. Implemented by {@code GeminiAiClient}
 * (real Gemini REST) and {@code MockAiClient} (deterministic, offline).
 */
public interface AiClient {

    String generateSummary(AiRequests.Summary request);

    String improveBullet(AiRequests.ImproveBullet request);

    AiResults.ResumeAnalysis analyzeResume(String resumeJson);

    String generateJobDescription(AiRequests.JobDescription request);

    List<String> suggestSkills(AiRequests.SkillsSuggest request);

    String generateCoverLetter(String resumeJson, String jobJson);

    AiResults.Screening screen(String resumeJson, String jobJson);

    AiResults.SearchFilters parseSearch(String query);

    /** True when backed by a real provider; false for the mock. */
    boolean isLive();
}
