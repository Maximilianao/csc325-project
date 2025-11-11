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

import javafx.scene.control.Alert;

public class createFlashcardController {

    @FXML
    private TextField wordField;
    @FXML
    private TextField defField;
    @FXML
    private TextField setField;
    @FXML
    private Button createFlashcardButton;

    public void createNewFlashcard() {
        DocumentReference docRef = FlashcardApplication.fstore.collection("Users").document(FlashcardApplication.currentUser).collection(setField.getText()).document(wordField.getText());
        //DocumentReference docRef = FlashcardApplication.fstore.collection("Users").document("test");


        Map<String, Object> data = new HashMap<>();
        data.put("Definition", defField.getText());

        //asynchronously write data
        //ApiFuture<WriteResult> writeResult = docRef.set(setField.getText());
        ApiFuture<WriteResult> result = docRef.set(data);
    }

    @FXML
    void createFlashcardButtonPressed(){
        createNewFlashcard();
    }

}


