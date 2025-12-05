package org.csc325.semesterproject.smartflashcards;

import com.google.cloud.firestore.*;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class StudyController {

    @FXML
    private ComboBox<String> setDropdown;
    @FXML
    private Label progressLabel;
    @FXML
    private Label cardCounterLabel;
    @FXML
    private Label sideLabel;
    @FXML
    private Label flashcardText;
    @FXML
    private StackPane flashcardContainer;

    @FXML
    private Button prevCardButton;
    @FXML
    private Button nextCardButton;
    @FXML
    private Button flipButton;
    @FXML
    private Button knownButton;
    @FXML
    private Button unknownButton;
    @FXML
    private Button restartButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button landingPageButton;

    // Holds all flashcards for the selected set
    private List<Flashcard> cards = new ArrayList<>();

    // Index of the current card being shown
    private int index = 0;

    // Counters for known / unknown progress
    private int known = 0;
    private int unknown = 0;

    // Tracks whether the front side (word) is displayed
    private boolean frontSide = true;

    // Prevents flipping animation from being triggered multiple times
    private boolean flipping = false;

    // Simple Flashcard structure: word = front, definition = back
    private static class Flashcard {
        String word;
        String definition;

        Flashcard(String w, String d) {
            word = w;
            definition = d;
        }
    }

    /* Initialization */
    @FXML
    public void initialize() {
        loadSets(); // Load all flashcard sets from Firestore
        disableUI(true); // Disable UI until a set is selected
        setupKeys(); // Enable keyboard controls (left/right/space)
    }

    /* Load sets from fire store */

    private void loadSets() {
        // Run Firestore call on background thread (never on JavaFX thread!)
        new Thread(() -> {
            try {
                Iterable<CollectionReference> cols = FlashcardApplication.fstore
                        .collection("Users")
                        .document(FlashcardApplication.currentUser)
                        .listCollections();

                List<String> setNames = new ArrayList<>();
                for (CollectionReference c : cols)
                    setNames.add(c.getId());

                // Update UI safely
                Platform.runLater(() -> setDropdown.getItems().setAll(setNames));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /* User selects a set for the type o topic */

    @FXML
    private void handleSetChange() {
        String selected = setDropdown.getValue();
        if (selected == null)
            return;

        FlashcardApplication.currentSet = selected;
        loadCards(selected);
    }

    /* Loads cards from fireStore */

    private void loadCards(String setName) {

        disableUI(true); // Prevent interaction while loading

        new Thread(() -> {
            try {
                Iterable<DocumentReference> docs = FlashcardApplication.fstore
                        .collection("Users")
                        .document(FlashcardApplication.currentUser)
                        .collection(setName)
                        .listDocuments();

                List<Flashcard> temp = new ArrayList<>();

                for (DocumentReference doc : docs) {
                    String id = doc.getId();
                    if (id.equals("_meta") || id.equals("exists_placeholder"))
                        continue; // Skip metadata document

                    DocumentSnapshot snap = doc.get().get();
                    if (!snap.exists())
                        continue;

                    String def = snap.getString("Definition");
                    temp.add(new Flashcard(id, def == null ? "" : def));
                }

                Platform.runLater(() -> {
                    cards = temp;

                    if (cards.isEmpty()) {
                        flashcardText.setText("No cards in this set.");
                        cardCounterLabel.setText("Card 0/0");
                        return;
                    }

                    restartSession();
                    disableUI(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    /* Reset Session */

    private void restartSession() {
        index = 0;
        known = 0;
        unknown = 0;
        frontSide = true;
        update();
    }

    /* Update text, counters and Colors */

    private void update() {
        Flashcard c = cards.get(index);

        // Switch between word and definition
        flashcardText.setText(frontSide ? c.word : c.definition);

        sideLabel.setText(frontSide ? "Front" : "Back");
        cardCounterLabel.setText("Card " + (index + 1) + "/" + cards.size());
        progressLabel.setText(known + " known | " + unknown + " unknown");

        // Remove both classes first to avoid duplicates
        flashcardContainer.getStyleClass().removeAll("flashcard-front", "flashcard-back");

        // Add the correct style
        if (frontSide) {
            flashcardContainer.getStyleClass().add("flashcard-front");
        } else {
            flashcardContainer.getStyleClass().add("flashcard-back");
        }
    }

    /* Buttons Actions */

    @FXML
    private void handlePrevCard() {
        if (flipping)
            return;
        index = (index - 1 + cards.size()) % cards.size();
        frontSide = true;
        update();
    }

    @FXML
    private void handleNextCard() {
        if (flipping)
            return;
        index = (index + 1) % cards.size();
        frontSide = true;
        update();
    }

    @FXML
    private void handleFlipCard() {
        if (flipping)
            return;
        animateFlip();
    }

    /* Flip Animation */

    private void animateFlip() {
        flipping = true;

        ScaleTransition shrink = new ScaleTransition(Duration.millis(120), flashcardContainer);
        shrink.setFromX(1);
        shrink.setToX(0);

        ScaleTransition grow = new ScaleTransition(Duration.millis(120), flashcardContainer);
        grow.setFromX(0);
        grow.setToX(1);

        // Shrink → switch card → grow
        shrink.setOnFinished(e -> {
            frontSide = !frontSide;
            update();
            grow.play();
        });

        grow.setOnFinished(e -> flipping = false);

        shrink.play();
    }

    /* Known and Unknow Logic */

    @FXML
    private void handleMarkKnown() {
        known++;
        handleNextCard();
    }

    @FXML
    private void handleMarkUnknown() {
        unknown++;
        handleNextCard();
    }

    /* Restart, logout and return to landing page */

    @FXML
    private void handleRestart() {
        restartSession();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = loader.load();
            Scene scene = logoutButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
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
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Keyboard Support */
    private void setupKeys() {
        Platform.runLater(() -> {
            Scene sc = flashcardContainer.getScene();
            if (sc == null)
                return;

            sc.setOnKeyPressed(key -> {
                if (key.getCode() == KeyCode.SPACE)
                    handleFlipCard();
                if (key.getCode() == KeyCode.RIGHT)
                    handleNextCard();
                if (key.getCode() == KeyCode.LEFT)
                    handlePrevCard();
            });
        });
    }

    /* Enable / Disable Buttons */
    private void disableUI(boolean b) {
        prevCardButton.setDisable(b);
        nextCardButton.setDisable(b);
        flipButton.setDisable(b);
        knownButton.setDisable(b);
        unknownButton.setDisable(b);
        restartButton.setDisable(b);
    }
}
