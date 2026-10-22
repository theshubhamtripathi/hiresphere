package com.hiresphere.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresphere.ai.client.AiClient;
import com.hiresphere.ai.client.GeminiAiClient;
import com.hiresphere.ai.client.MockAiClient;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableJpaAuditing
@EnableConfigurationProperties(GeminiProperties.class)
public class AiConfig {

    /** A dedicated Jackson 2 mapper for AI JSON parsing (independent of the web mapper). */
    @Bean
    ObjectMapper aiObjectMapper() {
        return new ObjectMapper();
    }

    /** Selects the real Gemini client when a key is configured, otherwise the mock. */
    @Bean
    AiClient aiClient(GeminiProperties properties, ObjectMapper aiObjectMapper) {
        if (StringUtils.hasText(properties.apiKey())) {
            return new GeminiAiClient(properties, aiObjectMapper);
        }
        return new MockAiClient(aiObjectMapper);
    }

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    OpenAPI aiServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("HireSphere — AI Service API")
                .version("1.0.0")
                .description("Gemini-powered resume, job, screening, cover letter and search features."));
    }
}
