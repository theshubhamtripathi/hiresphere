package com.hiresphere.application.client;

import com.hiresphere.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ResumeClient {

    private final RestClient restClient;

    public ResumeClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://resume-service").build();
    }

    /** Fetches the full resume as a raw JSON string for snapshotting. */
    public String getSnapshot(Long resumeId) {
        try {
            return restClient.get()
                    .uri("/api/internal/resumes/{id}", resumeId)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new BadRequestException("Selected resume could not be loaded");
        }
    }
}
