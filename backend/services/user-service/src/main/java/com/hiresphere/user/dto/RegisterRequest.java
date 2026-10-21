package com.hiresphere.user.dto;

import com.hiresphere.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password,
        @NotBlank @Size(max = 120) String fullName,
        @NotNull Role role) {
}
