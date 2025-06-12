package com.example.controller;

import com.example.model.Movie;
import com.example.util.HttpUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ReviewFormController {

    @FXML
    private TextArea reviewContentTextArea;

    @FXML
    private Spinner<Integer> ratingSpinner;

    @FXML
    private Button saveReviewButton;

    @FXML
    private Button cancelReviewButton;

    private Stage dialogStage;
    private Movie movie; 
    private boolean saved = false;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3); 
        ratingSpinner.setValueFactory(valueFactory);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSubmitReviewAction() {
        if (movie == null) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Movie context is missing.");
            return;
        }
        String content = reviewContentTextArea.getText();
        Integer rating = ratingSpinner.getValue();

        if (content == null || content.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Review content cannot be empty.");
            return;
        }

        com.example.dto.ReviewRequestDto reviewRequest = new com.example.dto.ReviewRequestDto();
        reviewRequest.setMovieId(movie.getId());
        reviewRequest.setContent(content);
        reviewRequest.setRating(rating);

        try {
            HttpUtil.addReview(reviewRequest);
            saved = true;
            showAlert(Alert.AlertType.INFORMATION, "Success", "Review saved successfully!");
            dialogStage.close();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save review: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelReview() {
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(dialogStage); 
        alert.showAndWait();
    }
}