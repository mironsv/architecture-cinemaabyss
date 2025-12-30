package com.cinemaabyss.proxy.service;

import com.cinemaabyss.proxy.config.ServiceConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FeatureFlagService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeatureFlagServiceTest {

    @Mock
    private ServiceConfiguration config;

    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagService = new FeatureFlagService(config);
    }

    @Test
    void testGetTargetService_MoviesPath_GradualMigrationDisabled() {
        // Given
        when(config.isGradualMigration()).thenReturn(false);
        when(config.getMonolithUrl()).thenReturn("http://monolith:8080");

        // When
        String targetService = featureFlagService.getTargetService("/api/movies");

        // Then
        assertEquals("http://monolith:8080", targetService);
    }

    @Test
    void testGetTargetService_MoviesPath_GradualMigrationEnabled() {
        // Given
        when(config.isGradualMigration()).thenReturn(true);
        when(config.getMoviesMigrationPercent()).thenReturn(50);
        when(config.getMonolithUrl()).thenReturn("http://monolith:8080");
        when(config.getMoviesServiceUrl()).thenReturn("http://movies-service:8081");

        // When
        String targetService = featureFlagService.getTargetService("/api/movies");

        // Then
        assertNotNull(targetService);
        assertTrue(targetService.equals("http://monolith:8080") || 
                   targetService.equals("http://movies-service:8081"));
    }

    @Test
    void testGetTargetService_EventsPath() {
        // Given
        when(config.getEventsServiceUrl()).thenReturn("http://events-service:8082");

        // When
        String targetService = featureFlagService.getTargetService("/api/events");

        // Then
        assertEquals("http://events-service:8082", targetService);
    }

    @Test
    void testGetTargetService_MonolithPath() {
        // Given
        when(config.getMonolithUrl()).thenReturn("http://monolith:8080");

        // When
        String targetService = featureFlagService.getTargetService("/api/users");

        // Then
        assertEquals("http://monolith:8080", targetService);
    }

    @Test
    void testIsMoviesPath() {
        assertTrue(featureFlagService.isMoviesPath("/api/movies"));
        assertTrue(featureFlagService.isMoviesPath("/api/movies/1"));
        assertFalse(featureFlagService.isMoviesPath("/api/users"));
        assertFalse(featureFlagService.isMoviesPath("/api/events"));
    }

    @Test
    void testIsEventsPath() {
        assertTrue(featureFlagService.isEventsPath("/api/events"));
        assertTrue(featureFlagService.isEventsPath("/api/events/health"));
        assertFalse(featureFlagService.isEventsPath("/api/users"));
        assertFalse(featureFlagService.isEventsPath("/api/movies"));
    }

    @Test
    void testIsMonolithPath() {
        assertTrue(featureFlagService.isMonolithPath("/api/users"));
        assertTrue(featureFlagService.isMonolithPath("/api/payments"));
        assertTrue(featureFlagService.isMonolithPath("/api/subscriptions"));
        assertFalse(featureFlagService.isMonolithPath("/api/movies"));
        assertFalse(featureFlagService.isMonolithPath("/api/events"));
    }
}
