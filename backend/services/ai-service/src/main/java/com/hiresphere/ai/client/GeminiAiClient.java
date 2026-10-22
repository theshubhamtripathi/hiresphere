package com.hiresphere.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresphere.ai.config.GeminiProperties;
import com.hiresphere.ai.dto.AiRequests;
import com.hiresphere.ai.dto.AiResults;
import com.hiresphere.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calls the Gemini REST API directly (no Spring AI dependency). Structured
 * endpoints ask for JSON and parse it, retrying once on malformed output.
 */
@Slf4j
public class GeminiAiClient implements AiClient {

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final MockAiClient fallback;
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();

    public GeminiAiClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.fallback = new MockAiClient(objectMapper);
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(5000));
        factory.setReadTimeout(Duration.ofMillis(properties.timeoutMs()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
        log.info("Gemini AI client active (model {}).", properties.model());
    }

    @Override
    public String generateSummary(AiRequests.Summary r) {
        return generateText(prompt("resume-summary", Map.of(
                "role", r.role(), "skills", join(r.skills()), "experience", nullSafe(r.experience()))),
                () -> fallback.generateSummary(r));
    }

    @Override
    public String improveBullet(AiRequests.ImproveBullet r) {
        return generateText(prompt("improve-bullet", Map.of(
                "bullet", r.bullet(), "role", nullSafe(r.role()))),
                () -> fallback.improveBullet(r));
    }

    @Override
    public AiResults.ResumeAnalysis analyzeResume(String resumeJson) {
        return generateJson(prompt("resume-analyze", Map.of("resume", resumeJson)),
                AiResults.ResumeAnalysis.class, () -> fallback.analyzeResume(resumeJson));
    }

    @Override
    public String generateJobDescription(AiRequests.JobDescription r) {
        return generateText(prompt("job-description", Map.of(
                "title", r.title(), "company", nullSafe(r.companyName()),
                "skills", join(r.skills()), "seniority", nullSafe(r.seniority()),
                "context", nullSafe(r.extraContext()))),
                () -> fallback.generateJobDescription(r));
    }

    @Override
    public List<String> suggestSkills(AiRequests.SkillsSuggest r) {
        AiResults.SkillsResult result = generateJson(prompt("skills-suggest", Map.of(
                "title", r.title(), "description", nullSafe(r.description()))),
                AiResults.SkillsResult.class, () -> new AiResults.SkillsResult(fallback.suggestSkills(r)));
        return result.skills() == null ? fallback.suggestSkills(r) : result.skills();
    }

    @Override
    public String generateCoverLetter(String resumeJson, String jobJson) {
        return generateText(prompt("cover-letter", Map.of("resume", resumeJson, "job", jobJson)),
                () -> fallback.generateCoverLetter(resumeJson, jobJson));
    }

    @Override
    public AiResults.Screening screen(String resumeJson, String jobJson) {
        return generateJson(prompt("screen", Map.of("resume", resumeJson, "job", jobJson)),
                AiResults.Screening.class, () -> fallback.screen(resumeJson, jobJson));
    }

    @Override
    public AiResults.SearchFilters parseSearch(String query) {
        return generateJson(prompt("search", Map.of("query", query)),
                AiResults.SearchFilters.class, () -> fallback.parseSearch(query));
    }

    @Override
    public boolean isLive() {
        return true;
    }

    // ---- Gemini plumbing ----

    private String generateText(String prompt, java.util.function.Supplier<String> onFailure) {
        try {
            return call(prompt);
        } catch (RuntimeException ex) {
            log.warn("Gemini text call failed ({}); using fallback.", ex.getMessage());
            return onFailure.get();
        }
    }

    private <T> T generateJson(String prompt, Class<T> type, java.util.function.Supplier<T> onFailure) {
        String jsonPrompt = prompt + "\n\nRespond with ONLY valid minified JSON, no markdown fences.";
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                String raw = call(jsonPrompt);
                return objectMapper.readValue(stripFences(raw), type);
            } catch (Exception ex) {
                log.warn("Gemini JSON attempt {} failed: {}", attempt, ex.getMessage());
            }
        }
        return onFailure.get();
    }

    @SuppressWarnings("unchecked")
    private String call(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", 0.6, "maxOutputTokens", 2048));
        JsonNode response = restClient.post()
                .uri("/models/{model}:generateContent?key={key}", properties.model(), properties.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new BadRequestException("Empty response from Gemini");
        }
        JsonNode parts = response.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new BadRequestException("Malformed Gemini response");
        }
        return parts.get(0).path("text").asText("").trim();
    }

    private String stripFences(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            t = t.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        return t;
    }

    private String prompt(String name, Map<String, String> vars) {
        String template = promptCache.computeIfAbsent(name, this::load);
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace("{{" + e.getKey() + "}}", e.getValue());
        }
        return result;
    }

    private String load(String name) {
        try {
            return new String(new ClassPathResource("prompts/" + name + ".txt")
                    .getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Missing prompt template: " + name, e);
        }
    }

    private String join(List<String> list) {
        return list == null || list.isEmpty() ? "" : String.join(", ", list);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
