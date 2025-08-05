package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Restaurant;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BuyerRestaurantViewController {

    @FXML private Label restaurantNameLabel;
    @FXML private VBox menuVBox;
    @FXML private Button backButton;
    @FXML private Label avgRatingLabel;
    @FXML private VBox reviewsContainer;

    private final ApiService apiService = new ApiService();
    private Restaurant restaurant;

    public void initData(Restaurant restaurant) {
        this.restaurant = restaurant;
        restaurantNameLabel.setText(restaurant.getName());
        loadMenu();
        loadRatings();
    }

    private void loadRatings() {
        new Thread(() -> {
            try {
                String responseString = apiService.getRestaurantRatings(SessionManager.getAuthToken(), restaurant.getId());
                JSONObject responseJson = new JSONObject(responseString);
                String avgRating = responseJson.getString("averageRating");
                JSONArray ratingsArray = responseJson.getJSONArray("ratings");

                Platform.runLater(() -> {
                    avgRatingLabel.setText("⭐ " + avgRating);
                    reviewsContainer.getChildren().clear();
                    for (int i = 0; i < ratingsArray.length(); i++) {
                        JSONObject ratingObj = ratingsArray.getJSONObject(i);
                        reviewsContainer.getChildren().add(createReviewBox(ratingObj));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createReviewBox(JSONObject ratingObj) {
        VBox reviewBox = new VBox(5);
        reviewBox.setPadding(new Insets(10));
        reviewBox.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 5;");

        Label userNameLabel = new Label(ratingObj.getString("userName"));
        userNameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label ratingLabel = new Label("Rating: " + ratingObj.getInt("rating") + " / 5");
        Label commentLabel = new Label(ratingObj.getString("comment"));
        commentLabel.setWrapText(true);

        reviewBox.getChildren().addAll(userNameLabel, ratingLabel, commentLabel);
        if (ratingObj.has("orderedItems")) {
            JSONArray itemsArray = ratingObj.getJSONArray("orderedItems");
            if (itemsArray.length() > 0) {
                StringBuilder itemsText = new StringBuilder("Ordered: ");
                for (int i = 0; i < itemsArray.length(); i++) {
                    itemsText.append(itemsArray.getString(i));
                    if (i < itemsArray.length() - 1) {
                        itemsText.append(", ");
                    }
                }
                Label itemsLabel = new Label(itemsText.toString());
                itemsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: grey;");
                reviewBox.getChildren().add(itemsLabel);
            }
        }
        if (ratingObj.has("imageBase64")) {
            HBox imageContainer = new HBox(5);
            JSONArray imagesJson = ratingObj.getJSONArray("imageBase64");
            for (int i = 0; i < imagesJson.length(); i++) {
                String base64Image = imagesJson.getString(i);
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(80);
                    imageView.setFitWidth(80);
                    imageView.setPreserveRatio(true);
                    imageContainer.getChildren().add(imageView);
                } catch (Exception e) {
                    System.err.println("Could not decode image for review.");
                }
            }
            reviewBox.getChildren().add(imageContainer);
        }

        return reviewBox;
    }

    private void loadMenu() {
        new Thread(() -> {
            try {
                String responseString = apiService.getVendorDetails(SessionManager.getAuthToken(), restaurant.getId());
                JSONObject responseJson = new JSONObject(responseString);
                JSONArray menusArray = responseJson.getJSONArray("menus");

                Platform.runLater(() -> {
                    menuVBox.getChildren().clear();
                    for (int i = 0; i < menusArray.length(); i++) {
                        JSONObject menuObj = menusArray.getJSONObject(i);
                        String menuTitle = menuObj.getString("title");
                        JSONArray itemsArray = menuObj.getJSONArray("items");
                        TitledPane menuPane = new TitledPane(menuTitle, createFoodGrid(itemsArray));
                        menuPane.setExpanded(true);
                        menuVBox.getChildren().add(menuPane);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private GridPane createFoodGrid(JSONArray itemsArray) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject foodObj = itemsArray.getJSONObject(i);
            Label nameLabel = new Label(foodObj.getString("name"));
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label descLabel = new Label(foodObj.getString("description"));
            descLabel.setWrapText(true);
            Label priceLabel = new Label("Price: " + foodObj.getDouble("price"));
            Button addButton = new Button("Add to Cart");

            // --- Logic to create the keywords label ---
            Label keywordsLabel = new Label();
            JSONArray keywordsJson = foodObj.optJSONArray("keywords");
            if (keywordsJson != null && keywordsJson.length() > 0) {
                List<String> keywordsList = new ArrayList<>();
                for (int j = 0; j < keywordsJson.length(); j++) {
                    keywordsList.add(keywordsJson.getString(j));
                }
                keywordsLabel.setText("Keywords: " + String.join(", ", keywordsList));
                keywordsLabel.setStyle("-fx-text-fill: grey; -fx-font-style: italic;");
            }

            addButton.setOnAction(e -> {
                long foodId = foodObj.getLong("id");
                new Thread(() -> {
                    try {
                        apiService.addToCart(SessionManager.getAuthToken(), foodId, 1);
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setContentText("Item added to your cart!");
                            alert.showAndWait();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            });

            VBox foodBox = new VBox(5);

            if (foodObj.has("imageBase64") && !foodObj.getString("imageBase64").isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(foodObj.getString("imageBase64"));
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    imageView.setPreserveRatio(true);
                    foodBox.getChildren().add(imageView);
                } catch (Exception e) {
                    System.err.println("Could not decode image for food item: " + foodObj.getString("name"));
                }
            }

            foodBox.getChildren().addAll(nameLabel, descLabel, priceLabel, keywordsLabel, addButton);
            grid.add(foodBox, 0, i);
        }
        return grid;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            AnchorPane contentPane = (AnchorPane) restaurantNameLabel.getScene().lookup("#contentPane");

            if (contentPane != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/restaurant-list-view.fxml"));
                Node restaurantListPage = loader.load();
                contentPane.getChildren().setAll(restaurantListPage);
            } else {
                System.err.println("Error in handleBack: Could not find #contentPane in the scene.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}