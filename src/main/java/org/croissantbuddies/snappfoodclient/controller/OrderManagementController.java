package org.croissantbuddies.snappfoodclient.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Order;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class OrderManagementController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Long> idColumn;
    @FXML private TableColumn<Order, String> addressColumn;
    @FXML private TableColumn<Order, String> buyerNameColumn;
    @FXML private TableColumn<Order, String> vendorNameColumn;
    @FXML private TableColumn<Order, Double> priceColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> createdAtColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;
    private final ApiService apiService = new ApiService();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private static final Gson gson = new Gson();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        buyerNameColumn.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        vendorNameColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        setupActionsColumn();
        loadOrders();
    }

    private void loadOrders() {
        new Thread(() -> {
            try {
                String authToken = SessionManager.getAuthToken();
                if (authToken == null) return;

                String response = apiService.getOrders(authToken);

                Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                List<Map<String, Object>> orderMaps = gson.fromJson(response, listType);

                Platform.runLater(() -> {
                    orderList.clear();
                    for (Map<String, Object> orderMap : orderMaps) {
                        orderList.add(new Order(
                                getLongFromMap(orderMap, "id"),
                                (String) orderMap.get("delivery_address"),
                                getLongFromMap(orderMap, "customerId"),
                                (String) orderMap.get("buyerName"),
                                getLongFromMap(orderMap, "vendorId"),
                                (String) orderMap.get("vendorName"),
                                getDoubleFromMap(orderMap, "pay_price"),
                                (String) orderMap.get("status"),
                                (String) orderMap.get("created_at")
                        ));
                    }
                    ordersTable.setItems(orderList);
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private double getDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("View Details");
            private final HBox pane = new HBox(5, detailsButton);

            {
                detailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewDetails(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
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
                String response = apiService.getAdminOrderDetails(SessionManager.getAuthToken(), order.getId());
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