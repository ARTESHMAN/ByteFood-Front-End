package org.croissantbuddies.snappfoodclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + SessionManager.getUserFullName() + "!");
        }
    }

    public void logout(ActionEvent event) throws IOException {
        SessionManager.logout();

        URL fxmlLocation = getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/welcome page.fxml");
        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Welcome to SnappFood");
        stage.show();
    }
}