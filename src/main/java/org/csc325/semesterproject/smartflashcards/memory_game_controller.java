package org.csc325.semesterproject.smartflashcards;

import com.google.cloud.firestore.*;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

public class memory_game_controller {

    @FXML
    private Button backButton, restartButton;

    @FXML
    private ComboBox<String> setDropdown;

    @FXML
    private GridPane cardGrid;

    @FXML
    private VBox rootVbox;

    private Map<StackPane, String> cardValues = new HashMap<>();
    private StackPane firstCard = null;
    private StackPane secondCard = null;

    private String currentSet;
    private boolean busy = false;

    private Timer timer = new Timer();

    @FXML
    public void initialize() {
        populateSets();
        restartButton.setOnAction(_ -> restartGame());

        Platform.runLater(() ->
                rootVbox.getScene().getWindow().setOnCloseRequest(_ -> timer.cancel())
        );
    }

    private void populateSets() {
        ObservableList<String> sets = javafx.collections.FXCollections.observableArrayList();

        try {
            Iterable<CollectionReference> collections = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .listCollections();

            for (CollectionReference c : collections) {
                if (!c.getId().startsWith("_meta") && !c.getId().startsWith("exists")) {
                    sets.add(c.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDropdown.setItems(sets);
        if (!sets.isEmpty()) {
            setDropdown.setValue(sets.get(0));
            currentSet = sets.get(0);
            loadCards();
        }

        setDropdown.setOnAction(_ -> {
            currentSet = setDropdown.getValue();
            loadCards();
        });
    }

    private void loadCards() {
        cardGrid.getChildren().clear();
        cardValues.clear();
        firstCard = null;
        secondCard = null;
        busy = false;

        if (currentSet == null) return;

        List<String> items = new ArrayList<>();

        try {
            Iterable<DocumentReference> docs = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .collection(currentSet)
                    .listDocuments();

            for (DocumentReference doc : docs) {
                String id = doc.getId();
                if (id.startsWith("_meta") || id.startsWith("exists")) continue;

                DocumentSnapshot snap = doc.get().get();
                if (snap.exists() && !"metadata".equals(snap.getString("type"))) {
                    String word = id;
                    String def = snap.getString("Definition") == null ? "" : snap.getString("Definition");

                    items.add(word);
                    items.add(def);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.shuffle(items);

        int col = 0, row = 0;
        for (String value : items) {
            StackPane card = createCard(value);
            cardGrid.add(card, col, row);
            cardValues.put(card, value);

            col++;
            if (col >= 6) {
                col = 0;
                row++;
            }
        }
    }

    private StackPane createCard(String value) {
        StackPane card = new StackPane();
        card.setPrefSize(120, 80);
        card.getStyleClass().add("card-face-down");

        Text text = new Text("?");
        card.getChildren().add(text);

        card.setOnMouseClicked(_ -> {
            if (busy || !text.getText().equals("?")) return;
            flipCard(card, value);
        });

        return card;
    }

    private void flipCard(StackPane card, String value) {
        //Prevent selecting the same card twice
        if (firstCard == card) return;

        Text text = (Text) card.getChildren().get(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
        st.setFromX(1);
        st.setToX(0);

        st.setOnFinished(_ -> {
            text.setText(value);
            card.getStyleClass().remove("card-face-down");
            card.getStyleClass().add("card-face-up");

            ScaleTransition st2 = new ScaleTransition(Duration.millis(150), card);
            st2.setFromX(0);
            st2.setToX(1);
            st2.play();
        });

        st.play();

        // First card selection
        if (firstCard == null) {
            firstCard = card;
            return;
        }

        // Second card selection
        secondCard = card;
        busy = true;
        checkMatch();
    }


    private void checkMatch() {
        String val1 = cardValues.get(firstCard);
        String val2 = cardValues.get(secondCard);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (isMatch(val1, val2)) {
                        markAsMatched(firstCard);
                        markAsMatched(secondCard);
                    } else {
                        flipBack(firstCard);
                        flipBack(secondCard);
                    }

                    firstCard = null;
                    secondCard = null;
                    busy = false;
                    checkWin();
                });
            }
        }, 500);

    }

    private boolean isMatch(String a, String b) {
        try {
            Iterable<DocumentReference> docs = FlashcardApplication.fstore
                    .collection("Users")
                    .document(FlashcardApplication.currentUser)
                    .collection(currentSet)
                    .listDocuments();

            for (DocumentReference doc : docs) {
                String id = doc.getId();
                if (id.startsWith("_meta") || id.startsWith("exists")) continue;

                DocumentSnapshot snap = doc.get().get();
                if (snap.exists() && !"metadata".equals(snap.getString("type"))) {
                    String word = id;
                    String def = snap.getString("Definition") == null ? "" : snap.getString("Definition");

                    if ((a.equals(word) && b.equals(def)) ||
                            (a.equals(def) && b.equals(word))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    private void markAsMatched(StackPane card) {
        card.getStyleClass().remove("card-face-up");
        if (!card.getStyleClass().contains("card-matched")) {
            card.getStyleClass().add("card-matched");
        }
    }

    private void flipBack(StackPane card) {
        Text text = (Text) card.getChildren().get(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
        st.setFromX(1);
        st.setToX(0);

        st.setOnFinished(_ -> {
            text.setText("?");
            card.getStyleClass().removeAll("card-face-up", "card-matched");

            if (!card.getStyleClass().contains("card-matched")) {
                card.getStyleClass().add("card-face-down");
            }

            ScaleTransition st2 = new ScaleTransition(Duration.millis(150), card);
            st2.setFromX(0);
            st2.setToX(1);
            st2.play();
        });

        st.play();
    }

    private void checkWin() {
        boolean allFlipped = cardGrid.getChildren().stream()
                .allMatch(card -> !((Text)((StackPane)card).getChildren().get(0))
                        .getText().equals("?"));

        if (allFlipped) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("You Won!");
                alert.setHeaderText(null);
                alert.setContentText("Congratulations! You matched all the cards!");
                alert.showAndWait();
                memoryCorrectAnswer();
            });
        }
    }

    @FXML
    private void restartGame() {
        loadCards();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("play_landing.fxml"));
            Parent root = loader.load();

            Scene scene = rootVbox.getScene();
            scene.setRoot(root);

            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void memoryCorrectAnswer() {

        try {
            DocumentReference docRef = FlashcardApplication.fstore
                    .collection("UserProgress")
                    .document(FlashcardApplication.currentUser)
                    .collection(currentSet)
                    .document(currentSet + "Memorize");

            Map<String, Object> data = new HashMap<>();
            data.put("Completed", "Yes");
            docRef.set(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
