package com.example.controller;

import com.example.dto.ReviewDto;
import com.example.model.Movie;
import com.example.model.Review;
import com.example.model.User;
import com.example.repository.MovieRepository;
import com.example.repository.ReviewRepository;
import com.example.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MovieRepository movieRepository; // Assumed to be CrudRepository<Movie, Integer>

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReview(@RequestBody ReviewDto reviewDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Optional<User> userOptional = userRepository.findByUsername(currentUsername);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found or not authenticated"));
        }
        User user = userOptional.get();

        if (reviewDto.getMovieId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Movie ID is required"));
        }
        Optional<Movie> movieOptional = movieRepository.findById(reviewDto.getMovieId());
        if (movieOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Movie not found with ID: " + reviewDto.getMovieId()));
        }
        Movie movie = movieOptional.get();

        Review review = new Review();
        review.setContent(reviewDto.getContent());

        Integer ratingValue = reviewDto.getRating();
        if (ratingValue == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Rating is required and cannot be null."));
        }
        review.setRating(ratingValue);

        review.setMovie(movie);
        review.setUser(user);
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }

        Review savedReview = reviewRepository.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedReview));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByMovieId(@PathVariable Integer movieId) {
        if (!movieRepository.existsById(movieId)) {
            return ResponseEntity.notFound().build();
        }
        List<Review> reviews = reviewRepository.findByMovieId(movieId);
        List<ReviewDto> reviewDtos = reviews.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(reviewDtos);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Optional<Review> reviewOptional = reviewRepository.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Review not found"));
        }
        Review review = reviewOptional.get();

        Optional<User> currentUserOptional = userRepository.findByUsername(currentUsername);
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authenticated user not found in database"));
        }
        User currentUser = currentUserOptional.get();

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (review.getUser().getId().equals(currentUser.getId()) || isAdmin) {
            reviewRepository.delete(review);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not authorized to delete this review"));
        }
    }

    private ReviewDto convertToDto(Review review) {
        Integer movieId = null;
        Movie movie = review.getMovie();
        if (movie != null) {
            movieId = movie.getId();
        }

        String username = null;
        Long userId = null;
        User user = review.getUser();
        if (user != null) {
            username = user.getUsername();
            userId = user.getId(); 
        }

        Integer rating = review.getRating();

        return new ReviewDto(
                review.getId(),
                movieId,
                userId,
                username,
                review.getContent(),
                rating,
                review.getCreatedAt()
        );
    }
}