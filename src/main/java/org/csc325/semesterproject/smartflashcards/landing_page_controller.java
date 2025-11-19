package org.csc325.semesterproject.smartflashcards;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import org.controlsfx.control.PopOver;

import java.util.HashMap;
import java.util.Map;

public class landing_page_controller {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ComboBox<String> setDropdown;

    @FXML
    private Label totalSetsLabel; // For "Total Sets" stat box

    // Temporary input for new sets
    private TextField setField = new TextField();

    @FXML
    public void initialize() {
        // Display current user
        String currentUser = FlashcardApplication.currentUser;
        if (currentUser != null && !currentUser.isEmpty()) {
            welcomeLabel.setText("Welcome, " + currentUser + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }

        ObservableList<String> sets = fetchUserSets();

        // Update total sets label
        totalSetsLabel.setText(String.valueOf(sets.size()));

        if (sets.isEmpty()) {
            // No sets: prompt user to create and switch to Create tab
            setDropdown.setPromptText("Create List");
            switchSceneToCreate();
        } else {
            // At least one set exists: show sets and prompt
            setDropdown.setItems(sets);

            if (FlashcardApplication.currentSet != null && !FlashcardApplication.currentSet.isEmpty()
                    && sets.contains(FlashcardApplication.currentSet)) {
                // Show previously selected set
                setDropdown.setValue(FlashcardApplication.currentSet);
            } else {
                // No set selected yet
                setDropdown.setValue(null);
                setDropdown.setPromptText("Select List");
            }
        }
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

        if (createdPopOver == false) {
            newHBox.setSpacing(60);
            newHBox.getChildren().addAll(
                    createButton,
                    closeButton);

            content.setSpacing(10);

            content.setPadding(new Insets(10));

            content.getChildren().addAll(
                    new Label("New Set"),
                    setField,
                    newHBox);
            createdPopOver = true;
        }
        createButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                setDropdown.setItems(setSets());
                String x = setField.getText();
                createNewSet();
                setDropdown.setItems(setSets());
                setDropdown.getSelectionModel().selectFirst();
                createNewSet();
                setDropdown.setItems(setSets());
                setDropdown.getSelectionModel().selectFirst();
                popover.hide();
                setDropdown.setValue(x);
            }
        });

        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                popover.hide();
                setDropdown.getSelectionModel().selectFirst();
            }
        });

    }

    private ObservableList<String> fetchUserSets() {
        ObservableList<String> sets = FXCollections.observableArrayList();
        try {
            Iterable<CollectionReference> collections = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .listCollections();
            for (CollectionReference collection : collections) {
                sets.add(collection.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sets;
    }

    @FXML
    private void handleSetChange(ActionEvent event) {
        String selectedSet = setDropdown.getValue();
        if (selectedSet != null && !selectedSet.isEmpty()) {
            FlashcardApplication.currentSet = selectedSet;
        }
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

    // Directly switch to Create Flashcard tab if no sets exist
    private void switchSceneToCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("createFlashcard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) setDropdown.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewSet() {
        String newSetName = "NewSet_" + System.currentTimeMillis(); // placeholder if needed
        if (!setField.getText().isEmpty()) {
            newSetName = setField.getText();
        }

        DocumentReference docRef = FlashcardApplication.fstore
                .collection("Users")
                .document(FlashcardApplication.currentUser)
                .collection(newSetName)
                .document("exists_placeholder_doc");

        Map<String, Boolean> data = new HashMap<>();
        data.put("exists", true);

        try {
            ApiFuture<WriteResult> result = docRef.set(data);
            result.get(); // wait for write to complete
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
