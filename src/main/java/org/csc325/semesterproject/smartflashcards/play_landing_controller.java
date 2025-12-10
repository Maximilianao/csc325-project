package org.csc325.semesterproject.smartflashcards;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    public void launchGame1() {
        try {
            FXMLLoader login = new FXMLLoader(getClass().getResource("memory_game.fxml"));
            Parent root = login.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void launchGame2() {
        System.out.println("Game 2 placeholder clicked.");
    }

    public void launchGame3() {
        try {
            FXMLLoader login = new FXMLLoader(getClass().getResource("matching_MiniGame_screen.fxml"));
            Parent root = login.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
