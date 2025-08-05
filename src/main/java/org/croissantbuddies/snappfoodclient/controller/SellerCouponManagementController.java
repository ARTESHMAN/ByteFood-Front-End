
package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Coupon;
import org.croissantbuddies.snappfoodclient.model.Restaurant;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This is the new controller for sellers
public class SellerCouponManagementController {

    @FXML private TableView<Coupon> couponsTable;
    @FXML private TableColumn<Coupon, Long> idColumn;
    @FXML private TableColumn<Coupon, String> codeColumn;
    @FXML private TableColumn<Coupon, String> typeColumn;
    @FXML private TableColumn<Coupon, Double> valueColumn;
    @FXML private TableColumn<Coupon, Double> minPriceColumn;
    @FXML private TableColumn<Coupon, Integer> countColumn;
    @FXML private TableColumn<Coupon, String> startColumn;
    @FXML private TableColumn<Coupon, String> endColumn;
    @FXML private TableColumn<Coupon, Void> actionsColumn;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Coupon> couponList = FXCollections.observableArrayList();
    private Restaurant currentRestaurant;

    public void initData(Restaurant restaurant) {
        this.currentRestaurant = restaurant;
        loadCoupons();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("couponCode"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        minPriceColumn.setCellValueFactory(new PropertyValueFactory<>("minPrice"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("userCount"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            {
                pane.setAlignment(Pos.CENTER);
                // TODO: Add edit/delete functionality if needed
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadCoupons() {
        new Thread(() -> {
            try {
                String authToken = SessionManager.getAuthToken();
                if (authToken == null || currentRestaurant == null) return;

                String response = apiService.getRestaurantCoupons(authToken, currentRestaurant.getId()); //

                JSONArray couponsJson = new JSONArray(response);
                List<Coupon> tempList = new ArrayList<>();
                for (int i = 0; i < couponsJson.length(); i++) {
                    JSONObject couponObj = couponsJson.getJSONObject(i);
                    tempList.add(new Coupon(
                            couponObj.getLong("id"),
                            couponObj.getString("coupon_code"),
                            couponObj.getString("type"),
                            couponObj.getDouble("value"),
                            couponObj.getDouble("min_price"),
                            couponObj.getInt("user_count"),
                            couponObj.getString("start_date"),
                            couponObj.getString("end_date")
                    ));
                }
                Platform.runLater(() -> {
                    couponList.setAll(tempList);
                    couponsTable.setItems(couponList);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddNewCoupon() {
        showCouponDialog(null);
    }

    private void showCouponDialog(Coupon couponToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/coupon-dialog.fxml")); //
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(couponToEdit == null ? "Add New Coupon" : "Edit Coupon");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            CouponDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);



            dialogStage.showAndWait();

            if (controller.isSaved()) {
                Map<String, Object> couponMap = new HashMap<>();
                couponMap.put("coupon_code", controller.codeField.getText());
                couponMap.put("type", controller.typeComboBox.getValue());
                couponMap.put("value", Double.parseDouble(controller.valueField.getText()));
                couponMap.put("min_price", Double.parseDouble(controller.minPriceField.getText()));
                couponMap.put("user_count", Integer.parseInt(controller.userCountField.getText()));
                couponMap.put("start_date", controller.startDatePicker.getValue().toString());
                couponMap.put("end_date", controller.endDatePicker.getValue().toString());
                JSONObject couponJson = new JSONObject(couponMap);

                new Thread(() -> {
                    try {
                        String authToken = SessionManager.getAuthToken();

                        apiService.createRestaurantCoupon(authToken, currentRestaurant.getId(), couponJson);
                        Platform.runLater(this::loadCoupons);
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Operation Failed", e.getMessage()));
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}