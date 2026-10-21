package com.hiresphere.job.repository;

import com.hiresphere.common.enums.ExperienceLevel;
import com.hiresphere.common.enums.JobType;
import com.hiresphere.common.enums.WorkMode;
import com.hiresphere.job.config.JpaConfig;
import com.hiresphere.job.domain.Job;
import com.hiresphere.job.domain.JobStatus;
import com.hiresphere.job.domain.Skill;
import com.hiresphere.job.dto.JobSearchCriteria;
import com.hiresphere.job.service.JobSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobSearchTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired JobRepository jobRepository;
    @Autowired SkillRepository skillRepository;

    @BeforeEach
    void seed() {
        Skill java = new Skill();
        java.setName("Java");
        java = skillRepository.save(java);

        jobRepository.save(job("Senior Java Engineer", JobType.FULL_TIME, WorkMode.REMOTE, Set.of(java)));
        jobRepository.save(job("Part-time Writer", JobType.PART_TIME, WorkMode.ON_SITE, Set.of()));
    }

    private Job job(String title, JobType type, WorkMode mode, Set<Skill> skills) {
        Job job = new Job();
        job.setTitle(title);
        job.setDescription("desc of " + title);
        job.setCompanyId(1L);
        job.setPostedBy(1L);
        job.setCategoryId(1L);
        job.setJobType(type);
        job.setWorkMode(mode);
        job.setExperienceLevel(ExperienceLevel.MID);
        job.setStatus(JobStatus.OPEN);
        job.setSkills(new java.util.HashSet<>(skills));
        return job;
    }

    private JobSearchCriteria criteria(JobType type, java.util.List<String> skills) {
        return new JobSearchCriteria(null, null, null, type, null, null, null, null,
                skills, null, null, null, JobStatus.OPEN);
    }

    @Test
    void filtersByJobType() {
        var page = jobRepository.findAll(JobSpecifications.from(criteria(JobType.FULL_TIME, null)),
                PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(Job::getTitle).containsExactly("Senior Java Engineer");
    }

    @Test
    void filtersBySkill() {
        var page = jobRepository.findAll(JobSpecifications.from(criteria(null, java.util.List.of("java"))),
                PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Senior Java Engineer");
    }

    @Test
    void onlyOpenJobsMatchDefaultStatus() {
        var all = jobRepository.findAll(JobSpecifications.from(criteria(null, null)), PageRequest.of(0, 10));
        assertThat(all.getTotalElements()).isEqualTo(2);
    }
}
