package org.csc325.semesterproject.smartflashcards;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    @FXML
    public void launchGame3(MouseEvent event) {
        System.out.println("Launching Matching Terms with Definitions Game...");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("matching_MiniGame_screen.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Fixed size ONLY for the mini-game
            Scene scene = new Scene(root, 1116, 674);
            stage.setScene(scene);

            //  Lock resizing ONLY for this screen
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
