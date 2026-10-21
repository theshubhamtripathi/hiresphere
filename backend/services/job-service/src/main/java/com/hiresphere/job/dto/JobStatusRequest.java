package com.hiresphere.job.dto;

import com.hiresphere.job.domain.JobStatus;
import jakarta.validation.constraints.NotNull;

public record JobStatusRequest(@NotNull JobStatus status) {
}
