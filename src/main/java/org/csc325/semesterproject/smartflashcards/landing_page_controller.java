package org.csc325.semesterproject.smartflashcards;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class landing_page_controller {

    @FXML
    private void handleCreate(ActionEvent event) {
        switchScene(event, "create_screen.fxml");
    }

    @FXML
    private void handleStudy(ActionEvent event) {
        switchScene(event, "study_screen.fxml");
    }

    @FXML
    private void handlePlay(ActionEvent event) {
        switchScene(event, "play_screen.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlFile) {
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
