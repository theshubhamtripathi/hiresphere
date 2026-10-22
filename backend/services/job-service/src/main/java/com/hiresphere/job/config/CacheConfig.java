package com.hiresphere.job.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/** Redis cache for job metadata and job details. */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String JOB_DETAILS = "jobDetails";
    public static final String JOB_CATEGORIES = "jobCategories";
    public static final String SKILLS = "skills";
    public static final String TAGS = "tags";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default (JDK) value serialization; all cached DTOs implement Serializable,
        // which handles java.time types that the JSON serializer rejects by default.
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(JOB_DETAILS, base.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration(JOB_CATEGORIES, base.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(SKILLS, base.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(TAGS, base.entryTtl(Duration.ofHours(1)))
                .build();
    }
}
