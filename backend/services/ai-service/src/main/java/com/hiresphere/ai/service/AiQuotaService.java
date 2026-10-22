package com.hiresphere.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

/** Per-user daily AI usage quota backed by Redis. */
@Service
public class AiQuotaService {

    private final StringRedisTemplate redis;
    private final int dailyQuota;

    public AiQuotaService(StringRedisTemplate redis,
                          @Value("${hiresphere.ai.daily-quota:50}") int dailyQuota) {
        this.redis = redis;
        this.dailyQuota = dailyQuota;
    }

    public void consume(Long userId) {
        if (userId == null) {
            return;
        }
        String key = "ai:quota:" + userId + ":" + LocalDate.now();
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofDays(1));
        }
        if (count != null && count > dailyQuota) {
            throw new QuotaExceededException("Daily AI usage limit reached. Try again tomorrow.");
        }
    }
}
