package com.hiresphere.common.security;

import com.hiresphere.common.enums.Role;

/** The authenticated caller, as resolved from gateway headers. */
public record UserContext(Long userId, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isEmployer() {
        return role == Role.EMPLOYER;
    }

    public boolean isJobSeeker() {
        return role == Role.JOB_SEEKER;
    }
}
