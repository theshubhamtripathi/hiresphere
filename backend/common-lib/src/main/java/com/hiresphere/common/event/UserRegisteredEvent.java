package com.hiresphere.common.event;

import com.hiresphere.common.enums.Role;

import java.time.Instant;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String fullName,
        Role role,
        Instant occurredAt) {
}
