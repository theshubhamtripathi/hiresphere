package com.hiresphere.preference.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class JobClient {

    private final RestClient restClient;

    public JobClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://job-service").build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JobSummary(Long id, String title, Long companyId, String location, String jobType,
                             String workMode, BigDecimal salaryMin, BigDecimal salaryMax, String currency,
                             String status) {
    }

    public List<JobSummary> batch(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String csv = ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        try {
            return restClient.get()
                    .uri("/api/internal/jobs?ids={ids}", csv)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException ex) {
            return List.of();
        }
    }
}
