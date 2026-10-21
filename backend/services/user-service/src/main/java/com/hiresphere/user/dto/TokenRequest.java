package com.hiresphere.user.dto;

import jakarta.validation.constraints.NotBlank;

/** Carries a refresh token for the refresh and logout endpoints. */
public record TokenRequest(@NotBlank String refreshToken) {
}
