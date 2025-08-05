// File: FoodSearchController.java

package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Food;
import org.croissantbuddies.snappfoodclient.model.Restaurant;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FoodSearchController {

    @FXML private TextField searchField;
    @FXML private TextField keywordsField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ListView<Food> resultsListView;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Food> foodList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        resultsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Food food, boolean empty) {
                super.updateItem(food, empty);
                if (empty || food == null) {
                    setGraphic(null);
                } else {

                    ImageView foodImageView = new ImageView(food.getImage());
                    foodImageView.setFitHeight(60);
                    foodImageView.setFitWidth(60);

                    VBox detailsVBox = new VBox(5);
                    Label nameLabel = new Label(food.getName() + " - Price: " + food.getPrice());
                    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                    Hyperlink restaurantLink = new Hyperlink("From: " + food.getRestaurantName());
                    restaurantLink.setOnAction(e -> goToRestaurant(food.getRestaurantId(), food.getRestaurantName()));

                    Button addToCartButton = new Button("Add to Cart");
                    addToCartButton.setOnAction(e -> handleAddToCart(food.getId()));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    detailsVBox.getChildren().addAll(nameLabel, restaurantLink);

                    HBox mainHBox = new HBox(10, foodImageView, detailsVBox, spacer, addToCartButton);

                    mainHBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(mainHBox);
                }
            }
        });
        resultsListView.setItems(foodList);
    }

    private void goToRestaurant(long restaurantId, String restaurantName) {
        try {

            Restaurant restaurant = new Restaurant(restaurantId, restaurantName, "", "", 0, 0, "", "");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/buyer-restaurant-view.fxml"));
            Node page = loader.load();

            BuyerRestaurantViewController controller = loader.getController();
            controller.initData(restaurant);

            AnchorPane contentPane = (AnchorPane) resultsListView.getScene().lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(page);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAddToCart(long foodId) {
        new Thread(() -> {
            try {
                apiService.addToCart(SessionManager.getAuthToken(), foodId, 1);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Item added to cart!");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @FXML
    private void handleSearch() {
        String search = searchField.getText();
        String keywordsText = keywordsField.getText();
        String minPriceText = minPriceField.getText();
        String maxPriceText = maxPriceField.getText();

        List<String> keywords = new ArrayList<>();
        if (keywordsText != null && !keywordsText.trim().isEmpty()) {
            keywords.addAll(Arrays.asList(keywordsText.split("\\s*,\\s*")));
        }

        Integer minPrice = null;
        Integer maxPrice = null;
        try {
            if (minPriceText != null && !minPriceText.isEmpty()) minPrice = Integer.parseInt(minPriceText);
            if (maxPriceText != null && !maxPriceText.isEmpty()) maxPrice = Integer.parseInt(maxPriceText);
        } catch (NumberFormatException e) {

        }

        Integer finalMinPrice = minPrice;
        Integer finalMaxPrice = maxPrice;
        new Thread(() -> {
            try {
                String token = SessionManager.getAuthToken();
                String response = apiService.searchFoodItems(token, search, finalMinPrice, finalMaxPrice, keywords);
                JSONArray itemsJson = new JSONArray(response);

                Platform.runLater(() -> {
                    foodList.clear();
                    for (int i = 0; i < itemsJson.length(); i++) {
                        JSONObject item = itemsJson.getJSONObject(i);

                        String restaurantName = "Unknown";
                        long restaurantId = -1;
                        if (item.has("restaurant")) {
                            JSONObject restaurantObj = item.getJSONObject("restaurant");
                            restaurantName = restaurantObj.optString("name", "Unknown");
                            restaurantId = restaurantObj.optLong("id", -1);
                        }
                        foodList.add(new Food(
                                item.getLong("id"),
                                item.getString("name"),
                                item.getString("description"),
                                item.getInt("price"),
                                item.getInt("supply"),
                                item.getJSONArray("keywords").toString(),
                                item.optString("imageBase64", ""),
                                restaurantName,
                                restaurantId
                        ));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}