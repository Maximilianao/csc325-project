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

    private Timeline timer;
    private int timeRemainingSeconds = 300;
    private boolean gameActive = false;

    public static int lastGameScore = 0;

    @FXML
    public void initialize() {
        scoreLabel.setText("0");
        attemptsLabel.setText("0");
        updateTimerLabel();
        timerLabel.setStyle("-fx-text-fill: #16a34a;");
        messageLabel.setText("Select a set to start the mini game.");

        setGameAreaVisible(false);

        if (checkMatchButton != null) {
            checkMatchButton.setVisible(false);
            checkMatchButton.setManaged(false);
            checkMatchButton.setDisable(true);
        }
        if (resetGameButton != null) resetGameButton.setDisable(true);

        loadSets();
    }

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
                    String id = c.getId();
                    if (id.startsWith("_meta") || id.startsWith("exists")) continue;
                    setNames.add(id);
                }
                Collections.sort(setNames, String.CASE_INSENSITIVE_ORDER);

                Platform.runLater(() -> {
                    if (setDropdown != null) {
                        setDropdown.getItems().clear();
                        setDropdown.getItems().addAll(setNames);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (messageLabel != null) messageLabel.setText("Error loading sets for mini game.");
                });
            }
        }).start();
    }

    @FXML
    private void handleSetChange() {
        String selected = (setDropdown == null) ? null : setDropdown.getValue();
        if (selected == null || selected.isEmpty()) return;

        FlashcardApplication.currentSet = selected;

        stopTimer();
        timeRemainingSeconds = 300;
        timerLabel.setStyle("-fx-text-fill: #16a34a;");
        updateTimerLabel();

        messageLabel.setText("Loading cards for: " + selected + " ...");

        loadPairsFromFirestore();
    }

    private void loadPairsFromFirestore() {
        if (FlashcardApplication.currentUser == null || FlashcardApplication.currentSet == null) {
            messageLabel.setText("No active set. Please choose a set first.");
            if (checkMatchButton != null) checkMatchButton.setDisable(true);
            if (resetGameButton != null) resetGameButton.setDisable(true);
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
                    if (id.startsWith("_meta") || id.startsWith("exists")) continue; // <-- same filter

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
                        if (checkMatchButton != null) checkMatchButton.setDisable(true);
                        if (resetGameButton != null) resetGameButton.setDisable(true);
                        setGameAreaVisible(false);
                    } else {
                        buildButtonsForGame();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messageLabel.setText("Error loading cards for mini game.");
                    if (checkMatchButton != null) checkMatchButton.setDisable(true);
                    if (resetGameButton != null) resetGameButton.setDisable(true);
                    setGameAreaVisible(false);
                });
            }
        }).start();
    }

    private void buildButtonsForGame() {
        setGameAreaVisible(true);

        if (checkMatchButton != null) {
            checkMatchButton.setVisible(true);
            checkMatchButton.setManaged(true);
            checkMatchButton.setDisable(false);
        }
        if (resetGameButton != null) resetGameButton.setDisable(false);

        if (termsBox != null) termsBox.getChildren().clear();
        if (definitionsBox != null) definitionsBox.getChildren().clear();

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
        timerLabel.setStyle("-fx-text-fill: #16a34a;");
        updateTimerLabel();
        startTimer();

        gameActive = true;
        messageLabel.setText("Select one term and one definition, then click 'Check Match'.");
        updateCheckButtonState();
    }

    private void setGameAreaVisible(boolean visible) {
        if (termsBox != null) { termsBox.setVisible(visible); termsBox.setManaged(visible); }
        if (definitionsBox != null) { definitionsBox.setVisible(visible); definitionsBox.setManaged(visible); }
    }

    private void handleTermSelection(Button b) {
        if (!gameActive || b == null || b.isDisable()) return;
        if (b.equals(selectedTermButton)) {
            b.getStyleClass().remove("selected-term");
            selectedTermButton = null;
        } else {
            if (selectedTermButton != null) selectedTermButton.getStyleClass().remove("selected-term");
            selectedTermButton = b;
            if (!b.getStyleClass().contains("selected-term")) b.getStyleClass().add("selected-term");
        }
        updateCheckButtonState();
    }

    private void handleDefinitionSelection(Button b) {
        if (!gameActive || b == null || b.isDisable()) return;
        if (b.equals(selectedDefButton)) {
            b.getStyleClass().remove("selected-definition");
            selectedDefButton = null;
        } else {
            if (selectedDefButton != null) selectedDefButton.getStyleClass().remove("selected-definition");
            selectedDefButton = b;
            if (!b.getStyleClass().contains("selected-definition")) b.getStyleClass().add("selected-definition");
        }
        updateCheckButtonState();
    }

    private void updateCheckButtonState() {
        boolean enable = gameActive && selectedTermButton != null && selectedDefButton != null;
        if (checkMatchButton != null) checkMatchButton.setDisable(!enable);
    }

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

        if (termPair != null && defPair != null && termPair.term.equals(defPair.term)) {
            score++;
            selectedTermButton.setDisable(true);
            selectedDefButton.setDisable(true);
            selectedTermButton.getStyleClass().remove("selected-term");
            selectedDefButton.getStyleClass().remove("selected-definition");
            selectedTermButton.getStyleClass().add("matched");
            selectedDefButton.getStyleClass().add("matched");
            messageLabel.setText("✅ Correct match!");
        } else {
            if (selectedTermButton != null) selectedTermButton.getStyleClass().remove("selected-term");
            if (selectedDefButton != null) selectedDefButton.getStyleClass().remove("selected-definition");
            messageLabel.setText("❌ Not a match. Try again.");
        }

        selectedTermButton = null;
        selectedDefButton = null;

        updateScoreUI();
        updateCheckButtonState();

        if (isGameFinished()) {
            stopTimer();
            gameActive = false;
            lastGameScore = score;
            String msg = "You matched all terms! Score: " + score + "/" + attempts;
            messageLabel.setText("🎉 " + msg);
            if (checkMatchButton != null) checkMatchButton.setDisable(true);
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

    private void startTimer() {
        stopTimer();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemainingSeconds--;
            updateTimerLabel();
            if (timeRemainingSeconds == 60 && timerLabel != null) timerLabel.setStyle("-fx-text-fill: #dc2626;");
            if (timeRemainingSeconds <= 0) handleTimeUp();
        }));
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
        if (timerLabel != null) timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void showGameOverDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (backButton != null && backButton.getScene() != null) {
            Stage stage = (Stage) backButton.getScene().getWindow();
            alert.initOwner(stage);
        }
        alert.showAndWait();
    }

    private void handleTimeUp() {
        stopTimer();
        gameActive = false;
        lastGameScore = score;
        String msg = "Time is up!\nFinal score: " + score + " / " + attempts;
        messageLabel.setText("⏰ " + msg);
        setGameAreaVisible(false);
        if (checkMatchButton != null) {
            checkMatchButton.setDisable(true);
            checkMatchButton.setVisible(false);
            checkMatchButton.setManaged(false);
        }
        showGameOverDialog("⏳ Time's Up!", msg);
    }

    private void updateScoreUI() {
        if (scoreLabel != null) scoreLabel.setText(String.valueOf(score));
        if (attemptsLabel != null) attemptsLabel.setText(String.valueOf(attempts));
    }

    private boolean isGameFinished() {
        return termButtonMap.keySet().stream().allMatch(Button::isDisable);
    }
}
