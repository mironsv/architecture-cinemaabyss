package com.cinemaabyss.events.controller;

import com.cinemaabyss.events.model.Event;
import com.cinemaabyss.events.model.EventResponse;
import com.cinemaabyss.events.model.UserEvent;
import com.cinemaabyss.events.service.EventProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for handling user events.
 * Provides endpoint for creating user events that are sent to Kafka.
 */
@RestController
@RequestMapping("/api/events")
public class UserEventController {

    private static final Logger logger = LoggerFactory.getLogger(UserEventController.class);

    @Autowired
    private EventProducerService eventProducerService;

    /**
     * Creates a new user event.
     * The event is sent to the user-events Kafka topic and processed by the consumer.
     */
    @PostMapping("/user")
    public ResponseEntity<EventResponse> createUserEvent(@Valid @RequestBody UserEvent userEvent) {
        try {
            logger.info("Creating user event: userId={}, action='{}'", 
                userEvent.getUserId(), userEvent.getAction());

            // Create event wrapper
            Event event = new Event("user", userEvent);

            // Send event to Kafka
            EventResponse response = eventProducerService.sendUserEvent(event);

            logger.info("User event created successfully: id={}", event.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating user event: {}", e.getMessage(), e);
            EventResponse errorResponse = new EventResponse();
            errorResponse.setStatus("error: " + e.getMessage());
            errorResponse.setEvent(new Event("user", userEvent));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
