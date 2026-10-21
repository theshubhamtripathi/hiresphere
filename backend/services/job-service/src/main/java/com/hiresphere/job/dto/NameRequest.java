package com.hiresphere.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create request for a skill or tag. */
public record NameRequest(@NotBlank @Size(max = 80) String name) {
}
