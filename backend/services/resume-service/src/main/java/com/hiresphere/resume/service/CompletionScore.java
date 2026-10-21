package com.hiresphere.resume.service;

import com.hiresphere.resume.domain.Resume;
import org.springframework.util.StringUtils;

/**
 * Completion score (0-100). Weights: personal 20, summary 10, experience 25,
 * education 15, skills 15, projects 10, extras 5.
 */
final class CompletionScore {

    private CompletionScore() {
    }

    static int calculate(Resume r) {
        int score = 0;
        if (hasPersonalInfo(r)) {
            score += 20;
        }
        if (StringUtils.hasText(r.getSummary())) {
            score += 10;
        }
        if (!r.getExperiences().isEmpty()) {
            score += 25;
        }
        if (!r.getEducation().isEmpty()) {
            score += 15;
        }
        if (!r.getSkills().isEmpty()) {
            score += 15;
        }
        if (!r.getProjects().isEmpty()) {
            score += 10;
        }
        if (!r.getCertifications().isEmpty() || !r.getAwards().isEmpty() || !r.getLanguages().isEmpty()) {
            score += 5;
        }
        return score;
    }

    private static boolean hasPersonalInfo(Resume r) {
        return StringUtils.hasText(r.getFullName())
                && StringUtils.hasText(r.getEmail())
                && (StringUtils.hasText(r.getPhone()) || StringUtils.hasText(r.getLocation()));
    }
}
