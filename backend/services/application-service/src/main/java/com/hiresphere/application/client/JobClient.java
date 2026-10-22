package com.hiresphere.application.client;

import com.hiresphere.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class JobClient {

    private final RestClient restClient;

    public JobClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://job-service").build();
    }

    public record JobInfo(Long id, String title, String status, Long companyId) {
    }

    public JobInfo getById(Long jobId) {
        try {
            return restClient.get()
                    .uri("/api/internal/jobs/{id}", jobId)
                    .retrieve()
                    .body(JobInfo.class);
        } catch (RestClientResponseException ex) {
            throw ResourceNotFoundException.of("Job", jobId);
        }
    }

    public void adjustApplicationsCount(Long jobId, long delta) {
        restClient.patch()
                .uri("/api/internal/jobs/{id}/applications-count?delta={delta}", jobId, delta)
                .retrieve()
                .toBodilessEntity();
    }
}
