package com.cinemaabyss.events.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for the Events Microservice.
 * Provides endpoint for checking service availability.
 */
@RestController
@RequestMapping("/api/events")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    /**
     * Health check endpoint for the events service.
     * Returns service status for monitoring and load balancing.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check requested for events service");
        
        return ResponseEntity.ok("{\"status\": true}");
    }
}
