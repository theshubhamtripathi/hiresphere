package com.hiresphere.user.config;

import com.hiresphere.common.enums.Role;
import com.hiresphere.user.domain.User;
import com.hiresphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Idempotently seeds the administrator and two demo accounts so a fresh
 * database is immediately usable. Disable with {@code hiresphere.seed.enabled=false}.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hiresphere.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hiresphere.admin.email:admin@hiresphere.local}")
    private String adminEmail;

    @Value("${hiresphere.admin.password:Admin@12345}")
    private String adminPassword;

    @Bean
    ApplicationRunner seedUsers() {
        return args -> {
            seed(adminEmail, adminPassword, "Platform Admin", Role.ADMIN);
            seed("employer@hiresphere.local", "Employer@123", "Demo Employer", Role.EMPLOYER);
            seed("seeker@hiresphere.local", "Seeker@123", "Demo Seeker", Role.JOB_SEEKER);
        };
    }

    private void seed(String email, String rawPassword, String fullName, Role role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User user = new User();
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Seeded {} account: {}", role, email);
    }
}
