package com.nexthire.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * HealthController — Service health check endpoint.
 * Used by Render, cloud load balancers, and monitoring tools to check uptime.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Service health check endpoint for monitoring & deployment")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Returns 200 OK when NextHire backend is live and healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "nexthire-backend",
                "timestamp", Instant.now().toString()
        ));
    }
}
