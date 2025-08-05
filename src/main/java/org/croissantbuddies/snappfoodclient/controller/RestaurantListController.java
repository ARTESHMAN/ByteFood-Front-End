package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Restaurant;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class RestaurantListController {

    @FXML private ListView<Restaurant> restaurantListView;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Restaurant> restaurantList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupCustomListView();
        loadRestaurants();
    }

    private void setupCustomListView() {
        restaurantListView.setCellFactory(param -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final Label ratingLabel = new Label();
            private final Label addressLabel = new Label();
            private final Label taxFeeLabel = new Label();
            private final Label packagingFeeLabel = new Label();
            private final Button viewMenuButton = new Button("View Menu");

            private final ToggleButton favoriteButton = new ToggleButton();


            private final HBox headerBox = new HBox(10);
            private final VBox textVBox = new VBox(5);
            private final HBox contentBox = new HBox(15);

            {
                imageView.setFitHeight(80);
                imageView.setFitWidth(80);
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                ratingLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                addressLabel.setWrapText(true);
                favoriteButton.getStyleClass().add("favorite-toggle-button");


                headerBox.getChildren().addAll(nameLabel, ratingLabel);
                textVBox.getChildren().addAll(headerBox, addressLabel, taxFeeLabel, packagingFeeLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                contentBox.getChildren().addAll(imageView, textVBox, spacer, favoriteButton, viewMenuButton);
                contentBox.setAlignment(Pos.CENTER_LEFT);
                contentBox.setPadding(new Insets(10));
            }

            @Override
            protected void updateItem(Restaurant restaurant, boolean empty) {
                super.updateItem(restaurant, empty);
                if (empty || restaurant == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(restaurant.getName());
                    addressLabel.setText(restaurant.getAddress());
                    taxFeeLabel.setText("Tax: " + restaurant.getTaxFee() + "%");
                    packagingFeeLabel.setText("Packaging Fee: " + restaurant.getAdditionalFee());
                    imageView.setImage(restaurant.getLogo());
                    ratingLabel.setText("⭐ " + restaurant.getAverageRating());

                    viewMenuButton.setOnAction(event -> showRestaurantMenu(restaurant));
                    favoriteButton.setSelected(restaurant.isFavorite());
                    favoriteButton.setOnAction(event -> {
                        boolean isSelected = favoriteButton.isSelected();
                        restaurant.setFavorite(isSelected);
                        updateFavoriteStatus(restaurant, isSelected);
                    });
                    setGraphic(contentBox);
                }
            }
        });
    }

    private void updateFavoriteStatus(Restaurant restaurant, boolean isFavorite) {
        new Thread(() -> {
            try {
                String token = SessionManager.getAuthToken();
                if (isFavorite) {
                    apiService.addToFavorites(token, restaurant.getId());
                } else {
                    apiService.removeFromFavorites(token, restaurant.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    restaurant.setFavorite(!isFavorite);
                });
            }
        }).start();
    }


    private void loadRestaurants() {
        new Thread(() -> {
            try {
                String token = SessionManager.getAuthToken();
                String vendorsResponse = apiService.getAllVendors(token);
                JSONArray restaurantsJson = new JSONArray(vendorsResponse);

                String favoritesResponse = apiService.getFavoriteRestaurants(token);
                JSONArray favoritesJson = new JSONObject(favoritesResponse).getJSONArray("restaurants");
                List<Long> favoriteIds = new ArrayList<>();
                for (int i = 0; i < favoritesJson.length(); i++) {
                    favoriteIds.add(favoritesJson.getJSONObject(i).getLong("id"));
                }
                Platform.runLater(() -> {
                    restaurantList.clear();
                    for (int i = 0; i < restaurantsJson.length(); i++) {
                        JSONObject r = restaurantsJson.getJSONObject(i);
                        long restaurantId = r.getLong("id");
                        Restaurant restaurant = new Restaurant(
                                restaurantId,
                                r.getString("name"),
                                r.getString("address"),
                                r.optString("phone", ""),
                                r.optDouble("tax_fee", 0),
                                r.optDouble("additional_fee", 0),
                                r.optString("logoBase64", ""),
                                r.getString("averageRating")
                        );
                        restaurant.setFavorite(favoriteIds.contains(restaurantId));
                        restaurantList.add(restaurant);
                    }
                    restaurantListView.setItems(restaurantList);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showRestaurantMenu(Restaurant restaurant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/buyer-restaurant-view.fxml"));
            VBox page = loader.load();
            BuyerRestaurantViewController controller = loader.getController();
            controller.initData(restaurant);
            VBox parentContainer = (VBox) restaurantListView.getParent();
            parentContainer.getChildren().setAll(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}