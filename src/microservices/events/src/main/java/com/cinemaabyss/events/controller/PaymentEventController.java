package com.cinemaabyss.events.controller;

import com.cinemaabyss.events.model.Event;
import com.cinemaabyss.events.model.EventResponse;
import com.cinemaabyss.events.model.PaymentEvent;
import com.cinemaabyss.events.service.EventProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for handling payment events.
 * Provides endpoint for creating payment events that are sent to Kafka.
 */
@RestController
@RequestMapping("/api/events")
public class PaymentEventController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventController.class);

    @Autowired
    private EventProducerService eventProducerService;

    /**
     * Creates a new payment event.
     * The event is sent to the payment-events Kafka topic and processed by the consumer.
     */
    @PostMapping("/payment")
    public ResponseEntity<EventResponse> createPaymentEvent(@Valid @RequestBody PaymentEvent paymentEvent) {
        try {
            logger.info("Creating payment event: paymentId={}, userId={}, amount={}, status='{}'", 
                paymentEvent.getPaymentId(), paymentEvent.getUserId(), 
                paymentEvent.getAmount(), paymentEvent.getStatus());

            // Create event wrapper
            Event event = new Event("payment", paymentEvent);

            // Send event to Kafka
            EventResponse response = eventProducerService.sendPaymentEvent(event);

            logger.info("Payment event created successfully: id={}", event.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating payment event: {}", e.getMessage(), e);
            EventResponse errorResponse = new EventResponse();
            errorResponse.setStatus("error: " + e.getMessage());
            errorResponse.setEvent(new Event("payment", paymentEvent));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
