package com.cinemaabyss.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for service URLs and feature flags.
 * Reads environment variables to configure service endpoints
 * and feature flag settings for gradual migration.
 */
@Configuration
@ConfigurationProperties(prefix = "cinemaabyss")
public class ServiceConfiguration {

    private String monolithUrl;
    private String moviesServiceUrl;
    private String eventsServiceUrl;
    private boolean gradualMigration = false;
    private int moviesMigrationPercent = 0;

    // Getters and Setters
    public String getMonolithUrl() {
        return monolithUrl;
    }

    public void setMonolithUrl(String monolithUrl) {
        this.monolithUrl = monolithUrl;
    }

    public String getMoviesServiceUrl() {
        return moviesServiceUrl;
    }

    public void setMoviesServiceUrl(String moviesServiceUrl) {
        this.moviesServiceUrl = moviesServiceUrl;
    }

    public String getEventsServiceUrl() {
        return eventsServiceUrl;
    }

    public void setEventsServiceUrl(String eventsServiceUrl) {
        this.eventsServiceUrl = eventsServiceUrl;
    }

    public boolean isGradualMigration() {
        return gradualMigration;
    }

    public void setGradualMigration(boolean gradualMigration) {
        this.gradualMigration = gradualMigration;
    }

    public int getMoviesMigrationPercent() {
        return moviesMigrationPercent;
    }

    public void setMoviesMigrationPercent(int moviesMigrationPercent) {
        this.moviesMigrationPercent = moviesMigrationPercent;
    }

    @Override
    public String toString() {
        return "ServiceConfiguration{" +
                "monolithUrl='" + monolithUrl + '\'' +
                ", moviesServiceUrl='" + moviesServiceUrl + '\'' +
                ", eventsServiceUrl='" + eventsServiceUrl + '\'' +
                ", gradualMigration=" + gradualMigration +
                ", moviesMigrationPercent=" + moviesMigrationPercent +
                '}';
    }
}
