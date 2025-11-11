package org.csc325.semesterproject.smartflashcards;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class landing_page_controller {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Retrieve current username from FlashcardApplication
        String currentUser = FlashcardApplication.currentUser;
        if (currentUser != null && !currentUser.isEmpty()) {
            welcomeLabel.setText("Welcome, " + currentUser + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    @FXML
    private void handleCreate(javafx.scene.input.MouseEvent event) {
        switchScene(event, "create_screen.fxml");
    }

    @FXML
    private void handleStudy(javafx.scene.input.MouseEvent event) {
        switchScene(event, "study_screen.fxml");
    }

    @FXML
    private void handlePlay(javafx.scene.input.MouseEvent event) {
        switchScene(event, "play_screen.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Clear current user
        FlashcardApplication.currentUser = null;

        // Switch to login scene
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchScene(javafx.scene.input.MouseEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
