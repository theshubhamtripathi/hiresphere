package com.hiresphere.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class GatewayConfig {

    /** Rate-limit per authenticated user, falling back to the client IP. */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            InetSocketAddress address = exchange.getRequest().getRemoteAddress();
            String ip = address == null ? "unknown" : address.getAddress().getHostAddress();
            return Mono.just("ip:" + ip);
        };
    }
}
