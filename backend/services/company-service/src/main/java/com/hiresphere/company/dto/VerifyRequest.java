package com.hiresphere.company.dto;

import jakarta.validation.constraints.NotNull;

public record VerifyRequest(@NotNull Boolean verified) {
}
