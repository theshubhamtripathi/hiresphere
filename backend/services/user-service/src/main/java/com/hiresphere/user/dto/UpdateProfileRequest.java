package com.hiresphere.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @Size(max = 512) String avatarUrl,
        @Size(max = 160) String headline,
        @Size(max = 160) String location) {
}
