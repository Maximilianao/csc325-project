package org.csc325.semesterproject.smartflashcards;

import javafx.fxml.FXML;
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
    public void viewRegistrationScreen() {
    }
}
