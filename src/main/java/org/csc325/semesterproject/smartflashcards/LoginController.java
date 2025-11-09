package org.csc325.semesterproject.smartflashcards;

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
import com.google.firebase.auth.UserRecord;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

        try {
            FXMLLoader registration = new FXMLLoader(getClass().getResource("registration_screen.fxml"));
            Parent root = registration.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            System.out.println("Error loading registration screen.");
        }
    }


    public void signIn() {
        //Validation : check for Empty username and password text field
        if (userInputField.getText() == null || userInputField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText("Username cannot be empty. Please try again.");
            alert.showAndWait();
            return;
        }

        if (passwordInputField.getText() == null || passwordInputField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText("Password cannot be empty. Please try again.");
            alert.showAndWait();
            return;
        }

        ApiFuture<QuerySnapshot> future =  FlashcardApplication.fstore.collection("Passwords").get();
        List<QueryDocumentSnapshot> documents;
        boolean passwordMatch = false;
        boolean userMatch = false;
        try
        {
            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Getting (reading) data from firabase database....");

                for (QueryDocumentSnapshot document : documents) {
                    if (document.getData().get("Password").equals(passwordInputField.getText())) {
                        System.out.println(document.getData().get("Password"));
                        passwordMatch = true;
                    }
                    else{
                        System.out.println("password wrong");
                    }
                    if(document.getId().equals(userInputField.getText())){
                        System.out.println(document.getId());
                        userMatch = true;
                    }
                    else{
                        System.out.println("username wrong");
                    }
                    if(passwordMatch == true && userMatch == true){
                        try {
                            FXMLLoader registration = new FXMLLoader(getClass().getResource("registration_screen.fxml"));
                            Parent root = registration.load();

                            Scene currentScene = rootVbox.getScene();
                            currentScene.setRoot(root);
                            break;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                // show alert if no match found
                if (!(passwordMatch && userMatch)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid username or password. Please try again.");
                    alert.showAndWait();
                }
            }
            else
            {
                System.out.println("No data");
            }

        }
        catch (InterruptedException | ExecutionException ex)
        {
            ex.printStackTrace();
        }
    }

    @FXML
    public void loginButton(){
       signIn();
    }
}
