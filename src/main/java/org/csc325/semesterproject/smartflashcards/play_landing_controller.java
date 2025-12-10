package org.csc325.semesterproject.smartflashcards;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class play_landing_controller {

    @FXML
    public Button backButton;
    @FXML
    private VBox rootVbox;

    @FXML
    private void handleBack() {
        try {
            FXMLLoader registration = new FXMLLoader(getClass().getResource("landing_Page.fxml"));
            Parent root = registration.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void launchGame1(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("memory_game.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            // Get current window
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void launchGame2() {
        System.out.println("Game 2 placeholder clicked.");
    }

    @FXML
    public void launchGame3() {
        System.out.println("Game 3 placeholder clicked.");
    }
}
