package com.hiresphere.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class PathRulesTest {

    private final PathRules rules = new PathRules();

    @Test
    void authEndpointsArePublic() {
        assertThat(rules.isPublic("/api/auth/login", HttpMethod.POST)).isTrue();
    }

    @Test
    void jobBrowsingIsPublicForGetOnly() {
        assertThat(rules.isPublic("/api/jobs", HttpMethod.GET)).isTrue();
        assertThat(rules.isPublic("/api/jobs/5", HttpMethod.GET)).isTrue();
        assertThat(rules.isPublic("/api/jobs", HttpMethod.POST)).isFalse();
    }

    @Test
    void adminMetadataIsNotPublic() {
        assertThat(rules.isPublic("/api/admin/skills", HttpMethod.GET)).isFalse();
        assertThat(rules.isAdminPath("/api/admin/users/1/status")).isTrue();
    }

    @Test
    void protectedEndpointsAreNotPublic() {
        assertThat(rules.isPublic("/api/resumes", HttpMethod.GET)).isFalse();
        assertThat(rules.isPublic("/api/applications", HttpMethod.POST)).isFalse();
    }
}
