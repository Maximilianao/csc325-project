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

    private Map<StackPane, String> cardValues = new HashMap<>();
    private StackPane firstCard = null;
    private StackPane secondCard = null;

    private String currentSet;
    private boolean busy = false; // <-- new flag
    @FXML
    private VBox rootVbox;
    Timer timer = new Timer();

    @FXML
    public void initialize() {
        populateSets();
        restartButton.setOnAction(_ -> restartGame());

        Platform.runLater(()-> rootVbox.getScene().getWindow().setOnCloseRequest(_ -> timer.cancel()));
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
        card.getStyleClass().add("clickable_box");

        Text text = new Text("");
        card.getChildren().add(text);

        card.setOnMouseClicked(_ -> {
            if (busy || !text.getText().isEmpty()) return; // ignore clicks if busy or already flipped
            flipCard(card, value);
        });

        return card;
    }

    private void flipCard(StackPane card, String value) {
        Text text = (Text) card.getChildren().get(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
        st.setFromX(1);
        st.setToX(0);
        st.setOnFinished(_ -> {
            text.setText(value);
            ScaleTransition st2 = new ScaleTransition(Duration.millis(150), card);
            st2.setFromX(0);
            st2.setToX(1);
            st2.play();
        });
        st.play();

        if (firstCard == null) {
            firstCard = card;
        } else {
            secondCard = card;
            busy = true; // disable clicks until match is checked
            checkMatch();
        }
    }

    private void checkMatch() {
        String val1 = cardValues.get(firstCard);
        String val2 = cardValues.get(secondCard);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!isMatch(val1, val2)) {
                        flipBack(firstCard);
                        flipBack(secondCard);
                    }
                    firstCard = null;
                    secondCard = null;
                    busy = false; // re-enable clicks
                    checkWin();
                });
            }
        }, 500); // short delay so player can see second card
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
                    if ((a.equals(word) && b.equals(def)) || (a.equals(def) && b.equals(word))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void flipBack(StackPane card) {
        Text text = (Text) card.getChildren().get(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
        st.setFromX(1);
        st.setToX(0);
        st.setOnFinished(_ -> {
            text.setText("");
            ScaleTransition st2 = new ScaleTransition(Duration.millis(150), card);
            st2.setFromX(0);
            st2.setToX(1);
            st2.play();
        });
        st.play();
    }

    private void checkWin() {
        boolean allFlipped = cardGrid.getChildren().stream()
                .allMatch(card -> !((Text)((StackPane)card).getChildren().get(0)).getText().isEmpty());
        if (allFlipped) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("You Won!");
                alert.setHeaderText(null);
                alert.setContentText("Congratulations! You matched all the cards!");
                alert.showAndWait();
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
            FXMLLoader login = new FXMLLoader(getClass().getResource("play_landing.fxml"));
            Parent root = login.load();

            Scene currentScene = rootVbox.getScene();
            currentScene.setRoot(root);
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
