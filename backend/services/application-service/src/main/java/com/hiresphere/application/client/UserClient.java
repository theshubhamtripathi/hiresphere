package com.hiresphere.application.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://user-service").build();
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record UserInfo(Long id, String email, String fullName) {
    }

    /** Returns {@code null} if the user cannot be resolved (events degrade gracefully). */
    public UserInfo getById(Long userId) {
        try {
            return restClient.get()
                    .uri("/api/internal/users/{id}", userId)
                    .retrieve()
                    .body(UserInfo.class);
        } catch (RestClientException ex) {
            return null;
        }
    }
}
