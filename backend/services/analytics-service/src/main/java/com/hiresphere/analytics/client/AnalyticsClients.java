package com.hiresphere.analytics.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hiresphere.common.exception.ForbiddenException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AnalyticsClients {

    private final RestClient companyClient;
    private final RestClient jobClient;

    public AnalyticsClients(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        this.companyClient = loadBalancedRestClientBuilder.clone().baseUrl("http://company-service").build();
        this.jobClient = loadBalancedRestClientBuilder.clone().baseUrl("http://job-service").build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CompanyInfo(Long id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Page(long totalElements) {
    }

    public Long companyIdForOwner(Long ownerUserId) {
        try {
            CompanyInfo info = companyClient.get().uri("/api/internal/companies/by-owner/{id}", ownerUserId)
                    .retrieve().body(CompanyInfo.class);
            return info == null ? null : info.id();
        } catch (RestClientException ex) {
            throw new ForbiddenException("You must own a company to view employer analytics");
        }
    }

    public long activeJobCount(Long companyId) {
        try {
            Long count = jobClient.get().uri("/api/internal/jobs/company/{id}/active-count", companyId)
                    .retrieve().body(Long.class);
            return count == null ? 0 : count;
        } catch (RestClientException ex) {
            return 0;
        }
    }

    public long totalCompanies() {
        try {
            Page page = companyClient.get().uri("/api/companies?size=1")
                    .retrieve().body(Page.class);
            return page == null ? 0 : page.totalElements();
        } catch (RestClientException ex) {
            return 0;
        }
    }
}
