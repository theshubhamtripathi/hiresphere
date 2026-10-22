package com.hiresphere.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.hiresphere.ai.dto.AiRequests;
import com.hiresphere.ai.dto.AiResults;
import com.hiresphere.common.exception.BadRequestException;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ServiceClients {

    private final RestClient resumeClient;
    private final RestClient jobClient;
    private final RestClient applicationClient;
    private final RestClient companyClient;

    public ServiceClients(RestClient.Builder loadBalancedRestClientBuilder) {
        this.resumeClient = loadBalancedRestClientBuilder.clone().baseUrl("http://resume-service").build();
        this.jobClient = loadBalancedRestClientBuilder.clone().baseUrl("http://job-service").build();
        this.applicationClient = loadBalancedRestClientBuilder.clone().baseUrl("http://application-service").build();
        this.companyClient = loadBalancedRestClientBuilder.clone().baseUrl("http://company-service").build();
    }

    public record ApplicationView(Long id, Long jobId, Long companyId, Long applicantId, String resumeSnapshot) {
    }

    public String resumeSnapshot(Long resumeId) {
        try {
            return resumeClient.get().uri("/api/internal/resumes/{id}", resumeId)
                    .retrieve().body(String.class);
        } catch (RestClientException ex) {
            throw new BadRequestException("Resume could not be loaded");
        }
    }

    public String jobJson(Long jobId) {
        try {
            return jobClient.get().uri("/api/internal/jobs/{id}", jobId)
                    .retrieve().body(String.class);
        } catch (RestClientException ex) {
            throw ResourceNotFoundException.of("Job", jobId);
        }
    }

    public ApplicationView application(Long applicationId) {
        try {
            return applicationClient.get().uri("/api/internal/applications/{id}", applicationId)
                    .retrieve().body(ApplicationView.class);
        } catch (RestClientException ex) {
            throw ResourceNotFoundException.of("Application", applicationId);
        }
    }

    public Long companyIdForOwner(Long ownerUserId) {
        try {
            JsonNode node = companyClient.get().uri("/api/internal/companies/by-owner/{id}", ownerUserId)
                    .retrieve().body(JsonNode.class);
            return node == null ? null : node.path("id").asLong();
        } catch (RestClientException ex) {
            throw new ForbiddenException("You must own a company to screen candidates");
        }
    }

    public void saveScreening(Long applicationId, AiResults.Screening screening) {
        var payload = new AiRequests.ScreeningPersist(screening.matchScore(), screening.matchedSkills(),
                screening.missingSkills(), screening.summary(), screening.recommendation());
        applicationClient.patch().uri("/api/internal/applications/{id}/ai-result", applicationId)
                .body(payload).retrieve().toBodilessEntity();
    }

    public JsonNode searchJobs(AiResults.SearchFilters filters) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/api/jobs").queryParam("size", 20);
        if (filters.keywords() != null && !filters.keywords().isBlank()) uri.queryParam("q", filters.keywords());
        if (filters.location() != null) uri.queryParam("location", filters.location());
        if (filters.jobType() != null) uri.queryParam("jobType", filters.jobType());
        if (filters.workMode() != null) uri.queryParam("workMode", filters.workMode());
        if (filters.experienceLevel() != null) uri.queryParam("experienceLevel", filters.experienceLevel());
        if (filters.salaryMin() != null) uri.queryParam("salaryMin", filters.salaryMin());
        if (filters.skills() != null && !filters.skills().isEmpty()) uri.queryParam("skills", filters.skills().toArray());
        try {
            return jobClient.get().uri(uri.build().toUriString()).retrieve().body(JsonNode.class);
        } catch (RestClientException ex) {
            return null;
        }
    }
}
