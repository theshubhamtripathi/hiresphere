package com.hiresphere.payment.service;

import com.hiresphere.common.exception.BadRequestException;
import com.hiresphere.payment.config.RazorpayProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

/** Thin client over the Razorpay Orders REST API. */
@Component
public class RazorpayClient {

    private final RazorpayProperties properties;
    private final RestClient restClient;

    public RazorpayClient(RazorpayProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl("https://api.razorpay.com/v1").build();
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    /** Creates a Razorpay order and returns its id. Amount is in the major currency unit. */
    public String createOrder(BigDecimal amount, String currency, String receipt) {
        if (!properties.isConfigured()) {
            throw new PaymentsNotConfiguredException();
        }
        long amountInSubunits = amount.movePointRight(2).longValueExact();
        String auth = Base64.getEncoder().encodeToString(
                (properties.keyId() + ":" + properties.keySecret()).getBytes());
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/orders")
                    .header("Authorization", "Basic " + auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("amount", amountInSubunits, "currency", currency, "receipt", receipt))
                    .retrieve()
                    .body(Map.class);
            if (response == null || response.get("id") == null) {
                throw new BadRequestException("Failed to create payment order");
            }
            return String.valueOf(response.get("id"));
        } catch (PaymentsNotConfiguredException e) {
            throw e;
        } catch (RuntimeException ex) {
            throw new BadRequestException("Payment provider error: " + ex.getMessage());
        }
    }
}
