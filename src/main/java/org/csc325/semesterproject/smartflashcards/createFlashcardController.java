package org.csc325.semesterproject.smartflashcards;

import com.google.cloud.firestore.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
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

    // Table + columns
    @FXML
    private TableView<Map<String, String>> flashcardTable;
    @FXML
    private TableColumn<Map<String, String>, String> wordColumn;
    @FXML
    private TableColumn<Map<String, String>, String> defColumn;

    // Optional action buttons under table (wired via FXML)
    @FXML
    private Button editSelectedButton;
    @FXML
    private Button deleteSelectedButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Set welcome text
        String currentUser = FlashcardApplication.currentUser;
        welcomeLabel.setText("Welcome, " + (currentUser != null ? currentUser : "") + "!");

        // Existing initialization
        Platform.runLater(() -> {
            refreshSets();
            setupFlashcardTable();
            refreshFlashcards();
        });
    }


    /**
     * Refresh list of sets (collection names) for current user.
     */
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

        // Update ComboBox on FX thread
        Platform.runLater(() -> {
            String prev = setDropdown.getValue();
            setDropdown.setItems(sets);

            // try to keep previous selection if it still exists
            if (prev != null && sets.contains(prev)) {
                setDropdown.setValue(prev);
                FlashcardApplication.currentSet = prev;
                currentSetLabel.setText("Current Set: " + prev);
            } else if (!sets.isEmpty() && !sets.get(0).equals("Create Set")) {
                setDropdown.getSelectionModel().selectFirst();
                FlashcardApplication.currentSet = setDropdown.getValue();
                currentSetLabel.setText("Current Set: " + FlashcardApplication.currentSet);
            } else {
                FlashcardApplication.currentSet = null;
                currentSetLabel.setText("No set selected");
            }

            // also refresh cards for new selection
            refreshFlashcards();
        });
    }

    @FXML
    private void handleSetChange() {
        String selected = setDropdown.getValue();
        if (selected != null && !selected.isEmpty()) {
            FlashcardApplication.currentSet = selected;
            currentSetLabel.setText("Current Set: " + selected);
        } else {
            FlashcardApplication.currentSet = null;
            currentSetLabel.setText("No set selected");
        }
        refreshFlashcards();
    }

    /**
     * Create a new set by creating a single metadata document inside the collection:
     * collection -> document "_meta" { type: "metadata", createdAt: ... }
     *
     * This ensures Firestore creates the collection while avoiding a fake/visible flashcard row.
     */
    @FXML
    private void handleAddSet() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Set");
        dialog.setHeaderText(null);
        dialog.setContentText("Set name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name == null || name.trim().isEmpty()) return;
            name = name.trim();
            try {
                DocumentReference metaRef = FlashcardApplication.fstore
                        .collection("Users")
                        .document(FlashcardApplication.currentUser)
                        .collection(name)
                        .document("_meta");

                Map<String, Object> data = new HashMap<>();
                data.put("type", "metadata");
                data.put("createdAt", System.currentTimeMillis());
                metaRef.set(data);

            } catch (Exception e) {
                e.printStackTrace();
            }
            final String selectedName = name;
            // Update UI after creating
            refreshSets();
            Platform.runLater(() -> {
                setDropdown.setValue(selectedName);
                FlashcardApplication.currentSet = selectedName;
                currentSetLabel.setText("Current Set: " + selectedName);
                refreshFlashcards();
            });
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

                } catch (Exception e) {
                    e.printStackTrace();
                }

                refreshSets();
            }
        });
    }

    @FXML
    private void createFlashcardButtonPressed() {
        if (FlashcardApplication.currentSet == null) return;

        String word = wordField.getText();
        String definition = defField.getText();
        if (word == null || word.trim().isEmpty()) return;

        word = word.trim();

        try {
            DocumentReference docRef = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .collection(FlashcardApplication.currentSet)
                    .document(word);

            Map<String, Object> data = new HashMap<>();
            data.put("Definition", definition == null ? "" : definition);
            docRef.set(data);

            wordField.clear();
            defField.clear();

            refreshFlashcards();

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

    /* -----------------------
       Table setup + operations
       ----------------------- */

    private void setupFlashcardTable() {
        // Make sure the columns show the Word and Definition
        wordColumn.setCellValueFactory(cell -> {
            Map<String, String> row = cell.getValue();
            return new SimpleStringProperty(row.getOrDefault("Word", ""));
        });

        defColumn.setCellValueFactory(cell -> {
            Map<String, String> row = cell.getValue();
            return new SimpleStringProperty(row.getOrDefault("Definition", ""));
        });

        // Double-click to edit and context menu
        flashcardTable.setRowFactory(tv -> {
            TableRow<Map<String, String>> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Edit");
            MenuItem deleteItem = new MenuItem("Delete");
            rowMenu.getItems().addAll(editItem, deleteItem);

            editItem.setOnAction(e -> {
                Map<String, String> item = row.getItem();
                if (item != null) openEditDialog(item.get("Word"), item.get("Definition"));
            });

            deleteItem.setOnAction(e -> {
                Map<String, String> item = row.getItem();
                if (item != null) confirmAndDeleteFlashcard(item.get("Word"));
            });

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                        Map<String, String> clicked = row.getItem();
                        openEditDialog(clicked.get("Word"), clicked.get("Definition"));
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        rowMenu.show(row, event.getScreenX(), event.getScreenY());
                    }
                }
            });

            // hide context menu for empty rows
            row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(rowMenu);
                }
            });

            return row;
        });

        // Buttons functionality (if user selects the row and clicks button)
        if (editSelectedButton != null) {
            editSelectedButton.setOnAction(e -> {
                Map<String, String> selected = flashcardTable.getSelectionModel().getSelectedItem();
                if (selected != null) openEditDialog(selected.get("Word"), selected.get("Definition"));
            });
        }

        if (deleteSelectedButton != null) {
            deleteSelectedButton.setOnAction(e -> {
                Map<String, String> selected = flashcardTable.getSelectionModel().getSelectedItem();
                if (selected != null) confirmAndDeleteFlashcard(selected.get("Word"));
            });
        }
    }

    /**
     * Refresh flashcards in current set.
     * Skips system docs:
     *  - documents with id starting with "exists"
     *  - documents named "_meta"
     *  - documents with a field type == "metadata"
     */
    private void refreshFlashcards() {
        if (FlashcardApplication.currentSet == null) {
            if (flashcardTable != null) {
                Platform.runLater(() -> flashcardTable.getItems().clear());
            }
            return;
        }

        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        try {
            Iterable<DocumentReference> docs = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .collection(FlashcardApplication.currentSet)
                    .listDocuments();

            for (DocumentReference doc : docs) {
                // Skip placeholder/system docs
                String docId = doc.getId();
                if (docId == null) continue;
                if (docId.startsWith("exists") || "_meta".equals(docId)) continue;

                // fetch document snapshot to check for metadata type flag
                DocumentSnapshot snap = doc.get().get();
                if (snap.exists()) {
                    // skip any doc explicitly labelled metadata
                    Object t = snap.get("type");
                    if (t != null && "metadata".equals(t.toString())) continue;

                    Map<String, String> fc = new HashMap<>();
                    fc.put("Word", docId);
                    String def = snap.contains("Definition") ? snap.getString("Definition") : "";
                    fc.put("Definition", def != null ? def : "");
                    data.add(fc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> flashcardTable.setItems(data));
    }

    /* -----------------------
       Edit / Delete helpers
       ----------------------- */

    private void openEditDialog(String word, String currentDefinition) {
        if (word == null) return;

        // Use TextInputDialog to match existing app dialog style
        TextInputDialog editDialog = new TextInputDialog(currentDefinition);
        editDialog.setTitle("Edit Flashcard");
        editDialog.setHeaderText("Editing: " + word);
        editDialog.setContentText("Update definition:");

        editDialog.showAndWait().ifPresent(newDef -> {
            try {
                FlashcardApplication.fstore
                        .collection("Users")
                        .document(FlashcardApplication.currentUser)
                        .collection(FlashcardApplication.currentSet)
                        .document(word)
                        .update("Definition", newDef);

                refreshFlashcards();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void confirmAndDeleteFlashcard(String word) {
        if (word == null) return;

        Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
        deleteAlert.setTitle("Delete Flashcard");
        deleteAlert.setHeaderText(null);
        deleteAlert.setContentText("Are you sure you want to delete the flashcard \"" + word + "\"?");
        deleteAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FlashcardApplication.fstore
                            .collection("Users")
                            .document(FlashcardApplication.currentUser)
                            .collection(FlashcardApplication.currentSet)
                            .document(word)
                            .delete();

                    refreshFlashcards();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
