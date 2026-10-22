package com.hiresphere.notification.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Resolves user and company owner details for addressing notifications. */
@Component
public class DirectoryClient {

    private final RestClient userClient;
    private final RestClient companyClient;

    public DirectoryClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.userClient = loadBalancedRestClientBuilder.clone().baseUrl("http://user-service").build();
        this.companyClient = loadBalancedRestClientBuilder.clone().baseUrl("http://company-service").build();
    }

    public record UserInfo(Long id, String email, String fullName) {
    }

    public UserInfo user(Long userId) {
        try {
            return userClient.get().uri("/api/internal/users/{id}", userId)
                    .retrieve().body(UserInfo.class);
        } catch (RestClientException ex) {
            return null;
        }
    }

    /** The owner user id of a company, or null if unavailable. */
    public Long companyOwnerUserId(Long companyId) {
        try {
            JsonNode node = companyClient.get().uri("/api/internal/companies/{id}", companyId)
                    .retrieve().body(JsonNode.class);
            return node == null || node.path("ownerUserId").isMissingNode()
                    ? null : node.path("ownerUserId").asLong();
        } catch (RestClientException ex) {
            return null;
        }
    }
}
