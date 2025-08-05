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

public class SellerDashboardController {

    @FXML
    private AnchorPane contentPane;
    @FXML
    private Label sellerNameLabel;

    @FXML
    public void initialize() {
        sellerNameLabel.setText(SessionManager.getUserFullName());
        handleMyRestaurants(null);
    }
    @FXML
    void handleProfile(ActionEvent event) {
        loadPage("profile-view");
    }


    @FXML
    void handleMyRestaurants(ActionEvent event) {
        loadPage("restaurant-management-view");
    }

    @FXML
    void handleManageOrders(ActionEvent event) {
        loadPage("seller-order-management-view");
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        SessionManager.logout();
        URL fxmlLocation = getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/welcome page.fxml");
        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Welcome to SnappFood");
        stage.show();
    }

    private void loadPage(String fxmlFileName) {
        try {
            URL fxmlUrl = getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/" + fxmlFileName + ".fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: " + fxmlFileName);
                return;
            }
            Node page = FXMLLoader.load(fxmlUrl);
            contentPane.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}