package com.example.controller;

import com.example.model.Movie;
import com.example.util.HttpUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;

public class MovieFormController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField titleField;
    @FXML
    private TextField directorField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField genreField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label errorMessageLabel;

    private Stage dialogStage;
    private Movie movie;
    private boolean okClicked = false;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("");
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        if (this.movie != null && this.movie.getId() != null) { // Check if it's an existing movie
            if (titleLabel != null) titleLabel.setText("Edit Movie");
            if (dialogStage != null) dialogStage.setTitle("Edit Movie");
            if (titleField != null) titleField.setText(this.movie.getTitle());
            if (directorField != null) directorField.setText(this.movie.getDirector());
            if (yearField != null) yearField.setText(this.movie.getReleaseYear() != null ? String.valueOf(this.movie.getReleaseYear()) : "");
            if (genreField != null) genreField.setText(this.movie.getGenre());
        } else { // New movie or movie with null ID
            this.movie = new Movie(); // Ensure movie object is not null for new entries
            if (titleLabel != null) titleLabel.setText("Add New Movie");
            if (dialogStage != null) dialogStage.setTitle("Add New Movie");
            if (titleField != null) titleField.clear();
            if (directorField != null) directorField.clear();
            if (yearField != null) yearField.clear();
            if (genreField != null) genreField.clear();
        }
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        this.movie.setTitle(titleField.getText());
        this.movie.setDirector(directorField.getText());
        this.movie.setGenre(genreField.getText());
        try {
            if (yearField.getText() != null && !yearField.getText().trim().isEmpty()) {
                this.movie.setReleaseYear(Integer.parseInt(yearField.getText().trim()));
            } else {
                this.movie.setReleaseYear(null); // Allow null year
            }
        } catch (NumberFormatException e) {
            setErrorMessage("Invalid year format. Please enter a number.");
            return;
        }

        try {
            if (this.movie.getId() == null) { 
                HttpUtil.addMovie(this.movie);
            } else {
                HttpUtil.updateMovie(this.movie); 
            }
            okClicked = true;
            if (dialogStage != null) {
                dialogStage.close();
            }
        } catch (Exception e) {
            setErrorMessage("Failed to save movie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private boolean isInputValid() {
        StringBuilder errorMessages = new StringBuilder();
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessages.append("Title is required.\n");
        }
        if (yearField.getText() != null && !yearField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(yearField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessages.append("Release year must be a valid number (e.g., 2023).\n");
            }
        }
        if (errorMessages.length() == 0) {
            setErrorMessage("");
            return true;
        } else {
            setErrorMessage("Please correct the following errors:\n" + errorMessages.toString());
            return false;
        }
    }

    private void setErrorMessage(String message) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(message);
        } else {
            System.err.println("MovieForm Error: " + message);
        }
    }
}