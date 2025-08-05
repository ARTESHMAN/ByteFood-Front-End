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

public class FavoriteRestaurantsController {

    @FXML private ListView<Restaurant> favoriteListView;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Restaurant> favoriteList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupCustomListView();
        loadFavorites();
    }

    private void loadFavorites() {
        new Thread(() -> {
            try {
                String token = SessionManager.getAuthToken();
                String favoritesResponse = apiService.getFavoriteRestaurants(token);
                JSONArray restaurantsJson = new JSONObject(favoritesResponse).getJSONArray("restaurants");

                Platform.runLater(() -> {
                    favoriteList.clear();
                    for (int i = 0; i < restaurantsJson.length(); i++) {
                        JSONObject r = restaurantsJson.getJSONObject(i);
                        Restaurant restaurant = new Restaurant(
                                r.getLong("id"),
                                r.getString("name"),
                                r.getString("address"),
                                r.optString("phone", ""),
                                r.optDouble("tax_fee", 0),
                                r.optDouble("additional_fee", 0),
                                r.optString("logoBase64", ""),
                                "N/A"
                        );
                        restaurant.setFavorite(true);
                        favoriteList.add(restaurant);
                    }
                    favoriteListView.setItems(favoriteList);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupCustomListView() {
        favoriteListView.setCellFactory(param -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final Label addressLabel = new Label();
            private final Label taxFeeLabel = new Label();
            private final Label packagingFeeLabel = new Label();
            private final Button viewMenuButton = new Button("View Menu");
            private final ToggleButton favoriteButton = new ToggleButton();
            private final VBox textVBox = new VBox(5);
            private final HBox contentBox = new HBox(15);

            {
                imageView.setFitHeight(80);
                imageView.setFitWidth(80);
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                addressLabel.setWrapText(true);
                favoriteButton.getStyleClass().add("favorite-toggle-button");
                textVBox.getChildren().addAll(nameLabel, addressLabel, taxFeeLabel, packagingFeeLabel);

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
                    viewMenuButton.setOnAction(event -> showRestaurantMenu(restaurant));

                    favoriteButton.setSelected(restaurant.isFavorite());
                    favoriteButton.setOnAction(event -> {
                        updateFavoriteStatus(restaurant);
                    });
                    setGraphic(contentBox);
                }
            }
        });
    }

    private void updateFavoriteStatus(Restaurant restaurant) {
        new Thread(() -> {
            try {
                String token = SessionManager.getAuthToken();
                apiService.removeFromFavorites(token, restaurant.getId());
                Platform.runLater(this::loadFavorites);
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
            VBox parentContainer = (VBox) favoriteListView.getParent();
            parentContainer.getChildren().setAll(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}