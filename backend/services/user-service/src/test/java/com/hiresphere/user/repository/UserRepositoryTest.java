package com.hiresphere.user.repository;

import com.hiresphere.common.enums.Role;
import com.hiresphere.user.config.JpaConfig;
import com.hiresphere.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
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
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // The slice does not run Flyway; let Hibernate build the schema from the
        // entities. Flyway/entity drift is caught when the full service boots.
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    private User newUser(String email, String name, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setFullName(name);
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }

    @Test
    void savesAndPopulatesAuditFields() {
        User saved = userRepository.save(newUser("a@x.com", "Alice", Role.JOB_SEEKER));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getVersion()).isNotNull();
    }

    @Test
    void findsByEmailCaseInsensitively() {
        userRepository.save(newUser("Bob@X.com", "Bob", Role.EMPLOYER));

        assertThat(userRepository.findByEmailIgnoreCase("bob@x.com")).isPresent();
        assertThat(userRepository.existsByEmailIgnoreCase("BOB@X.COM")).isTrue();
    }

    @Test
    void searchFiltersByRoleAndQuery() {
        userRepository.save(newUser("seeker1@x.com", "Carol Seeker", Role.JOB_SEEKER));
        userRepository.save(newUser("emp1@x.com", "Dave Employer", Role.EMPLOYER));

        Page<User> seekers = userRepository.search(Role.JOB_SEEKER, null, "%", PageRequest.of(0, 10));
        assertThat(seekers.getContent()).extracting(User::getRole).containsOnly(Role.JOB_SEEKER);

        Page<User> byName = userRepository.search(null, null, "%dave%", PageRequest.of(0, 10));
        assertThat(byName.getContent()).extracting(User::getEmail).containsExactly("emp1@x.com");

        assertThat(userRepository.countByRole(Role.EMPLOYER)).isEqualTo(1);
    }
}
