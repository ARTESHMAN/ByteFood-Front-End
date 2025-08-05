package org.croissantbuddies.snappfoodclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class WelcomeController {

    @FXML
    public void switchToLoginPage(ActionEvent event) throws IOException {

        URL fxmlLocation = getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/Login page.fxml");
        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToSignupPage(ActionEvent event) throws IOException {
        URL fxmlLocation = getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/signup.fxml");
        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Sign Up");
        stage.show();
    }
}