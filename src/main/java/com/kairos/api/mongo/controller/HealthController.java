package com.kairos.api.mongo.controller;

import com.kairos.api.mongo.dto.HealthResponse;
import com.kairos.api.mongo.service.MongoHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final MongoHealthService health;

    public HealthController(MongoHealthService health) {
        this.health = health;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        boolean available = health.isAvailable();
        return ResponseEntity.status(available ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(new HealthResponse(available ? "UP" : "DOWN"));
    }
}
