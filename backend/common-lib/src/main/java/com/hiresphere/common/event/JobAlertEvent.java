package com.hiresphere.common.event;

import java.time.Instant;

public record JobAlertEvent(
        Long userId,
        String email,
        Long jobId,
        String jobTitle,
        String companyName,
        Instant occurredAt) {
}
