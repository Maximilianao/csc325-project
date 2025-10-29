module org.csc325.semesterproject.smartflashcards {
    requires javafx.controls;
    requires javafx.fxml;
    requires firebase.admin;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires google.cloud.core;
    requires com.google.api.apicommon;

    opens org.csc325.semesterproject.smartflashcards to javafx.fxml;
    exports org.csc325.semesterproject.smartflashcards;
}