package com.cinemaabyss.events.model;

/**
 * Event response model matching the API specification.
 * Represents the response when an event is successfully created.
 */
public class EventResponse {

    private String status;

    private int partition;

    private long offset;

    private Event event;

    // Default constructor
    public EventResponse() {
    }

    // Constructor with required fields
    public EventResponse(String status, int partition, long offset, Event event) {
        this.status = status;
        this.partition = partition;
        this.offset = offset;
        this.event = event;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "EventResponse{" +
                "status='" + status + '\'' +
                ", partition=" + partition +
                ", offset=" + offset +
                ", event=" + event +
                '}';
    }
}
