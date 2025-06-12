package com.example.controller;

import com.example.dto.LoginRequest;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request,
            HttpServletResponse response) {
        logger.info("Login attempt for username: '{}'", loginRequest.getUsername());

        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            logger.warn("Login attempt with empty username or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Username and password cannot be empty"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            logger.info("User '{}' authenticated successfully. JSESSIONID: {}", loginRequest.getUsername(),
                    request.getSession(false) != null ? request.getSession(false).getId() : "null");

            User userDetails = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
            if (userDetails != null) {
                Set<String> roles = userDetails.getRoles().stream()
                        .map(Role::name)
                        .collect(Collectors.toSet());

                Map<String, Object> userResponse = Map.of(
                        "id", userDetails.getId(),
                        "username", userDetails.getUsername(),
                        "roles", roles);
                return ResponseEntity.ok(userResponse);
            } else {
                 logger.error("Authenticated user '{}' not found in repository after successful authentication.",
                        authentication.getName());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Error retrieving user details after login."));
            }

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user '{}': Bad credentials", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication failed: Bad credentials"));
        } catch (Exception e) {
            logger.error("Error during authentication for user '{}'", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login error: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody LoginRequest signUpRequest) {
        logger.info("Registration attempt for username: '{}'", signUpRequest.getUsername());

        if (signUpRequest.getUsername() == null || signUpRequest.getUsername().trim().isEmpty() ||
                signUpRequest.getPassword() == null || signUpRequest.getPassword().isEmpty()) {
            logger.warn("Registration attempt with empty username or password.");
            return ResponseEntity.badRequest().body(Map.of("message", "Username and password cannot be empty"));
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            logger.warn("Registration failed: Username '{}' is already taken.", signUpRequest.getUsername());
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Error: Username is already taken!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRoles(Set.of(Role.USER)); // Make sure Role.USER is a valid Role in your system

        try {
            userRepository.save(user);
            logger.info("User '{}' registered successfully.", signUpRequest.getUsername());
            return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
        } catch (Exception e) {
            logger.error("Error during registration for user '{}'", signUpRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error during registration: " + e.getMessage()));
        }
    }
}