package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private Label errorLabel;

    private final ApiService apiService = new ApiService();
    @FXML
    public void initialize() {
        phoneNumberField.textProperty().addListener((obs, oldText, newText) -> clearError());
        passwordField.textProperty().addListener((obs, oldText, newText) -> clearError());
    }


    @FXML
    void Login(ActionEvent event) {
        String phone = phoneNumberField.getText();
        String password = passwordField.getText();

        if (phone.isEmpty() || password.isEmpty()) {
            showError("Phone number and password cannot be empty.");
            return;
        }

        new Thread(() -> {
            try {
                String loginResponseBody = apiService.login(phone, password);
                JSONObject loginResponseJson = new JSONObject(loginResponseBody);
                String token = loginResponseJson.getString("token");

                if ("admin".equalsIgnoreCase(phone)) {
                    SessionManager.login(token, 0L, "Admin User", "ADMIN", null, "VALID");
                } else {
                    String profileResponseBody = apiService.getProfile(token);
                    JSONObject userJson = new JSONObject(profileResponseBody);

                    String status = userJson.optString("status", "VALID");

                    SessionManager.login(
                            token,
                            userJson.getLong("id"),
                            userJson.getString("full_name"),
                            userJson.getString("role"),
                            userJson.optString("profileImageBase64", null),
                            status
                    );
                }

                Platform.runLater(() -> {
                    try {
                        switchToDashboard(event);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Failed to load dashboard.");
                    }
                });

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> showError("Login failed: " + e.getMessage()));
            } catch (org.json.JSONException e) {
                Platform.runLater(() -> showError("Login failed: Invalid response from server."));
                e.printStackTrace();
            }
        }).start();
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
    }

    private void switchToDashboard(ActionEvent event) throws IOException {
        String fxmlFile;
        String title;
        String userRole = SessionManager.getUserRole();
        String userStatus = SessionManager.getUserStatus();

        if (("SELLER".equalsIgnoreCase(userRole) || "COURIER".equalsIgnoreCase(userRole)) && !"VALID".equalsIgnoreCase(userStatus)) {
            fxmlFile = "/org/croissantbuddies/snappfoodclient/fxml/waiting-for-approval-view.fxml";
            title = "Waiting for admin approval";
        } else {
            switch (userRole.toUpperCase()) {
                case "ADMIN":
                    fxmlFile = "/org/croissantbuddies/snappfoodclient/fxml/admin-dashboard-view.fxml";
                    title = "Admin Dashboard";
                    break;
                case "SELLER":
                    fxmlFile = "/org/croissantbuddies/snappfoodclient/fxml/seller-dashboard-view.fxml";
                    title = "Seller Dashboard";
                    break;
                case "BUYER":
                    fxmlFile = "/org/croissantbuddies/snappfoodclient/fxml/buyer-dashboard-view.fxml";
                    title = "Buyer Dashboard";
                    break;
                case "COURIER":
                    fxmlFile = "/org/croissantbuddies/snappfoodclient/fxml/courier-dashboard-view.fxml";
                    title = "Courier Dashboard";
                    break;
                default:
                    fxmlFile = "/org/croissantbuddies/snappfoodclient/fxml/dashboard-view.fxml";
                    title = "Dashboard";
                    break;
            }
        }

        URL fxmlLocation = getClass().getResource(fxmlFile);
        if (fxmlLocation == null) {
            System.err.println("Cannot find FXML file: " + fxmlFile);
            Platform.runLater(() -> showError("Could not load dashboard for role: " + userRole));
            return;
        }

        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }
    @FXML
    void previousPage(ActionEvent event) throws IOException {
        URL fxmlLocation = getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/welcome page.fxml");
        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}