package com.example.controller;

import com.example.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import java.io.IOException;

public class LandingPageController {

    @FXML
    private Button continueButton;

    @FXML
    private void handleContinueAction() {
        try {
            Main.getInstance().showLoginView();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load the login page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}