package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Order;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class OrderHistoryController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Long> idColumn;
    @FXML private TableColumn<Order, String> vendorNameColumn;
    @FXML private TableColumn<Order, Double> priceColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> createdAtColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        vendorNameColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        setupActionsColumn();

        statusFilterComboBox.getItems().addAll("ALL", "COMPLETED", "ON_THE_WAY", "CANCELLED", "SUBMITTED");

        loadOrderHistory(null, null, null);
    }

    @FXML
    private void handleFilter() {
        String selectedStatus = statusFilterComboBox.getValue();
        if ("ALL".equalsIgnoreCase(selectedStatus)) {
            selectedStatus = null;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        loadOrderHistory(selectedStatus, startDate, endDate);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button rateButton = new Button("Rate Order");
            private final Button detailsButton = new Button("View Details");
            private final HBox pane = new HBox(5);
            {
                rateButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showRatingDialog(order);
                });
                detailsButton.setOnAction(event -> {
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
                    pane.getChildren().clear();
                    pane.getChildren().add(detailsButton);
                    Order order = getTableView().getItems().get(getIndex());
                    if ("COMPLETED".equalsIgnoreCase(order.getStatus()) && !order.isRated()) {
                        pane.getChildren().add(rateButton);
                    }
                    setGraphic(pane);
                }
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
                String response = apiService.getOrderDetails(SessionManager.getAuthToken(), order.getId());
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

    private void showRatingDialog(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rate Order #" + order.getId());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Slider ratingSlider = new Slider(1, 5, 3);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your review here...");

        Button chooseImageButton = new Button("Choose Images");
        ListView<String> selectedFilesView = new ListView<>();
        selectedFilesView.setPrefHeight(80);
        List<String> imageBase64List = new ArrayList<>();

        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Images");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dialog.getDialogPane().getScene().getWindow());

            if (selectedFiles != null) {
                selectedFilesView.getItems().clear();
                imageBase64List.clear();
                for (File file : selectedFiles) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        imageBase64List.add(Base64.getEncoder().encodeToString(fileContent));
                        selectedFilesView.getItems().add(file.getName());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        grid.add(new Label("Rating:"), 0, 0);
        grid.add(ratingSlider, 1, 0);
        grid.add(new Label("Comment:"), 0, 1);
        grid.add(commentArea, 1, 1);
        grid.add(new Label("Images:"), 0, 2);
        grid.add(new VBox(5, chooseImageButton, selectedFilesView), 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            int rating = (int) ratingSlider.getValue();
            String comment = commentArea.getText();

            new Thread(() -> {
                try {
                    apiService.submitRating(SessionManager.getAuthToken(), order.getId(), rating, comment, imageBase64List);
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Thank you for your feedback!");
                        handleFilter();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit rating: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void loadOrderHistory(String statusFilter, LocalDate startDate, LocalDate endDate) {
        new Thread(() -> {
            try {
                String response = apiService.getOrderHistory(SessionManager.getAuthToken(), statusFilter, startDate, endDate);
                JSONArray ordersJson = new JSONArray(response);
                List<Long> ratedOrderIds = apiService.getRatedOrderIds(SessionManager.getAuthToken());

                Platform.runLater(() -> {
                    orderList.clear();
                    for (int i = 0; i < ordersJson.length(); i++) {
                        JSONObject orderObj = ordersJson.getJSONObject(i);
                        Order order = new Order(
                                orderObj.getLong("id"),
                                "",
                                0,
                                SessionManager.getUserFullName(),
                                orderObj.getLong("vendor_id"),
                                orderObj.optString("vendorName", "N/A"),
                                orderObj.getDouble("pay_price"),
                                orderObj.getString("status"),
                                orderObj.getString("created_at")
                        );

                        if (ratedOrderIds.contains(order.getId())) {
                            order.setRated(true);
                        }
                        orderList.add(order);
                    }
                    ordersTable.setItems(orderList);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}