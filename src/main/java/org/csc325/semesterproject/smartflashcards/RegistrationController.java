package org.csc325.semesterproject.smartflashcards;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegistrationController {
    @FXML
    private TextField userInputField;
    @FXML
    private Label usernameErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private VBox rootVbox;
    @FXML
    private Button signUpButton;
    @FXML
    private PasswordField passwordInputField;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private TextField emailInputField;

    @FXML
    protected void initialize(){
        Platform.runLater(()-> rootVbox.requestFocus());

        rootVbox.setOnMousePressed(_ -> rootVbox.requestFocus());
    }

    @FXML
    public void signUp() {
    }

    @FXML
    public void viewLoginScreen() {
        try {
            FXMLLoader registration = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = registration.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            System.out.println("Error loading login screen.");
        }
    }
}
