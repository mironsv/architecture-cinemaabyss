package com.cinemaabyss.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main application class for the CinemaAbyss Events Microservice.
 * This service handles event creation and processing with Kafka integration.
 * It implements the Strangler Fig pattern for event management.
 */
@SpringBootApplication
@EnableKafka
public class EventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventsApplication.class, args);
    }
}
