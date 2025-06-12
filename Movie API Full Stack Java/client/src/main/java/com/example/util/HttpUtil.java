package com.example.util;

import com.example.dto.ReviewRequestDto;
import com.example.model.Movie;
import com.example.model.Review;
import com.example.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    public static final String BASE_URL = "http://localhost:8080/api";
    private static final CookieManager cookieManager = new CookieManager();
    private static final HttpClient client;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String csrfTokenValue = null;

    static {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String getCsrfToken() {
        return csrfTokenValue;
    }

    private static void updateCsrfTokenFromCookies() {
        boolean found = false;
        String previousToken = csrfTokenValue;
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if ("XSRF-TOKEN".equalsIgnoreCase(cookie.getName())) {
                csrfTokenValue = cookie.getValue();
                if (previousToken == null || !previousToken.equals(csrfTokenValue)) {
                    logger.info("CSRF Token updated/set: {}", csrfTokenValue);
                }
                found = true;
                break;
            }
        }
        if (!found && previousToken == null) {
            logger.debug("XSRF-TOKEN cookie not found and no previous token stored.");
        }
    }

    public static void primeCookies() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/movies"))
                    .GET()
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
            updateCsrfTokenFromCookies();
            logger.info("Cookies primed. Current CSRF token (if any): {}", csrfTokenValue);
        } catch (Exception e) {
            logger.error("Failed to prime cookies: {}", e.getMessage(), e);
        }
    }

    public static User login(String username, String password) throws Exception {
        logger.info("Attempting login for username: '{}'", username);

        Map<String, String> loginData = Map.of("username", username, "password", password);
        String jsonPayload = objectMapper.writeValueAsString(loginData);
        logger.debug("Login JSON payload: {}", jsonPayload);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

        if (csrfTokenValue == null) {
            logger.warn("CSRF token is null before login. Attempting to prime cookies first.");
            primeCookies();
        }

        if (csrfTokenValue != null) {
            logger.debug("Attaching CSRF token to login request: {}", csrfTokenValue);
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.error(
                    "CSRF token still null after priming. Login might fail CSRF check if server expects it for login.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() == 200) {
            logger.info("Login successful for user: {}", username);
            try {
                if (response.body() != null && response.body().contains("username")) {
                    return objectMapper.readValue(response.body(), User.class);
                } else {
                    logger.warn("Login response did not seem to be a User object. Response: {}", response.body());
                    User loggedInUser = new User();
                    loggedInUser.setUsername(username);
                    return loggedInUser;
                }
            } catch (Exception e) {
                logger.error("Error parsing login success response: {}. Body: {}", e.getMessage(), response.body());
                throw new RuntimeException("Login succeeded but failed to parse response: " + e.getMessage());
            }
        } else if (response.statusCode() == 401 || response.statusCode() == 403) {
            logger.error("Login failed with status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Login failed. Server response: " + response.body());
        } else {
            logger.error("Login request failed with status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException(
                    "Login failed with status: " + response.statusCode() + ". Server: " + response.body());
        }
    }

    public static void register(String username, String password) throws Exception {
        Map<String, String> registrationData = Map.of("username", username, "password", password);
        String jsonPayload = objectMapper.writeValueAsString(registrationData);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

        if (csrfTokenValue == null) {
            logger.warn("CSRF token is null before registration. Attempting to prime cookies first.");
            primeCookies();
        }

        if (csrfTokenValue != null) {
            logger.debug("Attaching CSRF token to register request: {}", csrfTokenValue);
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.error("CSRF token still null after priming for registration. Registration might fail CSRF check.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            String errorMessage = response.body();
            try {
                Map<String, String> errorResponse = objectMapper.readValue(response.body(), new TypeReference<>() {});
                errorMessage = errorResponse.getOrDefault("message", response.body());
            } catch (Exception ignored) {
            }
            logger.error("Registration failed with status {}: {}", response.statusCode(), errorMessage);
            throw new RuntimeException(
                    "Registration failed with status " + response.statusCode() + ": " + errorMessage);
        }
        logger.info("Registration successful for user: {}", username);
    }

    public static List<Movie> getAllMovies() throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies"))
                .GET();

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        logger.info("getAllMovies - Status Code: {}", response.statusCode());
        logger.debug("getAllMovies - Raw Response Body: {}", response.body());

        if (response.statusCode() == 200) {
            try {
                return objectMapper.readValue(response.body(), new TypeReference<List<Movie>>() {});
            } catch (Exception e) {
                logger.error("Error parsing movies JSON: {}", response.body(), e);
                throw new RuntimeException("Failed to parse movies: " + e.getMessage() + ". Response: "
                        + response.body().substring(0, Math.min(response.body().length(), 200)), e);
            }
        } else {
            logger.error("Failed to load movies, status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Failed to load movies: " + response.statusCode() + " - " + response.body());
        }
    }

    public static Review addReview(ReviewRequestDto reviewRequestDto) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(reviewRequestDto);
        logger.debug("addReview JSON payload: {}", jsonPayload);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reviews"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

        if (csrfTokenValue != null) {
            logger.debug("Attaching CSRF token to addReview request: {}", csrfTokenValue);
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for addReview request. This might fail if CSRF is required.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            logger.info("Review added successfully for movie ID: {}", reviewRequestDto.getMovieId());
            return objectMapper.readValue(response.body(), Review.class);
        } else {
            String errorMessage = "Failed to add review: " + response.statusCode();
            try {
                Map<String, String> errorMap = objectMapper.readValue(response.body(),
                        new TypeReference<Map<String, String>>() {});
                if (errorMap.containsKey("message")) {
                    errorMessage += " - " + errorMap.get("message");
                } else if (errorMap.containsKey("error")) {
                    errorMessage += " - " + errorMap.get("error");
                } else {
                    errorMessage += " - " + response.body();
                }
            } catch (Exception e) {
                errorMessage += " - " + response.body();
                logger.warn("Could not parse error response body for addReview: {}", response.body());
            }
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    public static List<Review> getReviewsForMovie(Long movieId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reviews/movie/" + movieId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Review>>() {});
        } else {
            logger.error("Failed to fetch reviews for movie {}, status: {}, body: {}", movieId, response.statusCode(),
                    response.body());
            throw new RuntimeException("Failed to fetch reviews: " + response.statusCode());
        }
    }

    public static Movie addMovie(Movie movie) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(movie);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

        if (csrfTokenValue != null) {
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for addMovie request.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            logger.info("Movie added successfully via HttpUtil.addMovie");
            return objectMapper.readValue(response.body(), Movie.class);
        } else {
            logger.error("Failed to add movie, status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Failed to add movie: " + response.statusCode() + " - " + response.body());
        }
    }

    public static Movie updateMovie(Movie movie) throws Exception {
        if (movie.getId() == null || movie.getId() == 0) {
            throw new IllegalArgumentException(
                    "Movie ID cannot be null or 0 for an update. This might be a new movie; use addMovie instead.");
        }

        String jsonPayload = objectMapper.writeValueAsString(movie);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies/" + movie.getId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload));

        if (csrfTokenValue != null) {
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for updateMovie request.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() == 200) {
            logger.info("Movie updated successfully: {}", movie.getId());
            return objectMapper.readValue(response.body(), Movie.class);
        } else {
            logger.error("Failed to update movie {}, status {}: {}", movie.getId(), response.statusCode(),
                    response.body());
            throw new RuntimeException("Failed to update movie: " + response.statusCode() + " - " + response.body());
        }
    }

    public static void deleteReview(Long reviewId) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reviews/" + reviewId))
                .DELETE();

        if (csrfTokenValue != null) {
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for deleteReview request.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            String errorMessage = "Failed to delete review: " + response.statusCode();
            try {
                Map<String, String> errorMap = objectMapper.readValue(response.body(),
                        new TypeReference<Map<String, String>>() {});
                if (errorMap.containsKey("message")) {
                    errorMessage += " - " + errorMap.get("message");
                } else if (errorMap.containsKey("error")) {
                    errorMessage += " - " + errorMap.get("error");
                } else {
                    errorMessage += " - " + response.body();
                }
            } catch (Exception e) {
                errorMessage += " - " + response.body();
                logger.warn("Could not parse error response body for deleteReview: {}", response.body());
            }
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        logger.info("Review {} deleted successfully via HttpUtil.", reviewId);
    }

    public static void deleteMovie(Long movieId) throws Exception {
        if (movieId == null || movieId == 0) {
            throw new IllegalArgumentException("Movie ID cannot be null or 0 for deletion.");
        }
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/movies/" + movieId))
                .DELETE();

        if (csrfTokenValue != null) {
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for deleteMovie request.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            logger.error("Failed to delete movie {}, status {}: {}", movieId, response.statusCode(), response.body());
            throw new RuntimeException("Failed to delete movie: " + response.statusCode() + " - " + response.body());
        }
        logger.info("Movie {} deleted successfully.", movieId);
    }

    public static List<User> getAllUsers() throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/admin/users"))
                .GET();

        if (csrfTokenValue != null) {
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for getAllUsers request.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
        } else {
            logger.error("Failed to fetch users, status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Failed to fetch users: " + response.statusCode() + " - " + response.body());
        }
    }

    public static void deleteUser(Long userId) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/admin/users/" + userId))
                .DELETE();

        if (csrfTokenValue != null) {
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for deleteUser request.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            logger.error("Failed to delete user {}, status {}: {}", userId, response.statusCode(), response.body());
            throw new RuntimeException("Failed to delete user: " + response.statusCode() + " - " + response.body());
        }
        logger.info("User {} deleted successfully via HttpUtil.", userId);
    }

    public static void logout() throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/logout"))
                .POST(HttpRequest.BodyPublishers.noBody());

        if (csrfTokenValue != null) {
            logger.debug("Attaching CSRF token to logout request: {}", csrfTokenValue);
            requestBuilder.header("X-XSRF-TOKEN", csrfTokenValue);
        } else {
            logger.warn("CSRF token is null for logout request.");
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        updateCsrfTokenFromCookies();
        
        logger.info("Logout request sent. Status: {}.", response.statusCode());

        if (response.statusCode() != 200) {
            logger.warn("Logout request returned status {}: {}", response.statusCode(), response.body());
        }
    }
}