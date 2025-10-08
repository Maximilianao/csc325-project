module org.csc325.semesterproject.smartflashcards {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.csc325.semesterproject.smartflashcards to javafx.fxml;
    exports org.csc325.semesterproject.smartflashcards;
}