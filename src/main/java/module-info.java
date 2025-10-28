module org.csc325.semesterproject.smartflashcards {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.csc325.semesterproject.smartflashcards to javafx.fxml;
    exports org.csc325.semesterproject.smartflashcards;
}