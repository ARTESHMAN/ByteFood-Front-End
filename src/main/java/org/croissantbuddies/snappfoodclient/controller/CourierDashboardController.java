package org.croissantbuddies.snappfoodclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import java.io.IOException;
import java.net.URL;

public class CourierDashboardController {

    @FXML private AnchorPane contentPane;
    @FXML private Label courierNameLabel;

    @FXML
    public void initialize() {
        courierNameLabel.setText(SessionManager.getUserFullName());
        handleAvailableDeliveries(null);
    }

    @FXML
    void handleAvailableDeliveries(ActionEvent event) {
        loadPage("available-deliveries-view");
    }

    @FXML
    void handleMyDeliveries(ActionEvent event) {
        loadPage("my-deliveries-view");
    }

    @FXML
    void handleProfile(ActionEvent event) {
        loadPage("profile-view");
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        SessionManager.logout();
        URL fxmlLocation = getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/welcome page.fxml");
        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void loadPage(String fxmlFileName) {
        try {
            Node page = FXMLLoader.load(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/" + fxmlFileName + ".fxml"));
            contentPane.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}