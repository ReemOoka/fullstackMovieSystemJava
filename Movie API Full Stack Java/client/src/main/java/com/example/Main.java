package com.example;

import com.example.util.HttpUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    private static Main instance;
    private Stage primaryStage;

    public Main() {
    }

    public static Main getInstance() {
        return instance;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Movie Application");

        HttpUtil.primeCookies();
        showLandingPage();
    }

    private void loadScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void loadModalScene(String fxmlPath, String title, Stage owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            dialogStage.initOwner(owner);
        }
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public void showLandingPage() throws IOException {
        loadScene("/com/example/fxml/landing_page.fxml", "Welcome - Movie Management System");
    }

    public void showLoginView() throws IOException {
        loadScene("/com/example/fxml/login.fxml", "Login");
    }

    public void showMovieView() throws IOException {
        loadScene("/com/example/fxml/movie_view.fxml", "Movie Collection");
    }

    public void showAdminDashboard() throws IOException {
        loadModalScene("/com/example/fxml/admin_dashboard.fxml", "Admin Dashboard", primaryStage);
    }

    public void showRegistrationView() throws IOException {
        loadScene("/com/example/fxml/registration.fxml", "Register New User");
    }

    public static void main(String[] args) {
        launch(args);
    }
}