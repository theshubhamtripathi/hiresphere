package com.hiresphere.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        Long orderId,
        String razorpayOrderId,
        Long companyId,
        String ownerEmail,
        String planCode,
        BigDecimal amount,
        String currency,
        Instant occurredAt) {
}
