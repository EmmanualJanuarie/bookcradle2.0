package com.bookcradle.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminController {

    @FXML
    private Label adminName;

    @FXML
    private Button dashboardButton, addBookButton, userLogsButton, logoutButton;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        adminName.setText("Welcome, Admin");

        // Load default view
        loadView("AdminDashboardView.fxml");
        setActiveButton(dashboardButton);

        dashboardButton.setOnAction(e -> {
            loadView("AdminDashboardView.fxml");
            setActiveButton(dashboardButton);
        });

        addBookButton.setOnAction(e -> {
            loadView("AddBookView.fxml");
            setActiveButton(addBookButton);
        });

        userLogsButton.setOnAction(e -> {
            loadView("UserLogsView.fxml");
            setActiveButton(userLogsButton);
        });
    }

    private void loadView(String fxmlFile) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/admin/" + fxmlFile));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Optional: show error dialog or message to admin here
        }
    }

    private void setActiveButton(Button activeButton) {
        dashboardButton.getStyleClass().remove("active");
        addBookButton.getStyleClass().remove("active");
        userLogsButton.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/AuthUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene newScene = new Scene(root);
            newScene.getStylesheets().add(getClass().getResource("/styles/AuthStyle.css").toExternalForm());

            stage.setScene(newScene);
            stage.setTitle("BookCradle");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
