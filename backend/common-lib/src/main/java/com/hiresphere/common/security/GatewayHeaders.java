package com.hiresphere.common.security;

/** Header names the gateway sets after validating the JWT. */
public final class GatewayHeaders {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLE = "X-User-Role";

    private GatewayHeaders() {
    }
}
