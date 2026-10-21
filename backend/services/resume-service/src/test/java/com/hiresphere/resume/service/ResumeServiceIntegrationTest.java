package com.hiresphere.resume.service;

import com.hiresphere.resume.config.JpaConfig;
import com.hiresphere.resume.dto.ResumeRequests;
import com.hiresphere.resume.dto.ResumeResponses;
import com.hiresphere.resume.mapper.ResumeMapper;
import com.hiresphere.resume.repository.ResumeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ResumeServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    ResumeRepository repository;

    private ResumeService service() {
        return new ResumeService(repository, new ResumeMapper());
    }

    @Test
    void firstResumeIsDefaultAndScoresZero() {
        var detail = service().create(1L, new ResumeRequests.CreateResume("My CV", null));
        assertThat(detail.isDefault()).isTrue();
        assertThat(detail.completionScore()).isZero();
    }

    @Test
    void addingSectionsRecalculatesScore() {
        ResumeService service = service();
        var created = service.create(1L, new ResumeRequests.CreateResume("My CV", null));

        service.updatePersonalInfo(created.id(), 1L,
                new ResumeRequests.PersonalInfo("Alice", "a@x.com", "123", null, null, null, null, null));
        var afterExp = service.addExperience(created.id(), 1L,
                new ResumeRequests.Experience("Acme", "Engineer", null, null, null, true, "Built things", 0));

        assertThat(afterExp.completionScore()).isEqualTo(20 + 25);
        assertThat(afterExp.experiences()).hasSize(1);
    }

    @Test
    void secondResumeIsNotDefault() {
        ResumeService service = service();
        service.create(2L, new ResumeRequests.CreateResume("First", null));
        var second = service.create(2L, new ResumeRequests.CreateResume("Second", null));
        assertThat(second.isDefault()).isFalse();
    }
}
