package com.example.service;

import com.example.exception.ResourceNotFoundException;
import com.example.model.Movie; 
import com.example.model.Review;
import com.example.model.User;
import com.example.repository.MovieRepository; 
import com.example.repository.ReviewRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository; // For fetching Movie if needed

    public List<Review> getReviewsByMovieId(int movieId) { // Assuming Movie ID is int
        return reviewRepository.findByMovieId(movieId);
    }

    @Transactional
    public Review addReview(Review reviewRequest, UserDetails currentUserDetails) {
        User user = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUserDetails.getUsername()));

        // Ensure movie is set on the review.
        // The reviewRequest from client should contain movie ID.
        if (reviewRequest.getMovie() == null || reviewRequest.getMovie().getId() == 0) {
            throw new IllegalArgumentException("Movie ID must be provided to add a review.");
        }

        Movie movie = movieRepository.findById(reviewRequest.getMovie().getId()) // Assuming Movie ID is int
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + reviewRequest.getMovie().getId()));

        reviewRequest.setUser(user);
        reviewRequest.setMovie(movie); // Ensure the full movie object is associated
        reviewRequest.setCreatedAt(LocalDateTime.now()); // Set creation time
        return reviewRepository.save(reviewRequest);
    }

    @Transactional
    public void deleteReview(Long reviewId, UserDetails currentUserDetails) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        User requestingUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Requesting user not found: " + currentUserDetails.getUsername()));

        boolean isAdmin = currentUserDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        // Check if the user is an admin or the owner of the review
        if (isAdmin || review.getUser().getId().equals(requestingUser.getId())) {
            reviewRepository.delete(review);
        } else {
            throw new AccessDeniedException("You are not authorized to delete this review.");
        }
    }
}