package com.hiresphere.job.client;

import com.hiresphere.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Resolves the caller's company from company-service via the service registry. */
@Component
public class CompanyClient {

    private final RestClient restClient;

    public CompanyClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder
                .baseUrl("http://company-service")
                .build();
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(Long id, String name, String logoUrl, String status) {
    }

    public Company getByOwner(Long ownerUserId) {
        try {
            return restClient.get()
                    .uri("/api/internal/companies/by-owner/{id}", ownerUserId)
                    .retrieve()
                    .body(Company.class);
        } catch (RestClientException ex) {
            throw new BadRequestException("Create a company profile before posting jobs");
        }
    }

    public Company getById(Long companyId) {
        return restClient.get()
                .uri("/api/internal/companies/{id}", companyId)
                .retrieve()
                .body(Company.class);
    }
}
