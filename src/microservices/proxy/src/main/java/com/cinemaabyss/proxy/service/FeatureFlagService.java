package com.cinemaabyss.proxy.service;

import com.cinemaabyss.proxy.config.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service for handling feature flags and routing decisions.
 * Implements gradual migration logic for routing requests
 * between monolith and microservices.
 */
@Service
public class FeatureFlagService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagService.class);
    
    private final ServiceConfiguration config;
    private final Random random;

    @Autowired
    public FeatureFlagService(ServiceConfiguration config) {
        this.config = config;
        this.random = new Random();
        logger.info("FeatureFlagService initialized with configuration: {}", config);
    }

    /**
     * Determines which service should handle the request based on feature flags.
     * 
     * @param path The request path
     * @return The target service URL
     */
    public String getTargetService(String path) {
        if (path.startsWith("/api/movies")) {
            return getMoviesServiceTarget();
        } else if (path.startsWith("/api/events")) {
            return config.getEventsServiceUrl();
        } else {
            // Default to monolith for users, payments, subscriptions
            return config.getMonolithUrl();
        }
    }

    /**
     * Determines the target service for movie requests based on feature flags.
     * 
     * @return The target service URL for movies
     */
    private String getMoviesServiceTarget() {
        if (!config.isGradualMigration()) {
            logger.debug("Gradual migration disabled, routing to monolith");
            return config.getMonolithUrl();
        }

        int randomValue = random.nextInt(100);
        int migrationPercent = config.getMoviesMigrationPercent();

        if (randomValue < migrationPercent) {
            logger.debug("Routing to movies microservice ({}% migration, random: {})", 
                        migrationPercent, randomValue);
            return config.getMoviesServiceUrl();
        } else {
            logger.debug("Routing to monolith ({}% migration, random: {})", 
                        migrationPercent, randomValue);
            return config.getMonolithUrl();
        }
    }

    /**
     * Checks if a path should be handled by the movies service.
     * 
     * @param path The request path
     * @return true if the path is for movies
     */
    public boolean isMoviesPath(String path) {
        return path.startsWith("/api/movies");
    }

    /**
     * Checks if a path should be handled by the events service.
     * 
     * @param path The request path
     * @return true if the path is for events
     */
    public boolean isEventsPath(String path) {
        return path.startsWith("/api/events");
    }

    /**
     * Checks if a path should be handled by the monolith.
     * 
     * @param path The request path
     * @return true if the path should go to monolith
     */
    public boolean isMonolithPath(String path) {
        return !isMoviesPath(path) && !isEventsPath(path);
    }
}
