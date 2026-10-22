package com.hiresphere.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hiresphere.ai.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        long timeoutMs) {
}
