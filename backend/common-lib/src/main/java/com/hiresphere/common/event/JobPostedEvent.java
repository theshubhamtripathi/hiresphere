package com.hiresphere.common.event;

import java.time.Instant;

public record JobPostedEvent(
        Long jobId,
        Long companyId,
        String title,
        Long categoryId,
        String location,
        Instant occurredAt) {
}
