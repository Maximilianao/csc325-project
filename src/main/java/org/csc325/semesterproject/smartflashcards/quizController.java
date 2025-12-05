package org.csc325.semesterproject.smartflashcards;

import com.google.api.core.ApiFuture;
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

import javax.swing.text.Document;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class quizController {

    private ArrayList<String> def = new ArrayList<String>();
    private ArrayList<String> words = new ArrayList<String>();

    @FXML
    private Button createQuestionButton;

    @FXML
    public void initialize() throws ExecutionException, InterruptedException {
        Iterable<DocumentReference> defs = FlashcardApplication.fstore
                .collection("Users")
                .document(FlashcardApplication.currentUser)
                .collection("Math")
                .listDocuments();

        for (DocumentReference doc : defs) {
            ApiFuture<DocumentSnapshot> future = doc.get();
            String id = doc.getId();
            DocumentSnapshot inside = future.get();
            if(!id.equals("exists_placeholder") && !id.equals("_meta")) {
                def.add(id);
                words.add(inside.getString("Definition"));
            }
        }

        System.out.println(def);
        System.out.println(words);
    }

    @FXML
    private void createQuestion(){

    }
}
