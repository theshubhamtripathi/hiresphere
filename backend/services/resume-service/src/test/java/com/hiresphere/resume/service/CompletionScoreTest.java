package com.hiresphere.resume.service;

import com.hiresphere.resume.domain.Resume;
import com.hiresphere.resume.domain.WorkExperience;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompletionScoreTest {

    @Test
    void emptyResumeScoresZero() {
        assertThat(CompletionScore.calculate(new Resume())).isZero();
    }

    @Test
    void personalInfoContributesTwenty() {
        Resume r = new Resume();
        r.setFullName("Alice");
        r.setEmail("a@x.com");
        r.setLocation("Remote");
        assertThat(CompletionScore.calculate(r)).isEqualTo(20);
    }

    @Test
    void summaryAndExperienceAddUp() {
        Resume r = new Resume();
        r.setSummary("Experienced engineer");
        r.getExperiences().add(new WorkExperience());
        assertThat(CompletionScore.calculate(r)).isEqualTo(10 + 25);
    }

    @Test
    void fullyCompletedResumeScoresHundred() {
        Resume r = new Resume();
        r.setFullName("Alice");
        r.setEmail("a@x.com");
        r.setPhone("123");
        r.setSummary("Summary");
        r.getExperiences().add(new WorkExperience());
        r.getEducation().add(new com.hiresphere.resume.domain.Education());
        r.getSkills().add(new com.hiresphere.resume.domain.ResumeSkill());
        r.getProjects().add(new com.hiresphere.resume.domain.Project());
        r.getLanguages().add(new com.hiresphere.resume.domain.Language());
        assertThat(CompletionScore.calculate(r)).isEqualTo(100);
    }
}
