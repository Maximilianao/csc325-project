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
    private ArrayList<String> usedDef = new ArrayList<String>();
    private ArrayList<String> usedWords = new ArrayList<String>();

    private String question = "";
    private String correctAns = "";
    private String wrongAns1 = "";
    private String wrongAns2 = "";
    private String wrongAns3 = "";
    private String location = null;


    @FXML
    private Button createQuestionButton;

    @FXML
    private Button buttonA;
    @FXML
    private Button buttonB;
    @FXML
    private Button buttonC;
    @FXML
    private Button buttonD;
    @FXML
    private Label questionLabel;

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

        if(location == null){
            createQuestionButton.disableProperty().set(true);
        }
        int rand = (int) (Math.random() * def.size());
        while (question.equals("")) {
            if (usedDef.isEmpty()) {
                question = def.get(rand);
                usedDef.add(def.get(rand));
                usedWords.add(words.get(rand));
                correctAns = words.get(rand);
            } else {
                for (String quest : usedDef) {
                    if (!def.get(rand).equals(quest)) {
                        question = def.get(rand);
                        usedDef.add(def.get(rand));
                        usedWords.add(words.get(rand));
                        correctAns = words.get(rand);
                    }
                }
            }
            questionLabel.setText(question);
        }

        while (wrongAns1.equals("")) {

            int randomNum3 = (int) (Math.random() * words.size());
            for (String usedWord : usedWords) {
                if (!words.get(randomNum3).equals(usedWord) && !words.get(randomNum3).equals(wrongAns2)
                        && !words.get(randomNum3).equals(wrongAns3)) {
                    wrongAns1 = words.get(randomNum3);
                }
            }
        }

        while (wrongAns2.equals("")) {

            int randomNum3 = (int) (Math.random() * words.size());
            for (String usedWord : usedWords) {
                if (!words.get(randomNum3).equals(usedWord) && !words.get(randomNum3).equals(wrongAns1)
                        && !words.get(randomNum3).equals(wrongAns3)) {
                    wrongAns2 = words.get(randomNum3);
                }
            }
        }

        while (wrongAns3.equals("")) {

            int randomNum3 = (int) (Math.random() * words.size());
            for (String usedWord : usedWords) {
                if (!words.get(randomNum3).equals(usedWord) && !words.get(randomNum3).equals(wrongAns1)
                        && !words.get(randomNum3).equals(wrongAns2)) {
                    wrongAns3 = words.get(randomNum3);
                }
            }
        }

        populateButtons();
    }

    private void populateButtons(){
        int randomAns1 = (int) (Math.random() * 4);
        int randomAns2 = -1;
        int randomAns3 = -1;
        int randomAns4 = -1;

        System.out.println("1 " + randomAns1 + "\n");
        if (randomAns1 == 0) {
            buttonA.setText(correctAns);
            location = "buttonA";
        }
        if (randomAns1 == 1) {
            buttonA.setText(wrongAns1);
        }
        if (randomAns1 == 2) {
            buttonA.setText(wrongAns2);
        }
        if (randomAns1 == 3) {
            buttonA.setText(wrongAns3);
        }

        while(randomAns2 == randomAns1 || randomAns2 == -1){
            randomAns2 = (int) (Math.random() * 4);
            System.out.println("2 " + randomAns2);
        }
        System.out.println();
        if (randomAns2 == 0) {
            buttonB.setText(correctAns);
            location = "buttonB";
        }
        if (randomAns2 == 1) {
            buttonB.setText(wrongAns1);
        }
        if (randomAns2 == 2) {
            buttonB.setText(wrongAns2);
        }
        if (randomAns2 == 3) {
            buttonB.setText(wrongAns3);
        }

        while(randomAns3 == randomAns1 || randomAns3 == randomAns2 ||  randomAns3 == -1) {
            randomAns3 = (int) (Math.random() * 4);
            System.out.println("3 " + randomAns3);
        }
        System.out.println();
        if (randomAns3 == 0) {
            buttonC.setText(correctAns);
            location = "buttonC";
        }
        if (randomAns3 == 1) {
            buttonC.setText(wrongAns1);
        }
        if (randomAns3 == 2) {
            buttonC.setText(wrongAns2);
        }
        if (randomAns3 == 3) {
            buttonC.setText(wrongAns3);
        }

        while(randomAns4 == randomAns1 || randomAns4 == randomAns2 || randomAns4 == randomAns3 || randomAns4 == -1) {
            randomAns4 = (int) (Math.random() * 4);
            System.out.println("4 " + randomAns4);
        }
        if (randomAns4 == 0) {
            buttonD.setText(correctAns);
            location = "buttonD";
        }
        if (randomAns4 == 1) {
            buttonD.setText(wrongAns1);
        }
        if (randomAns4 == 2) {
            buttonD.setText(wrongAns2);
        }
        if (randomAns4 == 3) {
            buttonD.setText(wrongAns3);
        }
    }
}
