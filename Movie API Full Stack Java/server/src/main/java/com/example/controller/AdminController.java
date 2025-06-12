package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin") 
@PreAuthorize("hasRole('ADMIN')") 
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        logger.info("Admin request to get all users received.");
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userResponses = users.stream().map(user -> Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "roles", user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
            )).collect(Collectors.toList());
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            logger.error("Admin error fetching all users", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        logger.info("Admin request to delete user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            logger.warn("Admin attempted to delete non-existent user with ID: {}", userId);
            return ResponseEntity.notFound().build();
        }
        try {
            userRepository.deleteById(userId);
            logger.info("User with ID: {} deleted successfully by admin.", userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            logger.error("Admin error deleting user with ID: {}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Error deleting user: " + e.getMessage()));
        }
    }
}