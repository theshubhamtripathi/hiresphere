package com.hiresphere.notification.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Resolves user and company owner details for addressing notifications. */
@Component
public class DirectoryClient {

    private final RestClient userClient;
    private final RestClient companyClient;

    public DirectoryClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        this.userClient = loadBalancedRestClientBuilder.clone().baseUrl("http://user-service").build();
        this.companyClient = loadBalancedRestClientBuilder.clone().baseUrl("http://company-service").build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserInfo(Long id, String email, String fullName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompanyView(Long ownerUserId) {
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
            CompanyView view = companyClient.get().uri("/api/internal/companies/{id}", companyId)
                    .retrieve().body(CompanyView.class);
            return view == null ? null : view.ownerUserId();
        } catch (RestClientException ex) {
            return null;
        }
    }
}
