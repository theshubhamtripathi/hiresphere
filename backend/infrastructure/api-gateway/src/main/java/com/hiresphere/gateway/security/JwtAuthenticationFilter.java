package com.hiresphere.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * Validates the JWT at the edge, strips any client-supplied identity headers,
 * and propagates the authenticated user via X-User-* headers. Enforces that
 * admin paths require the ADMIN role.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String USER_ID = "X-User-Id";
    private static final String USER_EMAIL = "X-User-Email";
    private static final String USER_ROLE = "X-User-Role";

    private final SecretKey key;
    private final String issuer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PathRules pathRules = new PathRules();

    public JwtAuthenticationFilter(
            @org.springframework.beans.factory.annotation.Value("${hiresphere.jwt.secret}") String secret,
            @org.springframework.beans.factory.annotation.Value("${hiresphere.jwt.issuer:hiresphere}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // Internal endpoints must never be reachable through the gateway.
        if (path.startsWith("/api/internal")) {
            return deny(exchange, HttpStatus.NOT_FOUND, "Not found");
        }

        // Strip any client-supplied identity headers.
        ServerHttpRequest.Builder mutated = request.mutate()
                .headers(h -> {
                    h.remove(USER_ID);
                    h.remove(USER_EMAIL);
                    h.remove(USER_ROLE);
                });

        Claims claims = parseToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

        if (claims != null) {
            String role = claims.get("role", String.class);
            if (pathRules.isAdminPath(path) && !"ADMIN".equals(role)) {
                return deny(exchange, HttpStatus.FORBIDDEN, "Administrator access required");
            }
            mutated.header(USER_ID, claims.getSubject())
                    .header(USER_EMAIL, claims.get("email", String.class))
                    .header(USER_ROLE, role);
            return chain.filter(exchange.mutate().request(mutated.build()).build());
        }

        // No valid token.
        if (pathRules.isPublic(path, method)) {
            return chain.filter(exchange.mutate().request(mutated.build()).build());
        }
        return deny(exchange, HttpStatus.UNAUTHORIZED, "Authentication is required");
    }

    private Claims parseToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(authHeader.substring(7))
                    .getPayload();
        } catch (Exception ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return null;
        }
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", Instant.now().toString());
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"status\":" + status.value() + "}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // run before routing
    }
}
