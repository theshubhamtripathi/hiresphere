package com.hiresphere.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code hiresphere.jwt.*}. */
@ConfigurationProperties(prefix = "hiresphere.jwt")
public record JwtProperties(
        String secret,
        long accessTtl,
        long refreshTtl,
        String issuer) {
}
