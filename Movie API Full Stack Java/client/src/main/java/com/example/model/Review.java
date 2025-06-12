package com.example.model;

public class Review {

    private Long id;
    private Long movieId; 
    private Long userId;
    private String username;
    private String content;
    private Integer rating;

    // Constructors
    public Review() {
    }

    public Review(Long id, Long movieId, Long userId, String username, String content, Integer rating) {
        this.id = id;
        this.movieId = movieId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
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

    @Override
    public String toString() {
        return "Review{" +
               "id=" + id +
               ", movieId=" + movieId +
               ", userId=" + userId +
               ", username='" + username + '\'' +
               ", content='" + content + '\'' +
               ", rating=" + rating +
               '}';
    }
}