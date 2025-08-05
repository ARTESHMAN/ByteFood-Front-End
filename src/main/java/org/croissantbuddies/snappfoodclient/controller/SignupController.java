package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Role;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class SignupController {

    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private TextField addressField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private TextField emailField;
    @FXML private TextField bankNameField;
    @FXML private TextField accountNumberField;

    private final ApiService apiService = new ApiService();

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll(Role.BUYER, Role.SELLER, Role.COURIER);

        fullNameField.textProperty().addListener((obs, old, aNew) -> clearError());
        phoneField.textProperty().addListener((obs, old, aNew) -> clearError());
        passwordField.textProperty().addListener((obs, old, aNew) -> clearError());
        addressField.textProperty().addListener((obs, old, aNew) -> clearError());
        roleComboBox.valueProperty().addListener((obs, old, aNew) -> clearError());
    }



    @FXML
    void onSignupButtonClick(ActionEvent event) {

        String fullName = fullNameField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String address = addressField.getText();
        Role role = roleComboBox.getValue();


        if (fullName.isEmpty() || phone.isEmpty() || password.isEmpty() || address.isEmpty() || role == null) {
            showError("All fields are required.");
            return;
        }

        new Thread(() -> {
            try {

                String registerResponseBody = apiService.register(fullName, phone, password, address, role.name());
                JSONObject registerResponseJson = new JSONObject(registerResponseBody);
                String token = registerResponseJson.getString("token");


                String profileResponseBody = apiService.getProfile(token);
                JSONObject userJson = new JSONObject(profileResponseBody);


                Platform.runLater(() -> {
                    try {

                        String status = userJson.optString("status", "VALID");

                        SessionManager.login(
                                token,
                                userJson.getLong("id"),
                                userJson.getString("full_name"),
                                userJson.getString("role"),
                                userJson.optString("profileImageBase64", null),
                                status
                        );
                        switchToDashboard(event);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Failed to switch dashboard after registration.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Registration failed: " + e.getMessage()));
            }
        }).start();
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

    private void clearError() {
        if(errorLabel != null) errorLabel.setText("");
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
            showError("Could not load dashboard for role: " + userRole);
            return;
        }

        Parent root = FXMLLoader.load(fxmlLocation);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}