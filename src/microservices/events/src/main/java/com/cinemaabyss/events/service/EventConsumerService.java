package com.cinemaabyss.events.service;

import com.cinemaabyss.events.model.Event;
import com.cinemaabyss.events.model.MovieEvent;
import com.cinemaabyss.events.model.PaymentEvent;
import com.cinemaabyss.events.model.UserEvent;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

/**
 * Event consumer service for processing events from Kafka topics.
 * Listens to movie-events, user-events, and payment-events topics
 * and processes them with structured logging.
 */
@Service
public class EventConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumerService.class);

    /**
     * Listens to movie-events topic and processes movie events.
     */
    @KafkaListener(topics = "movie-events", groupId = "events-service-group")
    public void consumeMovieEvent(Event event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            logger.info("Processing movie event: id={}, type={}, timestamp={}, topic={}, partition={}, offset={}",
                    event.getId(), event.getType(), event.getTimestamp(), topic, partition, offset);

            // Log detailed event information
            if (event.getPayload() instanceof MovieEvent movieEvent) {
                logger.info("Movie Event Details - Movie ID: {}, Title: '{}', Action: '{}', User ID: {}, Rating: {}",
                        movieEvent.getMovieId(), movieEvent.getTitle(), movieEvent.getAction(),
                        movieEvent.getUserId(), movieEvent.getRating());
            } else if (event.getPayload() instanceof LinkedHashMap<?, ?> map) {
                logger.info("Movie Event Details from LinkedHashMap - Movie ID: {}, Title: '{}', Action: '{}', User ID: {}, Rating: {}",
                        map.get("movie_id"), map.get("title"), map.get("action"), map.get("user_id"), map.get("rating"));
            } else {
                logger.warn("Unexpected payload type for movie event: {}", event.getPayload().getClass().getName());
            }

            logger.info("Successfully processed movie event: {}", event.getId());

        } catch (Exception e) {
            logger.error("Error processing movie event: id={}, error={}", event.getId(), e.getMessage(), e);
            // In a production system, you might want to implement retry logic or dead
            // letter queues
        }
    }

    /**
     * Listens to user-events topic and processes user events.
     */
    @KafkaListener(topics = "user-events", groupId = "events-service-group")
    public void consumeUserEvent(Event event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            logger.info("Processing user event: id={}, type={}, timestamp={}, topic={}, partition={}, offset={}",
                    event.getId(), event.getType(), event.getTimestamp(), topic, partition, offset);

            // Log detailed event information
            if (event.getPayload() instanceof UserEvent userEvent) {
                logger.info("User Event Details - User ID: {}, Username: '{}', Email: '{}', Action: '{}'",
                        userEvent.getUserId(), userEvent.getUsername(), userEvent.getEmail(), userEvent.getAction());
            } else if (event.getPayload() instanceof LinkedHashMap<?, ?> map) {
                logger.info("User Event Details from LinkedHashMap - User ID: {}, Username: '{}', Email: '{}', Action: '{}'",
                        map.get("userId"), map.get("username"), map.get("email"), map.get("action"));
            } else {
                logger.warn("Unexpected payload type for user event: {}", event.getPayload().getClass().getName());
            }

            logger.info("Successfully processed user event: {}", event.getId());

        } catch (Exception e) {
            logger.error("Error processing user event: id={}, error={}", event.getId(), e.getMessage(), e);
        }
    }

    /**
     * Listens to payment-events topic and processes payment events.
     */
    @KafkaListener(topics = "payment-events", groupId = "events-service-group")
    public void consumePaymentEvent(Event event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            logger.info("Processing payment event: id={}, type={}, timestamp={}, topic={}, partition={}, offset={}",
                    event.getId(), event.getType(), event.getTimestamp(), topic, partition, offset);

            // Log detailed event information
            if (event.getPayload() instanceof PaymentEvent paymentEvent) {
                logger.info(
                        "Payment Event Details - Payment ID: {}, User ID: {}, Amount: {}, Status: '{}', Method: '{}'",
                        paymentEvent.getPaymentId(), paymentEvent.getUserId(), paymentEvent.getAmount(),
                        paymentEvent.getStatus(), paymentEvent.getMethodType());
            } else if (event.getPayload() instanceof LinkedHashMap<?, ?> map) {
                logger.info("Payment Event Details from LinkedHashMap - Payment ID: {}, User ID: {}, Amount: {}, Status: '{}', Method: '{}'",
                        map.get("paymentId"), map.get("userId"), map.get("amount"), map.get("status"), map.get("methodType"));
            } else {
                logger.warn("Unexpected payload type for payment event: {}", event.getPayload().getClass().getName());
            }

            logger.info("Successfully processed payment event: {}", event.getId());

        } catch (Exception e) {
            logger.error("Error processing payment event: id={}, error={}", event.getId(), e.getMessage(), e);
        }
    }
}
