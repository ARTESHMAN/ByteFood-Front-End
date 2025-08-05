package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class ProfileController {

    @FXML
    private TextField fullNameField, phoneField, emailField, addressField, bankNameField, accountNumberField;
    @FXML
    private Label statusLabel;
    @FXML private Label currentBalanceLabel;
    @FXML private TextField topUpAmountField;
    private final ApiService apiService = new ApiService();
    private String newProfileImageBase64 = null;
    private String existingProfileImageBase64 = null;
    @FXML private ImageView profileImageView;


    @FXML
    public void initialize() {
        loadProfileData();
    }

    private void loadProfileData() {
        new Thread(() -> {
            try {
                String response = apiService.getProfile(SessionManager.getAuthToken());
                JSONObject userJson = new JSONObject(response);

                Platform.runLater(() -> {
                    fullNameField.setText(userJson.optString("full_name"));
                    phoneField.setText(userJson.optString("phone"));
                    emailField.setText(userJson.optString("email"));
                    addressField.setText(userJson.optString("address"));

                    JSONObject bankInfo = userJson.optJSONObject("bank_info");
                    if (bankInfo != null) {
                        bankNameField.setText(bankInfo.optString("bank_name"));
                        accountNumberField.setText(bankInfo.optString("account_number"));
                    }
                    double currentAmount = userJson.optDouble("amount", 0.0);
                    currentBalanceLabel.setText(String.format("Current Balance: %,.2f", currentAmount));

                    existingProfileImageBase64 = userJson.optString("profileImageBase64", null); // این خط را اضافه کنید
                    if (existingProfileImageBase64 != null && !existingProfileImageBase64.isEmpty()) {
                        byte[] imageBytes = Base64.getDecoder().decode(existingProfileImageBase64);
                        profileImageView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                newProfileImageBase64 = Base64.getEncoder().encodeToString(fileContent);
                profileImageView.setImage(new Image(new ByteArrayInputStream(fileContent)));
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image file.");
            }
        }
    }

    @FXML
    private void handleSaveChanges() {
        JSONObject profileData = new JSONObject();
        profileData.put("full_name", fullNameField.getText());
        profileData.put("phone", phoneField.getText());
        profileData.put("email", emailField.getText());
        profileData.put("address", addressField.getText());
        JSONObject bankInfo = new JSONObject();
        bankInfo.put("bank_name", bankNameField.getText());
        bankInfo.put("account_number", accountNumberField.getText());
        profileData.put("bank_info", bankInfo);

        if (newProfileImageBase64 != null) {
            profileData.put("profileImageBase64", newProfileImageBase64);
        } else if (existingProfileImageBase64 != null) {
            profileData.put("profileImageBase64", existingProfileImageBase64);
        }


        new Thread(() -> {
            try {
                apiService.updateProfile(SessionManager.getAuthToken(), profileData);
                Platform.runLater(() -> {
                    SessionManager.updateName(fullNameField.getText());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleTopUpWallet() {
        String amountText = topUpAmountField.getText();
        if (amountText == null || amountText.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter an amount to top up.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Amount must be a positive number.");
                return;
            }

            new Thread(() -> {
                try {
                    JSONObject response = apiService.topUpWallet(SessionManager.getAuthToken(), amount);
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", response.getString("message"));
                        topUpAmountField.clear();
                        loadProfileData();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()));
                    e.printStackTrace();
                }
            }).start();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for the amount.");
        }
    }

}