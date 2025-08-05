package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Order;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

public class AvailableDeliveriesController {

    @FXML private TableView<Order> deliveriesTable;
    @FXML private TableColumn<Order, Long> orderIdColumn;
    @FXML private TableColumn<Order, String> restaurantNameColumn;
    @FXML private TableColumn<Order, String> addressColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Order> deliveryList = FXCollections.observableArrayList();
    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        restaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));

        setupActionsColumn();
        loadAvailableDeliveries();
    }

    private void loadAvailableDeliveries() {
        new Thread(() -> {
            try {
                String response = apiService.getAvailableDeliveries(SessionManager.getAuthToken());
                JSONArray deliveriesJson = new JSONArray(response);

                Platform.runLater(() -> {
                    deliveryList.clear();
                    for (int i = 0; i < deliveriesJson.length(); i++) {
                        JSONObject orderObj = deliveriesJson.getJSONObject(i);
                        deliveryList.add(new Order(
                                orderObj.getLong("id"),
                                orderObj.optString("delivery_address", "No address provided"),
                                orderObj.getLong("customer_id"),
                                orderObj.optString("buyerName", "N/A"),
                                orderObj.getLong("vendor_id"),
                                orderObj.optString("vendorName", "N/A"),
                                orderObj.getDouble("pay_price"),
                                orderObj.getString("status"),
                                orderObj.getString("created_at")
                        ));
                    }
                    deliveriesTable.setItems(deliveryList);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");
            {
                acceptButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());

                    new Thread(() -> {
                        try {
                            apiService.updateDeliveryStatus(SessionManager.getAuthToken(), order.getId(), "ON_THE_WAY");

                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.INFORMATION, "Order " + order.getId() + " accepted!").show();
                                loadAvailableDeliveries();
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.ERROR, "Failed to accept order: " + e.getMessage()).showAndWait();
                            });
                        }
                    }).start();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : acceptButton);
            }
        });
    }

}