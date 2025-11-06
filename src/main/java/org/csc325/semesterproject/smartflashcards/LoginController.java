package org.csc325.semesterproject.smartflashcards;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML
    private TextField userInputField;
    @FXML
    private Label usernameErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private VBox rootVbox;
    @FXML
    private PasswordField passwordInputField;

    @FXML
    protected void initialize(){
        Platform.runLater(()-> rootVbox.requestFocus());

        rootVbox.setOnMousePressed(_ -> rootVbox.requestFocus());

        userInputField.textProperty().addListener(userInputListener);
        passwordInputField.textProperty().addListener(passwordInputListener);
    }

    @FXML
    public void viewRegistrationScreen() {
        try {
            FXMLLoader registration = new FXMLLoader(getClass().getResource("registration_screen.fxml"));
            Parent root = registration.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);

            userInputField.textProperty().removeListener(userInputListener);
            passwordInputField.textProperty().removeListener(userInputListener);
        } catch (Exception e) {
            System.out.println("Error loading registration screen.");
        }
    }

    private final ChangeListener<String> userInputListener = (_, _, newValue) -> {
        if (!newValue.isEmpty()){
            usernameErrorLabel.setText("");
        }
        else {
            usernameErrorLabel.setText("Username cannot be empty");
        }
    };

    private final ChangeListener<String> passwordInputListener = (_, _, newValue) -> {
        if (!newValue.isEmpty()){
            passwordErrorLabel.setText("");
        }
        else {
            passwordErrorLabel.setText("Password cannot be empty");
        }
    };
}
