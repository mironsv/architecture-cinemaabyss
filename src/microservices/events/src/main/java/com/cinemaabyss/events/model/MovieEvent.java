package com.cinemaabyss.events.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Movie event model matching the API specification.
 * Represents events related to movies (viewed, rated, added, etc.).
 */
public class MovieEvent {

    @JsonProperty("movie_id")
    private int movieId;

    private String title;

    private String action;

    @JsonProperty("user_id")
    private Integer userId;

    private Double rating;

    private String[] genres;

    private String description;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    // Default constructor
    public MovieEvent() {
    }

    // Constructor with required fields
    public MovieEvent(int movieId, String title, String action) {
        this.movieId = movieId;
        this.title = title;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MovieEvent{" +
                "movieId=" + movieId +
                ", title='" + title + '\'' +
                ", action='" + action + '\'' +
                ", userId=" + userId +
                ", rating=" + rating +
                ", genres=" + java.util.Arrays.toString(genres) +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
