module sevenator {
    requires java.sql;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;

    opens com.coniferproductions.sevenator to javafx.fxml;
    exports com.coniferproductions.sevenator;
}