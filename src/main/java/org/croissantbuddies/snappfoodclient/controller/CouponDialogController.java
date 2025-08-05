package org.croissantbuddies.snappfoodclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.time.LocalDate;

public class CouponDialogController {

    @FXML public TextField codeField;
    @FXML public ComboBox<String> typeComboBox;
    @FXML public TextField valueField;
    @FXML public TextField minPriceField;
    @FXML public TextField userCountField;
    @FXML public DatePicker startDatePicker;
    @FXML public DatePicker endDatePicker;

    private Stage dialogStage;
    private boolean saved = false;
    private JSONObject couponData;

    @FXML
    public void initialize() {
        typeComboBox.getItems().setAll("fixed", "percent");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCouponData(String code, String type, double value, double minPrice, int count, String start, String end) {
        codeField.setText(code);
        typeComboBox.setValue(type);
        valueField.setText(String.valueOf(value));
        minPriceField.setText(String.valueOf(minPrice));
        userCountField.setText(String.valueOf(count));
        startDatePicker.setValue(LocalDate.parse(start));
        endDatePicker.setValue(LocalDate.parse(end));
    }

    public boolean isSaved() {
        return saved;
    }
    public JSONObject getCouponData() {
        return couponData;
    }
    public void setCouponData(JSONObject couponData) {
        this.couponData = couponData;
        codeField.setText(couponData.optString("coupon_code"));
        typeComboBox.setValue(couponData.optString("type"));
        valueField.setText(String.valueOf(couponData.optDouble("value")));
        minPriceField.setText(String.valueOf(couponData.optDouble("min_price")));
        userCountField.setText(String.valueOf(couponData.optInt("user_count")));
        startDatePicker.setValue(LocalDate.parse(couponData.optString("start_date")));
        endDatePicker.setValue(LocalDate.parse(couponData.optString("end_date")));
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            saved = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            errorMessage.append("Coupon code is required.\n");
        }
        if (typeComboBox.getValue() == null) {
            errorMessage.append("Type is required.\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }
}