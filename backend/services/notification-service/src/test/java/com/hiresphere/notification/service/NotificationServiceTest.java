package com.hiresphere.notification.service;

import com.hiresphere.notification.config.JpaConfig;
import com.hiresphere.notification.domain.NotificationType;
import com.hiresphere.notification.repository.NotificationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    NotificationRepository repository;

    private NotificationService service() {
        return new NotificationService(repository);
    }

    @Test
    void notifyIsIdempotentOnEventKey() {
        NotificationService service = service();
        assertThat(service.notify("evt-1", 1L, NotificationType.WELCOME, "Hi", "msg", "/")).isPresent();
        assertThat(service.notify("evt-1", 1L, NotificationType.WELCOME, "Hi", "msg", "/")).isEmpty();
        assertThat(service.unreadCount(1L)).isEqualTo(1);
    }

    @Test
    void markAllReadClearsUnread() {
        NotificationService service = service();
        service.notify("a", 2L, NotificationType.JOB_ALERT, "t", "m", "/");
        service.notify("b", 2L, NotificationType.JOB_ALERT, "t", "m", "/");
        assertThat(service.unreadCount(2L)).isEqualTo(2);

        service.markAllRead(2L);
        assertThat(service.unreadCount(2L)).isZero();
        assertThat(service.list(2L, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
    }
}
