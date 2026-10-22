package com.hiresphere.job.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Checks and consumes a company's job-posting credit via payment-service. */
@Slf4j
@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://payment-service").build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreditCheck(boolean allowed, int creditsLeft) {
    }

    /**
     * Attempts to consume a posting credit. Returns true when allowed. If
     * payment-service is unreachable, posting is allowed (graceful degradation).
     */
    public boolean consumeJobCredit(Long companyId) {
        try {
            CreditCheck result = restClient.post()
                    .uri("/api/internal/subscriptions/company/{id}/consume-job-credit", companyId)
                    .retrieve()
                    .body(CreditCheck.class);
            return result == null || result.allowed();
        } catch (RestClientException ex) {
            log.warn("Credit check unavailable for company {}: {}", companyId, ex.getMessage());
            return true;
        }
    }
}
