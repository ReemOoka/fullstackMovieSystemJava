package com.example.controller;

import com.example.Main;
import com.example.model.User;
import com.example.util.HttpUtil;
import com.example.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        // Prime cookies when the login view is initialized
        // This helps in getting the CSRF token before the first login attempt
        HttpUtil.primeCookies();
    }

    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        try {
            User loggedInUser = HttpUtil.login(username, password);

            if (loggedInUser != null && loggedInUser.getUsername() != null) { // Check if username is not null
                // Login successful
                messageLabel.setText("Login successful!");
                messageLabel.setTextFill(Color.GREEN);
                SessionManager.setCurrentUser(loggedInUser); // Use SessionManager
                SessionManager.setCsrfToken(HttpUtil.getCsrfToken()); // Store CSRF token in SessionManager

                Main.getInstance().showMovieView();
            } else {
                // Login failed
                messageLabel.setText("Login failed. Please check your username and password.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            String errorMessage = "Login error: ";
            if (e.getMessage() != null && e.getMessage().contains("Login failed. Server response:")) {
                errorMessage = "Login failed. Invalid credentials.";
            } else if (e.getMessage() != null) {
                errorMessage += e.getMessage();
            } else {
                errorMessage += "An unexpected error occurred.";
            }
            messageLabel.setText(errorMessage);
            messageLabel.setTextFill(Color.RED);
            System.err.println("Login exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterLinkAction() {
        try {
            Main.getInstance().showRegistrationView();
        } catch (Exception e) {
            messageLabel.setText("Error loading registration: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }
}