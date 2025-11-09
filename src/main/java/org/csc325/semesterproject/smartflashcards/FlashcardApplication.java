package org.csc325.semesterproject.smartflashcards;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.google.cloud.firestore.Firestore;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.*;
import com.google.cloud.firestore.*;

public class FlashcardApplication extends Application {



    public static Firestore fstore;
    public static FirebaseAuth fauth;
    private final FirestoreContext contxtFirebase = new FirestoreContext();

    @Override
    public void start(Stage stage) throws IOException {
        fstore = contxtFirebase.firebase();
        fauth = FirebaseAuth.getInstance();

        FXMLLoader fxmlLoader = new FXMLLoader(FlashcardApplication.class.getResource("login_screen" +
                ".fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);
        stage.setTitle("Smart Flashcards");
        stage.setScene(scene);
        stage.show();
    }



}
