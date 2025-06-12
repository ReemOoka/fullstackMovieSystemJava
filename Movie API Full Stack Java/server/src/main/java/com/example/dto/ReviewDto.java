package com.example.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private Integer movieId; // Changed to Integer
    private Long userId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createdAt;

    // Constructors
    public ReviewDto() {}

    public ReviewDto(Long id, Integer movieId, Long userId, String username, String content, Integer rating, LocalDateTime createdAt) { // movieId changed to Integer
        this.id = id;
        this.movieId = movieId; // Changed to Integer
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMovieId() { // Changed to Integer
        return movieId;
    }

    public void setMovieId(Integer movieId) { // Changed to Integer
        this.movieId = movieId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}