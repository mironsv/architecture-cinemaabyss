package com.cinemaabyss.events.service;

import com.cinemaabyss.events.model.Event;
import com.cinemaabyss.events.model.EventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

/**
 * Event producer service for sending events to Kafka topics.
 * Handles movie-events, user-events, and payment-events topics.
 */
@Service
public class EventProducerService {

    private static final Logger logger = LoggerFactory.getLogger(EventProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // Topic names matching docker-compose.yml configuration
    private static final String MOVIE_EVENTS_TOPIC = "movie-events";
    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    /**
     * Sends a movie event to the movie-events topic.
     */
    public EventResponse sendMovieEvent(Event event) {
        return sendEvent(MOVIE_EVENTS_TOPIC, event);
    }

    /**
     * Sends a user event to the user-events topic.
     */
    public EventResponse sendUserEvent(Event event) {
        return sendEvent(USER_EVENTS_TOPIC, event);
    }

    /**
     * Sends a payment event to the payment-events topic.
     */
    public EventResponse sendPaymentEvent(Event event) {
        return sendEvent(PAYMENT_EVENTS_TOPIC, event);
    }

    /**
     * Generic method to send events to specified topic.
     */
    private EventResponse sendEvent(String topic, Event event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, event.getId(), event);

            // Wait for the future to complete and handle the result
            try {
                SendResult<String, Object> result = future.join();
                if (result.getRecordMetadata() != null) {
                    logger.info("Event sent successfully to topic {}: id={}, partition={}, offset={}", 
                        topic, event.getId(), result.getRecordMetadata().partition(), 
                        result.getRecordMetadata().offset());
                    
                    EventResponse response = new EventResponse();
                    response.setStatus("success");
                    response.setEvent(event);
                    response.setPartition(result.getRecordMetadata().partition());
                    response.setOffset(result.getRecordMetadata().offset());
                    return response;
                } else {
                    logger.warn("Event sent to topic {} but metadata is null: id={}", 
                        topic, event.getId());
                    
                    EventResponse response = new EventResponse();
                    response.setStatus("success");
                    response.setEvent(event);
                    response.setPartition(-1);
                    response.setOffset(-1);
                    return response;
                }
                
            } catch (Exception e) {
                logger.error("Failed to send event to topic {}: id={}, error={}", 
                    topic, event.getId(), e.getMessage());
                
                EventResponse response = new EventResponse();
                response.setStatus("error: " + e.getMessage());
                response.setEvent(event);
                response.setPartition(-1);
                response.setOffset(-1);
                return response;
            }

        } catch (Exception e) {
            logger.error("Error sending event to topic {}: {}", topic, e.getMessage());
            EventResponse response = new EventResponse();
            response.setStatus("error: " + e.getMessage());
            response.setEvent(event);
            response.setPartition(-1);
            response.setOffset(-1);
            return response;
        }
    }
}
