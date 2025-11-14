package org.csc325.semesterproject.smartflashcards;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;


import com.google.api.core.ApiFuture;
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

public class landing_page_controller {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ComboBox<String> setDropdown;
    //temporary list of flashcards
    /*private ObservableList<String> sets = FXCollections.observableArrayList(
            "Biology 101",
            "Spanish Vocabulary",
            "Java Basics",
            "History Dates"
    );*/



    private ObservableList<String> setSets(){
        ObservableList<String> sets = FXCollections.observableArrayList();
        Iterable<CollectionReference> collections = FlashcardApplication.fstore.collection("Users").document(FlashcardApplication.currentUser).listCollections();
        for (CollectionReference collection : collections) {
            sets.add(collection.getId());
        }
        return sets;
    }


    @FXML
    public void initialize() {
        // Display current user
        String currentUser = FlashcardApplication.currentUser;
        if (currentUser != null && !currentUser.isEmpty()) {
            welcomeLabel.setText("Welcome, " + currentUser + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }

        // Populate temporary sets
        setDropdown.setItems(setSets());
        setDropdown.getSelectionModel().selectFirst(); // default selection
    }

    @FXML
    private void handleSetChange(ActionEvent event) {
        String selectedSet = setDropdown.getValue();
        System.out.println("Current set changed to: " + selectedSet);
        FlashcardApplication.currentSet = selectedSet;
    }

    @FXML
    private void handleCreate(MouseEvent event) {
        switchScene(event, "createFlashcard.fxml");
    }

    @FXML
    private void handleStudy(MouseEvent event) {
        switchScene(event, "study_screen.fxml");
    }

    @FXML
    private void handlePlay(MouseEvent event) {
        switchScene(event, "play_screen.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        FlashcardApplication.currentUser = null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchScene(MouseEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
