package com.hiresphere.analytics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String DASHBOARDS = "dashboards";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default (JDK) value serialization; dashboard DTOs implement Serializable.
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues();
        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(DASHBOARDS, config)
                .build();
    }

    @Bean
    OpenAPI analyticsServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("HireSphere — Analytics Service API")
                .version("1.0.0")
                .description("Employer and admin dashboard aggregates."));
    }
}
