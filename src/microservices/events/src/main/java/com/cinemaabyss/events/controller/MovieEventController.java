package com.cinemaabyss.events.controller;

import com.cinemaabyss.events.model.Event;
import com.cinemaabyss.events.model.EventResponse;
import com.cinemaabyss.events.model.MovieEvent;
import com.cinemaabyss.events.service.EventProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for handling movie events.
 * Provides endpoint for creating movie events that are sent to Kafka.
 */
@RestController
@RequestMapping("/api/events")
public class MovieEventController {

    private static final Logger logger = LoggerFactory.getLogger(MovieEventController.class);

    @Autowired
    private EventProducerService eventProducerService;

    /**
     * Creates a new movie event.
     * The event is sent to the movie-events Kafka topic and processed by the consumer.
     */
    @PostMapping("/movie")
    public ResponseEntity<EventResponse> createMovieEvent(@Valid @RequestBody MovieEvent movieEvent) {
        try {
            logger.info("Creating movie event: movieId={}, title='{}', action='{}'", 
                movieEvent.getMovieId(), movieEvent.getTitle(), movieEvent.getAction());

            // Create event wrapper
            Event event = new Event("movie", movieEvent);

            // Send event to Kafka
            EventResponse response = eventProducerService.sendMovieEvent(event);

            logger.info("Movie event created successfully: id={}", event.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating movie event: {}", e.getMessage(), e);
            EventResponse errorResponse = new EventResponse();
            errorResponse.setStatus("error: " + e.getMessage());
            errorResponse.setEvent(new Event("movie", movieEvent));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
