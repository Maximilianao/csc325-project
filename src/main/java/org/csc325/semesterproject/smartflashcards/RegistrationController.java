package org.csc325.semesterproject.smartflashcards;

import com.google.firebase.auth.UserRecord;
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
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.scene.control.Alert;

import javafx.scene.control.TextArea;


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
    public boolean signUp() {
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
        } catch (Exception e) {
            System.out.println("Error loading login screen.");
        }
    }

    @FXML
    void signUpButtonClicked(ActionEvent event) {
        signUp();
    }
}
