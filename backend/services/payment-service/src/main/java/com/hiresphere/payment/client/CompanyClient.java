package com.hiresphere.payment.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hiresphere.common.exception.ForbiddenException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CompanyClient {

    private final RestClient restClient;

    public CompanyClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://company-service").build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompanyInfo(Long id, String name) {
    }

    public Long companyIdForOwner(Long ownerUserId) {
        try {
            CompanyInfo info = restClient.get().uri("/api/internal/companies/by-owner/{id}", ownerUserId)
                    .retrieve().body(CompanyInfo.class);
            return info == null ? null : info.id();
        } catch (RestClientException ex) {
            throw new ForbiddenException("Create a company profile before subscribing to a plan");
        }
    }
}
