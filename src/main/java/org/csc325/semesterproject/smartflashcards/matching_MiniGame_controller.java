package org.csc325.semesterproject.smartflashcards;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class matching_MiniGame_controller {

    @FXML private ComboBox<String> setDropdown;

    @FXML private VBox termsBox;
    @FXML private VBox definitionsBox;

    @FXML private Label scoreLabel;
    @FXML private Label attemptsLabel;
    @FXML private Label timerLabel;
    @FXML private Label messageLabel;

    @FXML private Button checkMatchButton;
    @FXML private Button resetGameButton;
    @FXML private Button backButton;
    @FXML private Button logoutButton;

    // Game state
    private static class Pair {
        final String term;
        final String definition;

        Pair(String term, String definition) {
            this.term = term;
            this.definition = definition;
        }
    }

    private final List<Pair> pairs = new ArrayList<>();

    private final Map<Button, Pair> termButtonMap = new HashMap<>();
    private final Map<Button, Pair> defButtonMap = new HashMap<>();

    private Button selectedTermButton = null;
    private Button selectedDefButton = null;

    private int score = 0;
    private int attempts = 0;

    // Timer state
    private Timeline timer;
    private int timeRemainingSeconds = 300;  // 5 minutes
    private boolean gameActive = false;

    // Store score for later use if needed
    public static int lastGameScore = 0;


    //  Initialization
    @FXML
    public void initialize() {
        // UI defaults
        scoreLabel.setText("0");
        attemptsLabel.setText("0");

        // Timer defaults
        updateTimerLabel();
        timerLabel.setStyle("-fx-text-fill: #16a34a;"); // green

        messageLabel.setText("Select a set to start the mini game.");

        // Hide game area until a set is selected
        setGameAreaVisible(false);
        checkMatchButton.setVisible(false);
        checkMatchButton.setManaged(false);

        resetGameButton.setDisable(true);

        loadSets();
    }

    //  Load sets for ComboBox

    private void loadSets() {
        new Thread(() -> {
            try {
                Iterable<CollectionReference> cols =
                        FlashcardApplication.fstore
                                .collection("Users")
                                .document(FlashcardApplication.currentUser)
                                .listCollections();

                List<String> setNames = new ArrayList<>();
                for (CollectionReference c : cols) {
                    setNames.add(c.getId());
                }

                Platform.runLater(() -> setDropdown.getItems().setAll(setNames));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        messageLabel.setText("Error loading sets for mini game.")
                );
            }
        }).start();
    }

    @FXML
    private void handleSetChange() {
        String selected = setDropdown.getValue();
        if (selected == null || selected.isEmpty()) return;

        FlashcardApplication.currentSet = selected;

        stopTimer();
        timeRemainingSeconds = 300; // 5 minutes
        timerLabel.setStyle("-fx-text-fill: #16a34a;"); //green style for the timer as default
        updateTimerLabel();

        messageLabel.setText("Loading cards for: " + selected + " ...");

        loadPairsFromFirestore();
    }

    private void loadPairsFromFirestore() {
        if (FlashcardApplication.currentUser == null || FlashcardApplication.currentSet == null) {
            messageLabel.setText("No active set. Please choose a set first.");
            checkMatchButton.setDisable(true);
            resetGameButton.setDisable(true);
            return;
        }

        new Thread(() -> {
            try {
                CollectionReference colRef = FlashcardApplication.fstore
                        .collection("Users")
                        .document(FlashcardApplication.currentUser)
                        .collection(FlashcardApplication.currentSet);

                Iterable<DocumentReference> docs = colRef.listDocuments();
                List<Pair> tempPairs = new ArrayList<>();

                for (DocumentReference doc : docs) {
                    String id = doc.getId();
                    if ("_meta".equals(id)) continue;

                    DocumentSnapshot snap = doc.get().get();
                    if (!snap.exists()) continue;

                    String def = snap.getString("Definition");
                    if (def == null) def = "";

                    tempPairs.add(new Pair(id, def));
                }

                Platform.runLater(() -> {
                    pairs.clear();
                    pairs.addAll(tempPairs);

                    if (pairs.isEmpty()) {
                        messageLabel.setText("This set has no cards. Add flashcards before playing.");
                        checkMatchButton.setDisable(true);
                        resetGameButton.setDisable(true);
                        setGameAreaVisible(false);
                    } else {
                        buildButtonsForGame();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messageLabel.setText("Error loading cards for mini game.");
                    checkMatchButton.setDisable(true);
                    resetGameButton.setDisable(true);
                    setGameAreaVisible(false);
                });
            }
        }).start();
    }


    //  Game / UI setup

    private void buildButtonsForGame() {
        setGameAreaVisible(true);
        checkMatchButton.setVisible(true);
        checkMatchButton.setManaged(true);
        checkMatchButton.setDisable(false);
        resetGameButton.setDisable(false);

        // Clear old buttons but keep header + separator (indexes 0 and 1)
        if (termsBox.getChildren().size() > 2) {
            termsBox.getChildren().remove(2, termsBox.getChildren().size());
        }
        if (definitionsBox.getChildren().size() > 2) {
            definitionsBox.getChildren().remove(2, definitionsBox.getChildren().size());
        }

        termButtonMap.clear();
        defButtonMap.clear();
        selectedTermButton = null;
        selectedDefButton = null;

        List<Pair> shuffledTerms = new ArrayList<>(pairs);
        List<Pair> shuffledDefs = new ArrayList<>(pairs);
        Collections.shuffle(shuffledTerms);
        Collections.shuffle(shuffledDefs);

        for (Pair p : shuffledTerms) {
            Button b = new Button(p.term);
            b.getStyleClass().add("term-button");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setWrapText(true);
            b.setOnAction(e -> handleTermSelection(b));

            termButtonMap.put(b, p);
            termsBox.getChildren().add(b);
        }

        for (Pair p : shuffledDefs) {
            Button b = new Button(p.definition);
            b.getStyleClass().add("definition-button");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setWrapText(true);
            b.setOnAction(e -> handleDefinitionSelection(b));

            defButtonMap.put(b, p);
            definitionsBox.getChildren().add(b);
        }

        score = 0;
        attempts = 0;
        updateScoreUI();

        stopTimer();
        timeRemainingSeconds = 300;
        timerLabel.setStyle("-fx-text-fill: #16a34a;"); // Green color for the timer as default
        updateTimerLabel();
        startTimer();

        gameActive = true;
        messageLabel.setText("Select one term and one definition, then click 'Check Match'.");
    }

    private void setGameAreaVisible(boolean visible) {
        termsBox.setVisible(visible);
        termsBox.setManaged(visible);
        definitionsBox.setVisible(visible);
        definitionsBox.setManaged(visible);
    }

    //  Selection Logic
    private void handleTermSelection(Button b) {
        if (!gameActive || b.isDisable()) return;

        if (selectedTermButton != null) {
            selectedTermButton.getStyleClass().remove("selected-term");
        }

        selectedTermButton = b;
        if (!b.getStyleClass().contains("selected-term")) {
            b.getStyleClass().add("selected-term");
        }
    }

    private void handleDefinitionSelection(Button b) {
        if (!gameActive || b.isDisable()) return;

        if (selectedDefButton != null) {
            selectedDefButton.getStyleClass().remove("selected-definition");
        }

        selectedDefButton = b;
        if (!b.getStyleClass().contains("selected-definition")) {
            b.getStyleClass().add("selected-definition");
        }
    }


    //  Button Handlers

    @FXML
    private void handleCheckMatch() {
        if (!gameActive) return;

        if (selectedTermButton == null || selectedDefButton == null) {
            messageLabel.setText("Please select one term and one definition first.");
            return;
        }

        Pair termPair = termButtonMap.get(selectedTermButton);
        Pair defPair = defButtonMap.get(selectedDefButton);

        attempts++;

        if (termPair != null && defPair != null
                && termPair.term.equals(defPair.term)) {

            score++;

            selectedTermButton.setDisable(true);
            selectedDefButton.setDisable(true);

            selectedTermButton.getStyleClass().remove("selected-term");
            selectedDefButton.getStyleClass().remove("selected-definition");

            selectedTermButton.getStyleClass().add("matched");
            selectedDefButton.getStyleClass().add("matched");

            messageLabel.setText("✅ Correct match!");

        } else {
            messageLabel.setText("❌ Not a match. Try again.");
            selectedTermButton.getStyleClass().remove("selected-term");
            selectedDefButton.getStyleClass().remove("selected-definition");
        }

        selectedTermButton = null;
        selectedDefButton = null;

        updateScoreUI();

        if (isGameFinished()) {
            stopTimer();
            gameActive = false;
            lastGameScore = score;

            String msg = "You matched all terms! Score: " + score + "/" + attempts;
            messageLabel.setText("🎉 " + msg);

            checkMatchButton.setDisable(true);

            // Show game over popup (finished all matches)
            showGameOverDialog("You matched all terms!", msg);
        }
    }

    @FXML
    private void handleResetGame() {
        if (FlashcardApplication.currentSet == null) {
            messageLabel.setText("Please select a set first.");
            return;
        }
        buildButtonsForGame();
    }

    @FXML
    private void handleBackToLanding() {
        stopTimer();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("play_landing.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) setDropdown.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        stopTimer();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login_screen.fxml"));
            Parent root = loader.load();
            Scene scene = logoutButton.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  Timer helpers

    private void startTimer() {
        stopTimer(); // safety

        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    timeRemainingSeconds--;
                    updateTimerLabel();

                    if (timeRemainingSeconds == 60) {
                        timerLabel.setStyle("-fx-text-fill: #dc2626;"); // red color for timer at 1 minute
                    }

                    if (timeRemainingSeconds <= 0) {
                        stopTimer();
                        handleTimeUp();
                    }
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void updateTimerLabel() {
        int minutes = timeRemainingSeconds / 60;
        int seconds = timeRemainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    //  Game Over Popup
    private void showGameOverDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Attach to current window
        if (backButton != null && backButton.getScene() != null) {
            Stage stage = (Stage) backButton.getScene().getWindow();
            alert.initOwner(stage);
        }

        alert.showAndWait();
    }

    private void handleTimeUp() {
        gameActive = false;
        lastGameScore = score;

        String msg = "Time is up!\nFinal score: " + score + " / " + attempts;

        messageLabel.setText("⏰ " + msg);


        setGameAreaVisible(false);
        checkMatchButton.setDisable(true);
        checkMatchButton.setVisible(false);
        checkMatchButton.setManaged(false);

        showGameOverDialog("⏳ Time's Up!", msg);
    }

    //  Misc helpers

    private void updateScoreUI() {
        scoreLabel.setText(String.valueOf(score));
        attemptsLabel.setText(String.valueOf(attempts));
    }

    private boolean isGameFinished() {
        return termButtonMap.keySet().stream().allMatch(Button::isDisable);
    }
}
