package com.hiresphere.preference.repository;

import com.hiresphere.preference.config.JpaConfig;
import com.hiresphere.preference.domain.JobPreference;
import com.hiresphere.preference.domain.SavedJob;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SavedJobRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired SavedJobRepository savedJobRepository;
    @Autowired JobPreferenceRepository preferenceRepository;

    @Test
    void savesAndChecksSavedJob() {
        SavedJob s = new SavedJob();
        s.setUserId(1L);
        s.setJobId(10L);
        savedJobRepository.save(s);

        assertThat(savedJobRepository.existsByUserIdAndJobId(1L, 10L)).isTrue();
        assertThat(savedJobRepository.findByUserIdOrderByCreatedAtDesc(1L)).hasSize(1);
    }

    @Test
    void persistsPreferenceJsonbListsAndFindsEnabled() {
        JobPreference p = new JobPreference();
        p.setUserId(2L);
        p.setDesiredTitles(List.of("Java", "Kotlin"));
        p.setCategoryIds(List.of(1L, 2L));
        p.setAlertsEnabled(true);
        preferenceRepository.save(p);

        assertThat(preferenceRepository.findByUserId(2L))
                .get().extracting(JobPreference::getDesiredTitles)
                .isEqualTo(List.of("Java", "Kotlin"));
        assertThat(preferenceRepository.findByAlertsEnabledTrue()).hasSize(1);
    }
}
