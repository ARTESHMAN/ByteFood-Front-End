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

public class MyDeliveriesController {

    @FXML private TableView<Order> historyTable;
    @FXML private TableColumn<Order, Long> orderIdColumn;
    @FXML private TableColumn<Order, String> restaurantNameColumn;
    @FXML private TableColumn<Order, String> addressColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Order> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        restaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        setupActionsColumn();
        loadHistory();
    }


    private void loadHistory() {
        new Thread(() -> {
            try {
                String response = apiService.getDeliveryHistory(SessionManager.getAuthToken());
                JSONArray historyJson = new JSONArray(response);

                Platform.runLater(() -> {
                    historyList.clear();
                    for (int i = 0; i < historyJson.length(); i++) {
                        JSONObject orderObj = historyJson.getJSONObject(i);
                        historyList.add(new Order(
                                orderObj.getLong("id"),
                                orderObj.getString("delivery_address"),
                                0,
                                orderObj.optString("buyerName", "N/A"),
                                orderObj.getLong("vendor_id"),
                                orderObj.optString("vendorName", "N/A"),
                                orderObj.getDouble("pay_price"),
                                orderObj.getString("status"),
                                orderObj.getString("created_at")
                        ));
                    }
                    historyTable.setItems(historyList);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deliveredButton = new Button("Delivered");
            {
                deliveredButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());

                    new Thread(() -> {
                        try {
                            apiService.updateDeliveryStatus(SessionManager.getAuthToken(), order.getId(), "COMPLETED");

                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.INFORMATION, "Order " + order.getId() + " marked as completed!").show();
                                loadHistory();
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to update status: " + e.getMessage()).showAndWait());
                        }
                    }).start();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());

                    if ("ON_THE_WAY".equalsIgnoreCase(order.getStatus())) {
                        setGraphic(deliveredButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }
}