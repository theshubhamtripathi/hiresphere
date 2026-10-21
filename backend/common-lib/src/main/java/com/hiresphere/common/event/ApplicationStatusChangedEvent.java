package com.hiresphere.common.event;

import com.hiresphere.common.enums.ApplicationStatus;

import java.time.Instant;

public record ApplicationStatusChangedEvent(
        Long applicationId,
        Long jobId,
        String jobTitle,
        Long applicantId,
        String applicantEmail,
        String applicantName,
        Long companyId,
        ApplicationStatus oldStatus,
        ApplicationStatus newStatus,
        Instant occurredAt) {
}
