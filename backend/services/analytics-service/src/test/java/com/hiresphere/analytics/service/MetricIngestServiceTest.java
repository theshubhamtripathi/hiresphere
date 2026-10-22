package com.hiresphere.analytics.service;

import com.hiresphere.analytics.config.JpaConfig;
import com.hiresphere.analytics.domain.MetricCounter;
import com.hiresphere.analytics.repository.JobCounterRepository;
import com.hiresphere.analytics.repository.MetricCounterRepository;
import com.hiresphere.analytics.repository.RoleCounterRepository;
import com.hiresphere.analytics.repository.StatusCounterRepository;
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
class MetricIngestServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired MetricCounterRepository metricRepository;
    @Autowired StatusCounterRepository statusRepository;
    @Autowired RoleCounterRepository roleRepository;
    @Autowired JobCounterRepository jobRepository;

    private MetricIngestService service() {
        return new MetricIngestService(metricRepository, statusRepository, roleRepository, jobRepository);
    }

    @Test
    void accumulatesMetricsAndStatuses() {
        MetricIngestService service = service();
        service.addMetric(5L, MetricIngestService.APPLICATIONS, 1);
        service.addMetric(5L, MetricIngestService.APPLICATIONS, 2);
        assertThat(metricRepository.sum(5L, MetricIngestService.APPLICATIONS)).isEqualTo(3);

        service.adjustStatus(5L, "APPLIED", 2);
        service.adjustStatus(5L, "APPLIED", -1);
        assertThat(statusRepository.findByScopeIdAndStatus(5L, "APPLIED"))
                .get().extracting(c -> c.getValue()).isEqualTo(1L);

        service.incrementRole("JOB_SEEKER");
        service.incrementRole("JOB_SEEKER");
        assertThat(roleRepository.findByRole("JOB_SEEKER")).get().extracting(c -> c.getValue()).isEqualTo(2L);
    }

    @Test
    void tracksTopJobsByApplications() {
        MetricIngestService service = service();
        service.incrementJobApplications(10L, 5L, "Engineer");
        service.incrementJobApplications(10L, 5L, "Engineer");
        service.incrementJobApplications(11L, 5L, "Designer");

        var top = jobRepository.findTop5ByCompanyIdOrderByApplicationsDesc(5L);
        assertThat(top).hasSize(2);
        assertThat(top.getFirst().getJobId()).isEqualTo(10L);
        assertThat(top.getFirst().getApplications()).isEqualTo(2);
    }
}
