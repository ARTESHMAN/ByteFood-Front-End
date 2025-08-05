package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.CartItemModel;
import org.croissantbuddies.snappfoodclient.model.Coupon;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartViewController {

    @FXML private TableView<CartItemModel> cartTable;
    @FXML private TableColumn<CartItemModel, String> foodNameColumn;
    @FXML private TableColumn<CartItemModel, Integer> quantityColumn;
    @FXML private TableColumn<CartItemModel, Double> priceColumn;
    @FXML private TableColumn<CartItemModel, Double> totalPriceColumn;
    @FXML private TableColumn<CartItemModel, Void> actionsColumn;
    @FXML private Label finalPriceLabel;
    @FXML private Button checkoutButton;
    @FXML private Label subTotalLabel;
    @FXML private Label feesLabel;
    @FXML private TextField couponField;
    @FXML private Label discountLabel;

    private final ApiService apiService = new ApiService();
    private final ObservableList<CartItemModel> cartList = FXCollections.observableArrayList();
    private Coupon appliedCoupon = null;
    private long currentVendorId = -1;

    @FXML
    public void initialize() {
        foodNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        setupActionsColumn();
        loadCartItems();
    }


    @FXML
    private void handleApplyCoupon() {
        String code = couponField.getText();
        if (code == null || code.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Error", "Please enter a coupon code.");
            return;
        }

        if (currentVendorId == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot apply coupon, cart vendor is unknown.");
            return;
        }

        new Thread(() -> {
            try {
                String response = apiService.checkCoupon(SessionManager.getAuthToken(), code, currentVendorId);
                JSONObject couponJson = new JSONObject(response);

                appliedCoupon = new Coupon(
                        couponJson.getLong("id"),
                        couponJson.getString("coupon_code"),
                        couponJson.getString("type"),
                        couponJson.getDouble("value"),
                        couponJson.getDouble("min_price"),
                        couponJson.getInt("user_count"),
                        couponJson.getString("start_date"),
                        couponJson.getString("end_date")
                );

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Coupon applied successfully.");
                    couponField.setDisable(true);
                    loadCartItems();
                });

            } catch (Exception e) {
                appliedCoupon = null;
                String errorMessage = "Invalid coupon code.";
                try {
                    JSONObject errorJson = new JSONObject(e.getMessage().substring(e.getMessage().indexOf("{")));
                    errorMessage = errorJson.getString("error");
                } catch (Exception parseException) {
                }
                final String finalErrorMessage = errorMessage;
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", finalErrorMessage));
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void handleCheckout() {
        if (cartList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Your cart is empty!");
            return;
        }

        if (currentVendorId == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not determine the restaurant for this order.");
            return;
        }

        List<String> choices = new ArrayList<>();
        choices.add("WALLET");
        choices.add("ONLINE");

        ChoiceDialog<String> paymentDialog = new ChoiceDialog<>("WALLET", choices);
        paymentDialog.setTitle("Payment Method");
        paymentDialog.setHeaderText("Please select your payment method.");
        paymentDialog.setContentText("Method:");

        Optional<String> paymentResult = paymentDialog.showAndWait();
        if (paymentResult.isEmpty()) {
            return;
        }
        String paymentMethod = paymentResult.get();

        TextInputDialog addressDialog = new TextInputDialog("Default Address");
        addressDialog.setTitle("Delivery Address");
        addressDialog.setHeaderText("Please confirm your delivery address.");
        addressDialog.setContentText("Address:");

        Optional<String> addressResult = addressDialog.showAndWait();
        if (addressResult.isEmpty() || addressResult.get().isBlank()) {
            return;
        }
        String deliveryAddress = addressResult.get();
        long vendorId = currentVendorId;
        new Thread(() -> {
            try {
                String token = SessionManager.getAuthToken();
                Long couponIdToSend = (appliedCoupon != null) ? appliedCoupon.getId() : null;

                JSONObject body = new JSONObject();
                body.put("delivery_address", deliveryAddress);
                body.put("vendor_id", vendorId);
                body.put("payment_method", paymentMethod);
                if (couponIdToSend != null) {
                    body.put("coupon_id", couponIdToSend);
                }

                System.out.println("Submitting order from Cart Page for vendor_id: " + vendorId);

                String response = apiService.submitOrder(token, body.toString());
                JSONObject responseJson = new JSONObject(response);

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Your order has been submitted successfully!");
                    appliedCoupon = null;
                    couponField.clear();
                    couponField.setDisable(false);
                    loadCartItems();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Order Failed", e.getMessage()));
            }
        }).start();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    CartItemModel item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item.getFoodId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void handleDeleteItem(long foodId) {
        new Thread(() -> {
            try {
                apiService.removeFromCart(SessionManager.getAuthToken(), foodId);
                Platform.runLater(this::loadCartItems);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage()));
            }
        }).start();
    }

    private void loadCartItems() {
        new Thread(() -> {
            try {
                String token = SessionManager.getAuthToken();
                String response = apiService.getCart(token);
                JSONObject responseJson = new JSONObject(response);
                currentVendorId = responseJson.optLong("vendorId", -1);

                JSONArray cartJson = responseJson.getJSONArray("items");
                double subTotal = responseJson.getDouble("subTotal");
                double taxFee = responseJson.getDouble("taxFee");
                double packagingFee = responseJson.getDouble("packagingFee");
                double finalPrice = responseJson.getDouble("finalPrice");
                double discount = 0.0;

                if (appliedCoupon != null && subTotal >= appliedCoupon.getMinPrice()) {
                    if (appliedCoupon.getType().equalsIgnoreCase("percent")) {
                        discount = (subTotal * appliedCoupon.getValue()) / 100.0;
                    } else {
                        discount = appliedCoupon.getValue();
                    }
                    finalPrice -= discount;
                    if (finalPrice < 0) {
                        finalPrice = 0;
                    }
                }

                final double finalPriceForUI = finalPrice;
                final double finalDiscountForUI = discount;

                Platform.runLater(() -> {
                    cartList.clear();
                    for (int i = 0; i < cartJson.length(); i++) {
                        JSONObject itemObj = cartJson.getJSONObject(i);
                        cartList.add(new CartItemModel(
                                itemObj.getLong("foodId"),
                                itemObj.getString("foodName"),
                                itemObj.getInt("quantity"),
                                itemObj.getDouble("price")
                        ));
                    }
                    cartTable.setItems(cartList);
                    subTotalLabel.setText(String.format("Subtotal: %.2f", subTotal));
                    feesLabel.setText(String.format("Tax & Packaging: %.2f", taxFee + packagingFee));

                    if (finalDiscountForUI > 0 && discountLabel != null) {
                        discountLabel.setText(String.format("Discount: -%.2f", finalDiscountForUI));
                        discountLabel.setVisible(true);
                    } else if (discountLabel != null){
                        discountLabel.setVisible(false);
                    }

                    finalPriceLabel.setText(String.format("Total: %.2f", finalPriceForUI));
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not load cart: " + e.getMessage());
                });
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
}