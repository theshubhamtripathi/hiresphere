package com.hiresphere.company.repository;

import com.hiresphere.company.config.JpaConfig;
import com.hiresphere.company.domain.Company;
import com.hiresphere.company.domain.CompanyStatus;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CompanyRepository repository;

    private Company company(String name, Long owner) {
        Company c = new Company();
        c.setName(name);
        c.setSlug(name.toLowerCase().replace(' ', '-'));
        c.setOwnerUserId(owner);
        c.setStatus(CompanyStatus.ACTIVE);
        c.setSocialLinks(Map.of("linkedin", "https://linkedin.com/" + owner));
        return c;
    }

    @Test
    void persistsJsonSocialLinksAndFindsBySlug() {
        repository.save(company("Acme Corp", 1L));

        var found = repository.findBySlug("acme-corp");
        assertThat(found).isPresent();
        assertThat(found.get().getSocialLinks()).containsEntry("linkedin", "https://linkedin.com/1");
        assertThat(repository.existsByOwnerUserId(1L)).isTrue();
    }

    @Test
    void searchFiltersByVerifiedAndQuery() {
        Company verified = company("Globex", 2L);
        verified.setVerified(true);
        repository.save(verified);
        repository.save(company("Initech", 3L));

        var verifiedOnly = repository.search(null, true, "%", PageRequest.of(0, 10));
        assertThat(verifiedOnly.getContent()).extracting(Company::getName).containsExactly("Globex");

        var byName = repository.search(null, null, "%init%", PageRequest.of(0, 10));
        assertThat(byName.getContent()).extracting(Company::getName).containsExactly("Initech");
    }
}
