package com.hiresphere.user.dto;

public record UserStatsResponse(
        long total,
        long jobSeekers,
        long employers,
        long admins) {
}
