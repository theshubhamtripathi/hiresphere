package com.hiresphere.gateway.security;

import org.springframework.http.HttpMethod;

import java.util.List;

/** Encodes which paths are publicly accessible without authentication. */
class PathRules {

    // Always public, any method.
    private static final List<String> OPEN_PREFIXES = List.of(
            "/api/auth/",
            "/actuator/health",
            "/fallback/",
            "/swagger-ui",
            "/v3/api-docs"
    );

    // Public for GET only (anonymous browsing).
    private static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/api/jobs",
            "/api/companies",
            "/api/job-categories",
            "/api/skills",
            "/api/tags",
            "/api/plans"
    );

    boolean isPublic(String path, HttpMethod method) {
        for (String prefix : OPEN_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        if (HttpMethod.GET.equals(method) || HttpMethod.OPTIONS.equals(method)) {
            for (String prefix : PUBLIC_GET_PREFIXES) {
                if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                    // Admin metadata management is not public even though it shares a prefix.
                    if (path.startsWith("/api/admin")) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    boolean isAdminPath(String path) {
        return path.startsWith("/api/admin/");
    }
}
