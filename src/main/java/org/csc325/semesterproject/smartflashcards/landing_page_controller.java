package org.csc325.semesterproject.smartflashcards;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class landing_page_controller {
    @FXML
    private Label welcomeLabel;
    @FXML
    private ComboBox<String> setDropdown;
    @FXML
    private Label totalSetsLabel;
    @FXML
    private Button removeButton;
    @FXML
    private VBox rootVbox;
    @FXML
    private Button logoutButton;

    @FXML
    private Label accuracyLabel;

    // Popup components
    static private final VBox content = new VBox();
    static private final HBox buttonsHBox = new HBox();
    static private final Button createButton = new Button("Create");
    static private final Button closeButton = new Button("Close");
    static private final TextField setField = new TextField();
    static private final PopOver popover = new PopOver(content);
    static private boolean popoverBuilt = false;



    @FXML
    public void initialize() {

        String currentUser = FlashcardApplication.currentUser;
        welcomeLabel.setText("Welcome, " + (currentUser != null ? currentUser : "") + "!");

        refreshSetDropdown();

        // Build popup only once
        if (!popoverBuilt) {
            buttonsHBox.setSpacing(40);
            buttonsHBox.getChildren().addAll(createButton, closeButton);

            content.setSpacing(10);
            content.setPadding(new Insets(10));
            content.getChildren().addAll(
                    new Label("New Set Name:"),
                    setField,
                    buttonsHBox
            );

            popoverBuilt = true;
        }

        // Create Set Button Action
        createButton.setOnAction(_ -> {

            String name = setField.getText().trim();
            if (name.isEmpty()) return;

            createNewSetFirestore(name);

            popover.hide();
            setField.clear();

            refreshSetDropdown();
            setDropdown.setValue(name);   // auto-select new set
            FlashcardApplication.currentSet = name;
        });

        closeButton.setOnAction(_ -> popover.hide());
    }


    /** Refresh the dropdown list */
    private void refreshSetDropdown() {
        ObservableList<String> sets = FXCollections.observableArrayList();

        sets.add("-No Set Selected-");

        try {
            Iterable<CollectionReference> collections = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .listCollections();

            for (CollectionReference c : collections) {
                sets.add(c.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sets.add("-Create New Set-");

        setDropdown.setItems(sets);
        setDropdown.getSelectionModel().selectFirst();

        // update total sets count (exclude first/last entries)
        totalSetsLabel.setText(String.valueOf(sets.size() - 2));
    }


    /** Matching the create page: create a real set + _meta doc */
    private void createNewSetFirestore(String setName) {

        try {
            DocumentReference meta = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .collection(setName)
                    .document("_meta");

            Map<String, Object> info = new HashMap<>();
            info.put("exists", true);
            info.put("createdAt", System.currentTimeMillis());

            meta.set(info); // creates set and its metadata
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleSetChange() {
        String selected = setDropdown.getValue();

        if (selected == null) return;

        if (selected.equals("-Create New Set-")) {
            popover.show(setDropdown);
            return;
        }

        if (!selected.equals("-No Set Selected-")) {
            FlashcardApplication.currentSet = selected;
        }
        loadAccuracy();
    }


    /** Scene Switching */
    @FXML
    private void handleCreate() {
        try {
            FXMLLoader create = new FXMLLoader(getClass().getResource("createFlashcard.fxml"));
            Parent root = create.load();

            createFlashcardController controller = create.getController();

            //Sets the current selected set so that it is shown when study mode is shown
            controller.setSelectedSet(setDropdown.getValue());

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    // private void handleStudy(MouseEvent event) {switchScene(event,
    // "study_screen.fxml");} old version resizable
    private void handleStudy() { // This is the new version of the study window nonresizable
        try {
            FXMLLoader study = new FXMLLoader(getClass().getResource("study_screen.fxml"));
            Parent root = study.load();

            StudyController controller = study.getController();

            //Sets the current selected set so that it is shown when study mode is shown
            controller.setSelectedSet(setDropdown.getValue());

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePlay() {
        try {
            switchScene("play_landing.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        FlashcardApplication.currentUser = null;
        try {
            FXMLLoader login = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = login.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void switchScene(String fxml) {
        try {
            FXMLLoader registration = new FXMLLoader(getClass().getResource(fxml));
            Parent root = registration.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Delete a set and its flashcards */
    @FXML
    private void removeSet() {

        String selected = setDropdown.getValue();

        if (selected == null ||
                selected.equals("-No Set Selected-") ||
                selected.equals("-Create New Set-"))
            return;

        try {
            CollectionReference setRef = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .collection(selected);

            for (DocumentReference doc : setRef.listDocuments()) {
                doc.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        FlashcardApplication.currentSet = null;
        refreshSetDropdown();
    }

    private void loadAccuracy() {
        int totalAccuracy = 4;
        int currentAccuracy = 0;
        double percentage = 0.0;


        Iterable<DocumentReference> completedFlashcards = FlashcardApplication.fstore
                .collection("UserProgress")
                .document(FlashcardApplication.currentUser)
                .collection(FlashcardApplication.currentSet)
                .listDocuments();

        for (DocumentReference flashcard : completedFlashcards) {
            currentAccuracy++;
        }
        percentage = (double)(currentAccuracy) / (double)(totalAccuracy);
        percentage = percentage * 100;
        accuracyLabel.setText(percentage + "%");
    }
}
