package org.csc325.semesterproject.smartflashcards;

import com.google.firebase.auth.UserRecord;
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
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;

import javafx.scene.control.Alert;



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

        emailInputField.textProperty().addListener(emailInputListener);
        userInputField.textProperty().addListener(userInputListener);
        passwordInputField.textProperty().addListener(passwordInputListener);
    }

    boolean alreadyRegistered = false;

    @FXML
    public boolean signUp() {
        // Validation check for Empty text fields:  email, username, and password
        if (emailInputField.getText().isEmpty() || userInputField.getText().isEmpty() || passwordInputField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText(null);
            alert.setContentText("Email, username, and password cannot be empty. Please try again.");
            alert.showAndWait();
            return false;
        }

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(emailInputField.getText())
                .setEmailVerified(false)
                .setPassword(passwordInputField.getText())
                //.setPhoneNumber(phoneNumberTextField.getText())
                .setUid(userInputField.getText())
                .setDisabled(false);



        UserRecord userRecord;
        try {
            userRecord = FlashcardApplication.fauth.createUser(request);
            System.out.println("Successfully created new user with Firebase Uid: " + userRecord.getUid()
                    + " check Firebase > Authentication > Users tab");
            addData();
            return true;

        } catch (FirebaseAuthException ex) {
            // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error creating a new user in the firebase");
            passwordErrorLabel.setText("Account Already Exists");
            return false;
        }

    }

    public void addData() {
        DocumentReference docRef = FlashcardApplication.fstore.collection("Passwords").document(userInputField.getText());

        Map<String, Object> data = new HashMap<>();
        data.put("Password", passwordInputField.getText());

        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }

    @FXML
    public void viewLoginScreen() {
        try {
            FXMLLoader registration = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = registration.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);

            emailInputField.textProperty().removeListener(emailInputListener);
            userInputField.textProperty().removeListener(userInputListener);
            passwordInputField.textProperty().removeListener(passwordInputListener);
        } catch (Exception e) {
            System.out.println("Error loading login screen.");
        }
    }

    @FXML
    void signUpButtonClicked(ActionEvent event) {
        signUp();
    }

    private final ChangeListener<String> emailInputListener = (_, _, newValue) -> {
        String pattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(emailInputField.getText());
        boolean emailFormat = m.matches();

        if (!newValue.isEmpty() && emailFormat) {
            emailErrorLabel.setText("");
        }
        else if (!emailFormat) {
            emailErrorLabel.setText("Enter a valid email address");
        }
        else {
            emailErrorLabel.setText("Email cannot be empty");
        }
    };

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
