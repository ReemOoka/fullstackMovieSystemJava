package com.example.controller;

import com.example.model.User;
import com.example.model.Role;
import com.example.util.HttpUtil;
import com.example.util.SessionManager; 

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType; 
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional; 
import java.util.Set;
import java.util.concurrent.Callable; 
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Long> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> rolesColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;
    @FXML
    private Button backToMoviesButton;

    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        userTable.setItems(userList);
        loadUsers();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        rolesColumn.setCellValueFactory(cellData -> {
            Set<Role> roles = cellData.getValue().getRoles();
            if (roles == null || roles.isEmpty()) {
                return new SimpleStringProperty("N/A");
            }
            String rolesString = roles.stream()
                    .map(Role::name)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(rolesString);
        });

        actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(deleteButton);

            {
                pane.setSpacing(5);
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    User currentUser = SessionManager.getCurrentUser();
    
                    if (currentUser != null && currentUser.getId() != null
                            && currentUser.getId().equals(user.getId())) {
                        deleteButton.setDisable(true);
                    } else {
                        deleteButton.setDisable(false);
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadUsers() {
        executeTask(
                HttpUtil::getAllUsers,
                usersFromServer -> {
                    if (usersFromServer != null) {
                        userList.setAll(usersFromServer);
                    } else {
                        userList.clear();
                    }
                    userTable.refresh(); 
                },
                ex -> showAlert("Error Loading Users",
                        "Failed to load users: " + (ex != null ? ex.getMessage() : "Unknown error"),
                        Alert.AlertType.ERROR));
    }

    private void handleDeleteUser(User user) {
        if (user == null || user.getId() == null) {
            showAlert("Error", "Cannot delete user: user data is invalid.", Alert.AlertType.ERROR);
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(user.getId())) {
            showAlert("Action Not Allowed", "You cannot delete your own account from the admin dashboard.",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete User: " + user.getUsername());
        confirmation.setContentText("Are you sure you want to delete this user? This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            executeVoidTask(
                    () -> { 
                        HttpUtil.deleteUser(user.getId());
                        return null; 
                    },
                    () -> { 
                        showAlert("Success", "User '" + user.getUsername() + "' deleted successfully.",
                                Alert.AlertType.INFORMATION);
                        loadUsers(); 
                    },
                    ex -> showAlert("Deletion Error", "Failed to delete user '" + user.getUsername() + "': "
                            + (ex != null ? ex.getMessage() : "Unknown error"), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleBackToMoviesButtonAction() {
        try {
            Stage currentStage = (Stage) backToMoviesButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not return to movie view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private <T> void executeTask(Callable<List<T>> taskAction, java.util.function.Consumer<List<T>> onSuccess,
            java.util.function.Consumer<Throwable> onError) {
        javafx.concurrent.Task<List<T>> task = new javafx.concurrent.Task<>() {
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

    private void executeVoidTask(Callable<Void> taskAction, Runnable onSuccess,
            java.util.function.Consumer<Throwable> onError) {
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                taskAction.call();
                return null;
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

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            if (userTable.getScene() != null && userTable.getScene().getWindow() != null) {
                alert.initOwner(userTable.getScene().getWindow());
            }
            alert.showAndWait();
        });
    }
}