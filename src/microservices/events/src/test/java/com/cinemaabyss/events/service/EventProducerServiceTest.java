package com.cinemaabyss.events.service;

import com.cinemaabyss.events.model.Event;
import com.cinemaabyss.events.model.EventResponse;
import com.cinemaabyss.events.model.MovieEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventProducerService.
 */
@ExtendWith(MockitoExtension.class)
class EventProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private EventProducerService eventProducerService;

    @BeforeEach
    void setUp() {
        eventProducerService = new EventProducerService();
        // Use reflection to set the kafkaTemplate field
        try {
            java.lang.reflect.Field field = EventProducerService.class.getDeclaredField("kafkaTemplate");
            field.setAccessible(true);
            field.set(eventProducerService, kafkaTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSendMovieEvent_Success() {
        // Given
        MovieEvent movieEvent = new MovieEvent(1, "Inception", "viewed");
        Event event = new Event("movie", movieEvent);
        
        SendResult<String, Object> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(mock(org.apache.kafka.clients.producer.RecordMetadata.class));
        when(sendResult.getRecordMetadata().partition()).thenReturn(0);
        when(sendResult.getRecordMetadata().offset()).thenReturn(1L);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(Event.class))).thenReturn(future);

        // When
        EventResponse response = eventProducerService.sendMovieEvent(event);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(event, response.getEvent());
        assertEquals(0, response.getPartition()); // We get the actual partition now
        assertEquals(1, response.getOffset()); // We get the actual offset now
    }

    @Test
    void testSendMovieEvent_Failure() {
        // Given
        MovieEvent movieEvent = new MovieEvent(1, "Inception", "viewed");
        Event event = new Event("movie", movieEvent);

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), any(Event.class))).thenReturn(future);

        // When
        EventResponse response = eventProducerService.sendMovieEvent(event);

        // Then
        assertNotNull(response);
        assertEquals("error: java.lang.RuntimeException: Kafka error", response.getStatus());
        assertEquals(event, response.getEvent());
        assertEquals(-1, response.getPartition());
        assertEquals(-1, response.getOffset());
    }

    @Test
    void testSendUserEvent() {
        // Given
        Event event = new Event("user", new Object());

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(eq("user-events"), anyString(), any(Event.class))).thenReturn(future);

        // When
        EventResponse response = eventProducerService.sendUserEvent(event);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(event, response.getEvent());
        assertEquals(-1, response.getPartition()); // Metadata is null in test
        assertEquals(-1, response.getOffset()); // Metadata is null in test
    }

    @Test
    void testSendPaymentEvent() {
        // Given
        Event event = new Event("payment", new Object());

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(eq("payment-events"), anyString(), any(Event.class))).thenReturn(future);

        // When
        EventResponse response = eventProducerService.sendPaymentEvent(event);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(event, response.getEvent());
        assertEquals(-1, response.getPartition()); // Metadata is null in test
        assertEquals(-1, response.getOffset()); // Metadata is null in test
    }
}
