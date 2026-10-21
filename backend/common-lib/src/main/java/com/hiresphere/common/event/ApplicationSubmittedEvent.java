package com.hiresphere.common.event;

import java.time.Instant;

public record ApplicationSubmittedEvent(
        Long applicationId,
        Long jobId,
        String jobTitle,
        Long applicantId,
        String applicantEmail,
        String applicantName,
        Long companyId,
        Instant occurredAt) {
}
