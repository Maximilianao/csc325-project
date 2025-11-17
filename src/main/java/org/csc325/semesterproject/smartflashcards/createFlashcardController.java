package org.csc325.semesterproject.smartflashcards;

import com.google.cloud.firestore.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class createFlashcardController {

    @FXML
    private ComboBox<String> setDropdown;
    @FXML
    private Label currentSetLabel;
    @FXML
    private TextField wordField;
    @FXML
    private TextField defField;

    @FXML
    public void initialize() {
        refreshSets();
    }

    private void refreshSets() {
        ObservableList<String> sets = FXCollections.observableArrayList();
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

        if (sets.isEmpty()) {
            sets.add("Create Set");
        }

        setDropdown.setItems(sets);

        if (!sets.isEmpty() && !sets.get(0).equals("Create Set")) {
            setDropdown.getSelectionModel().selectFirst();
            FlashcardApplication.currentSet = setDropdown.getValue();
            currentSetLabel.setText("Current Set: " + FlashcardApplication.currentSet);
        } else {
            FlashcardApplication.currentSet = null;
            currentSetLabel.setText("No set selected");
        }
    }

    @FXML
    private void handleSetChange() {
        String selected = setDropdown.getValue();
        if (selected != null && !selected.isEmpty()) {
            FlashcardApplication.currentSet = selected;
            currentSetLabel.setText("Current Set: " + selected);
        }
    }

    @FXML
    private void handleAddSet() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Set");
        dialog.setHeaderText(null);
        dialog.setContentText("Set name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isEmpty()) {
                try {
                    DocumentReference docRef = FlashcardApplication.fstore
                            .collection("Users")
                            .document(FlashcardApplication.currentUser)
                            .collection(name)
                            .document("exists_placeholder");
                    Map<String, Boolean> data = new HashMap<>();
                    data.put("exists", true);
                    docRef.set(data);
                    refreshSets();
                    setDropdown.getSelectionModel().select(name);
                    FlashcardApplication.currentSet = name;
                    currentSetLabel.setText("Current Set: " + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleDeleteSet() {
        String selected = setDropdown.getValue();
        if (selected == null || selected.isEmpty()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Set");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete the set \"" + selected + "\"? This will delete all flashcards inside.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    CollectionReference setRef = FlashcardApplication.fstore
                            .collection("Users")
                            .document(FlashcardApplication.currentUser)
                            .collection(selected);

                    Iterable<DocumentReference> docs = setRef.listDocuments();
                    for (DocumentReference doc : docs) {
                        doc.delete();
                    }

                    refreshSets();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void createFlashcardButtonPressed() {
        if (FlashcardApplication.currentSet == null) return;

        try {
            DocumentReference docRef = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .collection(FlashcardApplication.currentSet)
                    .document(wordField.getText());

            Map<String, Object> data = new HashMap<>();
            data.put("Definition", defField.getText());
            docRef.set(data);

            wordField.clear();
            defField.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToLanding() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("landing_Page.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) setDropdown.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        FlashcardApplication.currentUser = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) setDropdown.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
