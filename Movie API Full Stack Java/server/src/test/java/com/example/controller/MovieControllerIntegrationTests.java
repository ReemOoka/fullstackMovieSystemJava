package com.example.controller;

import com.example.model.Movie;
import com.example.repository.MovieRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class MovieControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    private Movie movie1;
    private Movie movie2;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll(); // Clean slate

        Movie m1 = new Movie();
        m1.setTitle("Inception");
        m1.setDirector("Christopher Nolan");
        m1.setReleaseYear(2010);
        m1.setGenre("Sci-Fi");
        movie1 = movieRepository.save(m1); // Save and get the managed entity

        Movie m2 = new Movie();
        m2.setTitle("The Dark Knight");
        m2.setDirector("Christopher Nolan");
        m2.setReleaseYear(2008);
        m2.setGenre("Action");
        movie2 = movieRepository.save(m2); // Save and get the managed entity
    }

    @AfterEach
    void tearDown() {
        movieRepository.deleteAll();
    }

    @Test
    void getAllMovies_shouldReturnListOfMoviesAndOkStatus() throws Exception {
        mockMvc.perform(get("/api/movies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                // Use movie1.getTitle() and movie1.getDirector()
                .andExpect(jsonPath("$[?(@.title == '" + movie1.getTitle() + "')].director",
                        is(Arrays.asList(movie1.getDirector()))))
                // Use movie2.getTitle() and movie2.getDirector()
                .andExpect(jsonPath("$[?(@.title == '" + movie2.getTitle() + "')].director",
                        is(Arrays.asList(movie2.getDirector()))));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void createMovie_whenUserIsAdmin_shouldCreateMovieAndReturnCreated() throws Exception {
        String newMovieJson = "{\"title\":\"New Movie Test\",\"director\":\"New Director Test\",\"releaseYear\":2023,\"genre\":\"Drama Test\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newMovieJson)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Movie Test")))
                .andExpect(jsonPath("$.director", is("New Director Test")));
    }
}