package com.example.controller;

import com.example.Main;
import com.example.model.Movie;
import com.example.model.Review;
import com.example.model.User;
import com.example.model.Role;
import com.example.util.HttpUtil;
import com.example.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class MovieViewController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Movie> movieTable;
    @FXML
    private TableColumn<Movie, Long> idColumn;
    @FXML
    private TableColumn<Movie, String> titleColumn;
    @FXML
    private TableColumn<Movie, String> directorColumn;
    @FXML
    private TableColumn<Movie, Integer> yearColumn;
    @FXML
    private TableColumn<Movie, String> genreColumn;
    @FXML
    private ListView<Review> reviewListView;
    @FXML
    private Button addMovieButton;
    @FXML
    private Button editMovieButton;
    @FXML
    private Button deleteMovieButton;
    @FXML
    private Button addReviewButton;
    @FXML
    private Button deleteReviewButton;
    @FXML
    private Button adminDashboardButton;
    @FXML
    private Button logoutButton;

    private final ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private FilteredList<Movie> filteredMovieList;
    private final ObservableList<Review> reviewList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupMovieTableColumns();
        setupReviewListView();
        setupSearchFieldListener();
        updateWelcomeMessage();
        updateButtonVisibility();
        loadMovies();

        movieTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    loadReviewsForMovie(newSelection);
                    updateButtonVisibility();
                });

        reviewListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateButtonVisibility());
    }

    private void setupMovieTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        directorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        filteredMovieList = new FilteredList<>(movieList, p -> true);
        movieTable.setItems(filteredMovieList);
    }

    private void setupReviewListView() {
        reviewListView.setItems(reviewList);
        reviewListView.setCellFactory(param -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("User : %s\nRating : (%d/5)\nReview : %s",
                            item.getUsername() != null ? item.getUsername() : "Anonymous",
                            item.getRating(),
                            item.getContent()));
                    setWrapText(true);
                }
            }
        });
    }

    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMovieList.setPredicate(movie -> {
                if (newValue == null || newValue.trim().isEmpty())
                    return true;
                String lowerCaseFilter = newValue.trim().toLowerCase();
                if (movie.getTitle() != null && movie.getTitle().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (movie.getDirector() != null && movie.getDirector().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(lowerCaseFilter))
                    return true;
                return movie.getReleaseYear() != null
                        && String.valueOf(movie.getReleaseYear()).contains(lowerCaseFilter);
            });
        });
    }

    private void updateWelcomeMessage() {
        User currentUser = SessionManager.getCurrentUser();
        welcomeLabel.setText(
                currentUser != null && currentUser.getUsername() != null ? "Welcome, " + currentUser.getUsername() + "!"
                        : "Welcome!");
    }

    private void updateButtonVisibility() {
        User currentUser = SessionManager.getCurrentUser();
        boolean isLoggedIn = currentUser != null;
        boolean isAdmin = isLoggedIn && currentUser.getRoles() != null && currentUser.getRoles().contains(Role.ADMIN);
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();

        setButtonVisibility(adminDashboardButton, isAdmin);
        setButtonVisibility(addMovieButton, isAdmin);
        setButtonVisibility(editMovieButton, isAdmin && selectedMovie != null);
        setButtonVisibility(deleteMovieButton, isAdmin && selectedMovie != null);
        setButtonVisibility(addReviewButton, isLoggedIn && selectedMovie != null);

        boolean canDeleteReview = false;
        if (selectedReview != null && isLoggedIn) {
            Long reviewUserId = selectedReview.getUserId();
            Long currentUserId = currentUser.getId();
            if (isAdmin || (reviewUserId != null && reviewUserId.equals(currentUserId))) {
                canDeleteReview = true;
            }
        }
        setButtonVisibility(deleteReviewButton, canDeleteReview);
        setButtonVisibility(logoutButton, isLoggedIn);
    }

    private void setButtonVisibility(Button button, boolean visible) {
        if (button != null) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
    }

    public void loadMovies() {
        executeTask(
                HttpUtil::getAllMovies,
                moviesFromServer -> {
                    movieList.setAll(moviesFromServer != null ? moviesFromServer : FXCollections.emptyObservableList());
                    movieTable.getSelectionModel().clearSelection();
                    reviewList.clear();
                    updateButtonVisibility();
                },
                ex -> showAlert("Error Loading Movies",
                        "Failed to load movies: " + (ex != null ? ex.getMessage() : "Unknown error"),
                        Alert.AlertType.ERROR));
    }

    private <T> void executeTask(Callable<List<T>> taskAction, java.util.function.Consumer<List<T>> onSuccess,
            java.util.function.Consumer<Throwable> onError) {
        Task<List<T>> task = new Task<>() {
            @Override
            protected List<T> call() throws Exception {
                return taskAction.call();
            }
        };
        task.setOnSucceeded(event -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            if (ex != null)
                ex.printStackTrace();
            Platform.runLater(() -> onError.accept(ex));
        });
        new Thread(task).start();
    }

    private void executeTask(Callable<Void> taskAction, Runnable onSuccess,
            java.util.function.Consumer<Throwable> onError) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                taskAction.call(); // Corrected: call() returns Void, so just call the action.
                return null; // Explicitly return null for Callable<Void>
            }
        };
        task.setOnSucceeded(event -> Platform.runLater(onSuccess));
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            if (ex != null)
                ex.printStackTrace();
            Platform.runLater(() -> onError.accept(ex));
        });
        new Thread(task).start();
    }

    private void loadReviewsForMovie(Movie movie) {
        if (movie == null || movie.getId() == null) {
            reviewList.clear();
            updateButtonVisibility();
            return;
        }
        executeTask(
                () -> HttpUtil.getReviewsForMovie(movie.getId()),
                reviewsFromServer -> {
                    reviewList.setAll(
                            reviewsFromServer != null ? reviewsFromServer : FXCollections.emptyObservableList());
                    updateButtonVisibility();
                },
                ex -> showAlert("Error Loading Reviews",
                        "Failed to load reviews: " + (ex != null ? ex.getMessage() : "Unknown error"),
                        Alert.AlertType.ERROR));
    }

    private boolean showMovieFormDialog(String dialogTitle, Movie movieToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/movie_form.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(dialogTitle);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            getDialogOwner().ifPresent(dialogStage::initOwner);
            Scene scene = new Scene(page);

            dialogStage.setScene(scene);
            MovieFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMovie(movieToEdit);
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Dialog Error", "Could not open movie form: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    private void handleAddMovieButtonAction() {
        if (showMovieFormDialog("Add New Movie", null)) {
            loadMovies();
        }
    }

    @FXML
    private void handleEditMovieButtonAction() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            if (showMovieFormDialog("Edit Movie", selectedMovie)) {
                loadMovies();
            }
        } else {
            showAlert("No Selection", "Please select a movie to edit.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleDeleteMovieButtonAction() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null && selectedMovie.getId() != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selectedMovie.getTitle() + "?",
                    ButtonType.YES, ButtonType.NO);
            getDialogOwner().ifPresent(confirm::initOwner);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    executeTask(
                            () -> {
                                HttpUtil.deleteMovie(selectedMovie.getId());
                                return null;
                            },
                            () -> {
                                loadMovies();
                                showAlert("Success", "Movie deleted.", Alert.AlertType.INFORMATION);
                            },
                            ex -> showAlert("Deletion Error",
                                    "Failed to delete movie: " + (ex != null ? ex.getMessage() : "Unknown error"),
                                    Alert.AlertType.ERROR));
                }
            });
        } else {
            showAlert("No Selection", "Please select a movie to delete.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleAddReviewButtonAction() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert("No Movie Selected", "Select a movie to review.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/review_form.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Review for: " + selectedMovie.getTitle());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            getDialogOwner().ifPresent(dialogStage::initOwner);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            ReviewFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMovie(selectedMovie);
            dialogStage.showAndWait();
            if (controller.isSaved()) {
                loadReviewsForMovie(selectedMovie);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Dialog Error", "Could not open review form: " + (e != null ? e.getMessage() : "Unknown error"),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteReviewButtonAction() {
        Review selectedReview = reviewListView.getSelectionModel().getSelectedItem();
        Movie currentMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedReview != null && selectedReview.getId() != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this review?", ButtonType.YES,
                    ButtonType.NO);
            getDialogOwner().ifPresent(confirm::initOwner);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    executeTask(
                            () -> {
                                HttpUtil.deleteReview(selectedReview.getId());
                                return null;
                            },
                            () -> {
                                if (currentMovie != null)
                                    loadReviewsForMovie(currentMovie);
                                else
                                    reviewList.remove(selectedReview); // Fallback if movie context is lost
                                showAlert("Success", "Review deleted.", Alert.AlertType.INFORMATION);
                            },
                            ex -> showAlert("Deletion Error",
                                    "Failed to delete review: " + (ex != null ? ex.getMessage() : "Unknown error"),
                                    Alert.AlertType.ERROR));
                }
            });
        } else {
            showAlert("No Selection", "Please select a review to delete.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleLogoutButtonAction() {
        SessionManager.clearSession();
        try {
            Main mainApp = Main.getInstance();
            if (mainApp != null)
                mainApp.showLoginView();
            else
                showAlert("Application Error", "Main application instance not available.", Alert.AlertType.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to go to login: " + (e != null ? e.getMessage() : "Unknown error"),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAdminDashboardButtonAction() {
        try {
            Main mainApp = Main.getInstance();
            if (mainApp != null) {
                mainApp.showAdminDashboard();
            } else {
                showAlert("Application Error", "Main application instance not available.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error",
                    "Could not open Admin Dashboard: " + (e != null ? e.getMessage() : "Unknown error"),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred while opening Admin Dashboard: "
                    + (e != null ? e.getMessage() : "Unknown error"), Alert.AlertType.ERROR);
        }
    }

    private Optional<Window> getDialogOwner() {
        if (Main.getInstance() != null && Main.getInstance().getPrimaryStage() != null) {
            return Optional.of(Main.getInstance().getPrimaryStage());
        }
        return Optional.ofNullable(
                welcomeLabel != null && welcomeLabel.getScene() != null ? welcomeLabel.getScene().getWindow() : null);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            getDialogOwner().ifPresent(alert::initOwner);
            alert.showAndWait();
        });
    }
}