package org.csc325.semesterproject.smartflashcards;
import javafx.scene.Node;

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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;

import java.util.HashMap;
import java.util.Map;

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

    // Popup components
    static private VBox content = new VBox();
    static private HBox buttonsHBox = new HBox();
    static private Button createButton = new Button("Create");
    static private Button closeButton = new Button("Close");
    static private TextField setField = new TextField();
    static private PopOver popover = new PopOver(content);
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
        createButton.setOnAction(e -> {

            String name = setField.getText().trim();
            if (name.isEmpty()) return;

            createNewSetFirestore(name);

            popover.hide();
            setField.clear();

            refreshSetDropdown();
            setDropdown.setValue(name);   // auto-select new set
            FlashcardApplication.currentSet = name;
        });

        closeButton.setOnAction(e -> popover.hide());
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
    private void handleSetChange(ActionEvent event) {
        String selected = setDropdown.getValue();

        if (selected == null) return;

        if (selected.equals("-Create New Set-")) {
            popover.show(setDropdown);
            return;
        }

        if (!selected.equals("-No Set Selected-")) {
            FlashcardApplication.currentSet = selected;
        }
    }


    /** Scene Switching */
    @FXML
    private void handleCreate(MouseEvent event) {
        switchScene(event, "createFlashcard.fxml");
    }

    @FXML
   //private void handleStudy(MouseEvent event) {switchScene(event, "study_screen.fxml");} old version resizable
   private void handleStudy(MouseEvent event) { // study window nonresizable
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("study_screen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set fixed window size  for the study screen
            Scene scene = new Scene(root, 921, 685); // Window size to study screen only
            stage.setScene(scene);

            stage.setResizable(false);   //  only applied to this study  screen
            stage.centerOnScreen();

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePlay(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("play_landing.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            // Get the current stage
            Stage stage = (Stage) ((VBox) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        FlashcardApplication.currentUser = null;
        try {
            FXMLLoader registration = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = registration.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void switchScene(MouseEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);// Restore resizable for ALL screens except study
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Delete a set and its flashcards */
    @FXML
    private void removeSet(ActionEvent event) {

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
}
