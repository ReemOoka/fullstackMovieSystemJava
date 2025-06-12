package com.example.controller;

import com.example.Main;
import com.example.util.HttpUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RegistrationController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    @FXML
    private void handleRegisterButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        try {
            HttpUtil.register(username, password);

            messageLabel.setText("Registration successful! Please login.");
            messageLabel.setTextFill(Color.GREEN);

            // If you want to automatically navigate after successful registration,
            // you could uncomment and adapt the following:
            // Stage stage = (Stage) messageLabel.getScene().getWindow();
            // if (stage != null) {
            // stage.close();
            // }
            // Main.getInstance().showLoginView();

        } catch (Exception e) {
            messageLabel.setText("Registration error: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoginLinkAction() {
        try {
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
            Main.getInstance().showLoginView();
        } catch (Exception e) {
            messageLabel.setText("Error loading login: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }
}