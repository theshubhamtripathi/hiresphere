package com.hiresphere.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/** Circuit-breaker fallbacks: a uniform 503 when a downstream service is unavailable. */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/{service}")
    public ResponseEntity<Map<String, Object>> getFallback(@PathVariable String service) {
        return build(service);
    }

    @PostMapping("/{service}")
    public ResponseEntity<Map<String, Object>> postFallback(@PathVariable String service) {
        return build(service);
    }

    private ResponseEntity<Map<String, Object>> build(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", 503,
                "error", "Service Unavailable",
                "message", "The " + service + " service is temporarily unavailable. Please try again shortly.",
                "timestamp", Instant.now().toString()));
    }
}
