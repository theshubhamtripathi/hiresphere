package com.hiresphere.application.client;

import com.hiresphere.common.exception.ForbiddenException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CompanyClient {

    private final RestClient restClient;

    public CompanyClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://company-service").build();
    }

    public record CompanyInfo(Long id, String name) {
    }

    public CompanyInfo getByOwner(Long ownerUserId) {
        try {
            return restClient.get()
                    .uri("/api/internal/companies/by-owner/{id}", ownerUserId)
                    .retrieve()
                    .body(CompanyInfo.class);
        } catch (RestClientException ex) {
            throw new ForbiddenException("You must own a company to manage applications");
        }
    }
}
