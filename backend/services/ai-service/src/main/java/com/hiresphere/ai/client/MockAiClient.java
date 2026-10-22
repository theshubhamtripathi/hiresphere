package com.hiresphere.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresphere.ai.dto.AiRequests;
import com.hiresphere.ai.dto.AiResults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic, offline AI client. Produces realistic output derived from the
 * inputs so the whole platform works without a Gemini key.
 */
@Slf4j
public class MockAiClient implements AiClient {

    private final ObjectMapper objectMapper;

    public MockAiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.warn("GEMINI_API_KEY is not set — using the deterministic mock AI client.");
    }

    @Override
    public String generateSummary(AiRequests.Summary r) {
        String skills = r.skills() == null || r.skills().isEmpty()
                ? "a broad toolkit" : String.join(", ", r.skills());
        return ("Results-driven %s with a track record of delivering reliable, well-tested software. "
                + "Skilled in %s, with strengths in collaboration, clear communication and ownership. "
                + "Focused on building maintainable systems that create measurable business impact.")
                .formatted(r.role(), skills);
    }

    @Override
    public String improveBullet(AiRequests.ImproveBullet r) {
        String bullet = r.bullet().trim().replaceAll("^[-•\\s]+", "");
        return "Led " + lowerFirst(bullet) + ", improving delivery speed and quality while collaborating "
                + "cross-functionally to achieve measurable outcomes.";
    }

    @Override
    public AiResults.ResumeAnalysis analyzeResume(String resumeJson) {
        JsonNode node = parse(resumeJson);
        int base = node.path("completionScore").asInt(60);
        int skillCount = node.path("skills").size();
        int score = Math.min(100, Math.max(35, base + Math.min(20, skillCount * 2)));
        List<String> strengths = new ArrayList<>();
        if (node.path("summary").asText("").length() > 20) strengths.add("Clear professional summary");
        if (skillCount > 0) strengths.add("Relevant technical skills listed");
        if (node.path("experiences").size() > 0) strengths.add("Concrete work experience");
        if (strengths.isEmpty()) strengths.add("Good starting structure");
        List<String> gaps = new ArrayList<>();
        if (node.path("experiences").size() == 0) gaps.add("Add detailed work experience with impact");
        if (skillCount < 5) gaps.add("List more role-relevant skills");
        if (node.path("summary").asText("").isBlank()) gaps.add("Write a compelling summary");
        if (gaps.isEmpty()) gaps.add("Quantify achievements with metrics");
        List<String> ats = List.of(
                "Use standard section headings (Experience, Education, Skills)",
                "Mirror keywords from the target job description",
                "Prefer simple formatting without tables or images");
        return new AiResults.ResumeAnalysis(score, strengths, gaps, ats);
    }

    @Override
    public String generateJobDescription(AiRequests.JobDescription r) {
        String company = r.companyName() == null || r.companyName().isBlank() ? "our team" : r.companyName();
        String skills = r.skills() == null || r.skills().isEmpty()
                ? "modern tools and best practices" : String.join(", ", r.skills());
        return """
                About the role
                %s is hiring a %s to help build and scale impactful products. You will own meaningful
                work from design through delivery and collaborate closely with a supportive team.

                What you'll do
                - Design, build and maintain high-quality features
                - Collaborate with product and design to ship user-focused solutions
                - Write clean, well-tested, maintainable code
                - Contribute to technical decisions and continuous improvement

                What we're looking for
                - Solid experience relevant to a %s role
                - Hands-on expertise with %s
                - Strong communication and a bias for ownership

                What we offer
                - Competitive compensation and growth opportunities
                - A collaborative, learning-focused culture
                """.formatted(company, r.title(), r.seniority() == null ? "the" : r.seniority(), skills);
    }

    @Override
    public List<String> suggestSkills(AiRequests.SkillsSuggest r) {
        String text = (r.title() + " " + (r.description() == null ? "" : r.description())).toLowerCase(Locale.ROOT);
        Set<String> suggestions = new LinkedHashSet<>();
        addIf(suggestions, text, "java", "Java");
        addIf(suggestions, text, "spring", "Spring Boot");
        addIf(suggestions, text, "react", "React");
        addIf(suggestions, text, "python", "Python");
        addIf(suggestions, text, "node", "Node.js");
        addIf(suggestions, text, "aws", "AWS");
        addIf(suggestions, text, "docker", "Docker");
        addIf(suggestions, text, "kubernetes", "Kubernetes");
        addIf(suggestions, text, "sql", "SQL");
        addIf(suggestions, text, "design", "UI/UX Design");
        addIf(suggestions, text, "data", "Data Analysis");
        if (suggestions.isEmpty()) {
            suggestions.addAll(List.of("Communication", "Problem Solving", "Teamwork", "Git", "REST APIs"));
        }
        suggestions.addAll(List.of("Problem Solving", "Communication"));
        return new ArrayList<>(suggestions);
    }

    @Override
    public String generateCoverLetter(String resumeJson, String jobJson) {
        JsonNode resume = parse(resumeJson);
        JsonNode job = parse(jobJson);
        String name = resume.path("fullName").asText("the candidate");
        String title = job.path("title").asText("the role");
        return """
                Dear Hiring Manager,

                I am excited to apply for the %s position. With a background that aligns closely with your
                requirements, I am confident I can make a strong contribution from day one.

                Throughout my career I have focused on delivering reliable, high-quality work while
                collaborating effectively across teams. I am particularly drawn to this opportunity because it
                matches both my experience and the direction I want to grow in.

                I would welcome the chance to discuss how my skills can support your goals. Thank you for your
                time and consideration.

                Sincerely,
                %s
                """.formatted(title, name);
    }

    @Override
    public AiResults.Screening screen(String resumeJson, String jobJson) {
        Set<String> resumeSkills = extractSkills(parse(resumeJson), "skills");
        Set<String> jobSkills = extractSkills(parse(jobJson), "skills");
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String js : jobSkills) {
            if (resumeSkills.stream().anyMatch(rs -> rs.equalsIgnoreCase(js))) {
                matched.add(js);
            } else {
                missing.add(js);
            }
        }
        int score = jobSkills.isEmpty() ? 70 : (int) Math.round(100.0 * matched.size() / jobSkills.size());
        String recommendation = score >= 75 ? "STRONG_MATCH" : score >= 50 ? "POSSIBLE_MATCH" : "WEAK_MATCH";
        String summary = "The candidate matches %d of %d required skills (%d%%). %s"
                .formatted(matched.size(), jobSkills.size(), score,
                        score >= 75 ? "Strong alignment with the role."
                                : score >= 50 ? "Partial alignment; worth a closer look."
                                : "Limited alignment with the core requirements.");
        return new AiResults.Screening(score, matched, missing, summary, recommendation);
    }

    @Override
    public AiResults.SearchFilters parseSearch(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        String workMode = q.contains("remote") ? "REMOTE" : q.contains("hybrid") ? "HYBRID"
                : q.contains("on-site") || q.contains("onsite") ? "ON_SITE" : null;
        String jobType = q.contains("part-time") || q.contains("part time") ? "PART_TIME"
                : q.contains("intern") ? "INTERNSHIP"
                : q.contains("contract") ? "CONTRACT" : q.contains("full-time") ? "FULL_TIME" : null;
        String experience = q.contains("senior") ? "SENIOR" : q.contains("junior") ? "JUNIOR"
                : q.contains("lead") ? "LEAD" : q.contains("entry") ? "ENTRY" : null;
        List<String> skills = new ArrayList<>();
        for (String s : List.of("java", "react", "python", "node", "aws", "spring", "kafka", "sql")) {
            if (q.contains(s)) skills.add(capitalize(s));
        }
        String location = null;
        for (String city : List.of("bangalore", "mumbai", "delhi", "remote", "london", "berlin", "pune", "hyderabad")) {
            if (q.contains(city)) {
                location = capitalize(city);
                break;
            }
        }
        BigDecimal salaryMin = null;
        var matcher = java.util.regex.Pattern.compile("(\\d{4,})").matcher(q.replace(",", ""));
        if (matcher.find()) {
            salaryMin = new BigDecimal(matcher.group(1));
        }
        String keywords = skills.isEmpty() ? query.trim() : String.join(" ", skills);
        return new AiResults.SearchFilters(keywords, location, jobType, workMode, experience, salaryMin, skills);
    }

    @Override
    public boolean isLive() {
        return false;
    }

    // ---- helpers ----

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json == null ? "{}" : json);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    private Set<String> extractSkills(JsonNode node, String field) {
        Set<String> result = new LinkedHashSet<>();
        JsonNode skills = node.path(field);
        if (skills.isArray()) {
            for (JsonNode s : skills) {
                if (s.isTextual()) {
                    result.add(s.asText());
                } else if (s.hasNonNull("name")) {
                    result.add(s.get("name").asText());
                }
            }
        }
        return result;
    }

    private void addIf(Set<String> set, String text, String needle, String skill) {
        if (text.contains(needle)) {
            set.add(skill);
        }
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String lowerFirst(String s) {
        return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
