package com.cinemaabyss.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the CinemaAbyss Proxy Service.
 * This API Gateway provides feature flag support for gradual migration
 * from monolith to microservices architecture.
 */
@SpringBootApplication
public class ProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyApplication.class, args);
    }
}
