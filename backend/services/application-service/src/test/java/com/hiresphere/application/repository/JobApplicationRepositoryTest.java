package com.hiresphere.application.repository;

import com.hiresphere.application.config.JpaConfig;
import com.hiresphere.application.domain.JobApplication;
import com.hiresphere.common.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobApplicationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    JobApplicationRepository repository;

    private JobApplication application(Long jobId, Long applicantId) {
        JobApplication a = new JobApplication();
        a.setJobId(jobId);
        a.setApplicantId(applicantId);
        a.setCompanyId(3L);
        a.setResumeSnapshot("{\"title\":\"My CV\"}");
        a.setAnswers(Map.of("relocate", "yes"));
        a.setStatus(ApplicationStatus.APPLIED);
        return a;
    }

    @Test
    void persistsJsonbColumnsAndEnforcesUniqueApplication() {
        repository.saveAndFlush(application(10L, 1L));

        assertThat(repository.existsByJobIdAndApplicantId(10L, 1L)).isTrue();
        assertThat(repository.findByJobIdAndApplicantId(10L, 1L))
                .get().extracting(JobApplication::getAnswers)
                .isEqualTo(Map.of("relocate", "yes"));

        assertThatThrownBy(() -> repository.saveAndFlush(application(10L, 1L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
