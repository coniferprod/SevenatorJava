package com.coniferproductions.sevenator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class AppController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private void handleMouseClick(MouseEvent mouseEvent) {
        System.err.println("handleMouseClick");
    }
}
