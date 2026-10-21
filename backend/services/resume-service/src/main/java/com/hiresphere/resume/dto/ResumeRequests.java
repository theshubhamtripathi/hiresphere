package com.hiresphere.resume.dto;

import com.hiresphere.resume.domain.LanguageProficiency;
import com.hiresphere.resume.domain.SkillLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Request payloads for the resume and its sections. */
public final class ResumeRequests {

    private ResumeRequests() {
    }

    public record CreateResume(
            @NotBlank @Size(max = 150) String title,
            @Size(max = 50) String template) {
    }

    public record PersonalInfo(
            @Size(max = 150) String fullName,
            @Email @Size(max = 150) String email,
            @Size(max = 30) String phone,
            @Size(max = 150) String location,
            @Size(max = 160) String headline,
            @Size(max = 255) String website,
            @Size(max = 255) String linkedin,
            @Size(max = 255) String github) {
    }

    public record Summary(@Size(max = 4000) String summary) {
    }

    public record Experience(
            @NotBlank String company,
            @NotBlank String title,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            boolean current,
            @Size(max = 4000) String description,
            Integer displayOrder) {
    }

    public record Education(
            @NotBlank String institution,
            String degree,
            String fieldOfStudy,
            LocalDate startDate,
            LocalDate endDate,
            String grade,
            @Size(max = 2000) String description,
            Integer displayOrder) {
    }

    public record Skill(
            @NotBlank String name,
            SkillLevel level,
            Integer displayOrder) {
    }

    public record Project(
            @NotBlank String name,
            @Size(max = 4000) String description,
            String url,
            LocalDate startDate,
            LocalDate endDate,
            Integer displayOrder) {
    }

    public record Certification(
            @NotBlank String name,
            String issuer,
            LocalDate issueDate,
            LocalDate expiryDate,
            String credentialId,
            String url,
            Integer displayOrder) {
    }

    public record Award(
            @NotBlank String title,
            String issuer,
            LocalDate date,
            @Size(max = 2000) String description,
            Integer displayOrder) {
    }

    public record Language(
            @NotBlank String name,
            LanguageProficiency proficiency,
            Integer displayOrder) {
    }
}
