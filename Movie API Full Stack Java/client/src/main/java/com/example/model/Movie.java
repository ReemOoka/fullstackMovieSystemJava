package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {
    private Long id;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty director = new SimpleStringProperty();
    private final IntegerProperty releaseYear = new SimpleIntegerProperty();
    private final StringProperty genre = new SimpleStringProperty();

    public Movie() {}

    public Movie(Long id, String title, String director, Integer releaseYear, String genre) {
        this.id = id;
        setTitle(title);
        setDirector(director);
        setReleaseYear(releaseYear); 
        setGenre(genre);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public void setTitle(String title) { this.title.set(title); }

    public String getDirector() { return director.get(); }
    public StringProperty directorProperty() { return director; }
    public void setDirector(String director) { this.director.set(director); }

    public Integer getReleaseYear() {
        int yearValue = this.releaseYear.get();
        return yearValue == 0 ? null : yearValue; 
    }
    public IntegerProperty releaseYearProperty() { return releaseYear; }
    public void setReleaseYear(Integer releaseYearValue) {
        if (releaseYearValue == null) {
            this.releaseYear.set(0);
        } else {
            this.releaseYear.set(releaseYearValue);
        }
    }

    public String getGenre() { return genre.get(); }
    public StringProperty genreProperty() { return genre; }
    public void setGenre(String genre) { this.genre.set(genre); }

    @Override
    public String toString() {
        return "Movie{" +
               "id=" + id +
               ", title='" + getTitle() + '\'' +
               ", director='" + getDirector() + '\'' +
               ", releaseYear=" + getReleaseYear() + 
               ", genre='" + getGenre() + '\'' +
               '}';
    }
}