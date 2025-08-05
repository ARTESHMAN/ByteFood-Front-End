package org.croissantbuddies.snappfoodclient.controller;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;



import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Order;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

public class SellerOrderManagementController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Long> idColumn;
    @FXML private TableColumn<Order, String> restaurantNameColumn;
    @FXML private TableColumn<Order, Double> priceColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> createdAtColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;


    private final ApiService apiService = new ApiService();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        restaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        setupActionsColumn();
        loadOrders();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {

            private final Button acceptButton = new Button("Accept");
            private final Button preparingButton = new Button("Preparing");
            private final Button deliverButton = new Button("Deliver to Courier");
            private final Button viewDetailsButton = new Button("View Details");
            private final HBox pane = new HBox(5);

            {
                acceptButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white;");
                preparingButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white;");
                deliverButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;");

                acceptButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(order, "ACCEPTED");
                });

                preparingButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(order, "PREPARING");
                });

                deliverButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(order, "AWAITING_COURIER_PICKUP");
                });
                viewDetailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewDetails(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    String status = order.getStatus();
                    pane.getChildren().clear();
                    pane.getChildren().add(viewDetailsButton);
                    if ("SUBMITTED".equalsIgnoreCase(status) || "WAITING_VENDOR".equalsIgnoreCase(status)) {
                        pane.getChildren().add(acceptButton);
                    } else if ("ACCEPTED".equalsIgnoreCase(status)) {
                        pane.getChildren().add(preparingButton);
                    } else if ("PREPARING".equalsIgnoreCase(status)) {
                        pane.getChildren().add(deliverButton);
                    }
                    setGraphic(pane);
                }
            }
        });
    }
    private void handleUpdateStatus(Order order, String newStatus) {
        new Thread(() -> {
            try {
                apiService.updateOrderStatus(SessionManager.getAuthToken(), order.getId(), newStatus);
                Platform.runLater(this::loadOrders); // Refresh the table to show the new status
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Update Failed: " + e.getMessage()).showAndWait());
            }
        }).start();
    }

    private void loadOrders() {
        new Thread(() -> {
            try {
                String response = apiService.getSellerOrders(SessionManager.getAuthToken());
                JSONArray ordersJson = new JSONArray(response);

                Platform.runLater(() -> {
                    orderList.clear();
                    for (int i = 0; i < ordersJson.length(); i++) {
                        JSONObject orderObj = ordersJson.getJSONObject(i);
                        orderList.add(new Order(
                                orderObj.getLong("id"),
                                orderObj.getString("delivery_address"),
                                orderObj.getLong("customer_id"),
                                orderObj.optString("buyerName", "N/A"),
                                orderObj.getLong("vendor_id"),
                                orderObj.optString("vendorName", "N/A"),
                                orderObj.getDouble("pay_price"),
                                orderObj.getString("status"),
                                orderObj.getString("created_at")
                        ));
                    }
                    ordersTable.setItems(orderList);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void handleViewDetails(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Details for Order #" + order.getId());
        dialog.setHeaderText("Items in this order:");

        ListView<String> listView = new ListView<>();
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        new Thread(() -> {
            try {
                String response = apiService.getSellerOrderDetails(SessionManager.getAuthToken(), order.getId());
                JSONArray itemsJson = new JSONArray(response);
                ObservableList<String> items = FXCollections.observableArrayList();
                for (int i = 0; i < itemsJson.length(); i++) {
                    JSONObject itemObj = itemsJson.getJSONObject(i);
                    String itemText = String.format("%d x %s (Price: %.2f)",
                            itemObj.getInt("quantity"),
                            itemObj.getString("foodName"),
                            itemObj.getDouble("price")
                    );
                    items.add(itemText);
                }
                Platform.runLater(() -> listView.setItems(items));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Could not load order details.").show());
            }
        }).start();
        dialog.showAndWait();
    }
}