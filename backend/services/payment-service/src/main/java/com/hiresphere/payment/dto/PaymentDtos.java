package com.hiresphere.payment.dto;

import com.hiresphere.payment.domain.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record PlanResponse(String code, String name, BigDecimal priceInr, int jobCredits,
                               int featuredListings, String description) {
    }

    public record CreateOrderRequest(@NotBlank String planCode) {
    }

    public record OrderResponse(String razorpayOrderId, BigDecimal amount, String currency,
                                String keyId, String planCode) {
    }

    public record VerifyRequest(
            @NotBlank String razorpayOrderId,
            @NotBlank String razorpayPaymentId,
            @NotBlank String razorpaySignature) {
    }

    public record SubscriptionResponse(Long companyId, String planCode, SubscriptionStatus status,
                                       Instant validUntil, int creditsLeft) {
    }

    public record CreditCheck(boolean allowed, int creditsLeft) {
    }
}
