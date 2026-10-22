package com.hiresphere.preference.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://user-service").build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserInfo(Long id, String email, String fullName) {
    }

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
