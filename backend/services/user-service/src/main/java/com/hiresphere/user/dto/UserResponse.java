package com.hiresphere.user.dto;

import com.hiresphere.common.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        Role role,
        String avatarUrl,
        String headline,
        String location,
        boolean enabled,
        Instant lastLoginAt,
        Instant createdAt) {
}
