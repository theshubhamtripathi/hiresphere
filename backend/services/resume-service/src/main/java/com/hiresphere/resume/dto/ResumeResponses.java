package com.hiresphere.resume.dto;

import com.hiresphere.resume.domain.LanguageProficiency;
import com.hiresphere.resume.domain.SkillLevel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Response payloads for the resume and its sections. */
public final class ResumeResponses {

    private ResumeResponses() {
    }

    public record ResumeSummary(
            Long id,
            String title,
            boolean isDefault,
            String template,
            int completionScore,
            Instant updatedAt) {
    }

    public record ResumeDetail(
            Long id,
            String title,
            boolean isDefault,
            String template,
            int completionScore,
            String fullName,
            String email,
            String phone,
            String location,
            String headline,
            String website,
            String linkedin,
            String github,
            String summary,
            List<Experience> experiences,
            List<Education> education,
            List<Skill> skills,
            List<Project> projects,
            List<Certification> certifications,
            List<Award> awards,
            List<Language> languages,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record Experience(Long id, String company, String title, String location,
                             LocalDate startDate, LocalDate endDate, boolean current,
                             String description, int displayOrder) {
    }

    public record Education(Long id, String institution, String degree, String fieldOfStudy,
                            LocalDate startDate, LocalDate endDate, String grade,
                            String description, int displayOrder) {
    }

    public record Skill(Long id, String name, SkillLevel level, int displayOrder) {
    }

    public record Project(Long id, String name, String description, String url,
                          LocalDate startDate, LocalDate endDate, int displayOrder) {
    }

    public record Certification(Long id, String name, String issuer, LocalDate issueDate,
                                LocalDate expiryDate, String credentialId, String url, int displayOrder) {
    }

    public record Award(Long id, String title, String issuer, LocalDate date,
                        String description, int displayOrder) {
    }

    public record Language(Long id, String name, LanguageProficiency proficiency, int displayOrder) {
    }
}
