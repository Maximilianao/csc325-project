package org.csc325.semesterproject.smartflashcards;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;

public class landing_page_controller {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ComboBox<String> setDropdown;

    @FXML
    private Button removeButton;

    static private VBox content = new VBox();
    static private HBox newHBox = new HBox();
    static private Button createButton = new Button("Create");
    static private Button closeButton = new Button("Close");

    static private TextField setField = new TextField();

    static private PopOver popover = new PopOver(content);
    static private boolean createdPopOver = false;

    private ObservableList<String> setSets() {
        ObservableList<String> sets = FXCollections.observableArrayList();
        Iterable<CollectionReference> collections = FlashcardApplication.fstore.collection("Users")
                .document(FlashcardApplication.currentUser).listCollections();
        sets.add("-No Set Selected-");
        for (CollectionReference collection : collections) {
            sets.add(collection.getId());
        }
        sets.add("-Create New Set-");
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

    @FXML
    private void handleSetChange(ActionEvent event) {
        String selectedSet = setDropdown.getValue();
        System.out.println("Current set changed to: " + selectedSet);
        FlashcardApplication.currentSet = selectedSet;

        if (setDropdown.getValue().equals("-Create New Set-")) {
            popover.show(setDropdown);
            System.out.println("popup shown");
        }

    }

    @FXML
    private void removeSet(ActionEvent event) {
        CollectionReference future =  FlashcardApplication.fstore.collection("Users").document(FlashcardApplication.currentUser).collection(FlashcardApplication.currentSet);
        Iterable<DocumentReference> documents = future.listDocuments();
        if(!FlashcardApplication.currentSet.equals("-Create New Set-") && !FlashcardApplication.currentSet.equals("-No Set Selected-")) {
            for (DocumentReference document : documents) {
                document.delete();
            }
        }
        setDropdown.setItems(setSets());
        setDropdown.getSelectionModel().selectFirst();
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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private void createNewSet() {
        DocumentReference docRef = FlashcardApplication.fstore.collection("Users").document(FlashcardApplication.currentUser).collection(setField.getText()).document("exists23798tfhg7989w2889vb97498hfgw97fhn29wf8hed8h9w2h899309003948h9tg");
        // DocumentReference docRef =
        // FlashcardApplication.fstore.collection("Users").document("test");

        Map<String, Boolean> data = new HashMap<>();
        data.put("exists", true);

        // asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }

}
