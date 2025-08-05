package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Restaurant;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

public class RestaurantManagementController {

    @FXML private TableView<Restaurant> restaurantsTable;
    @FXML private TableColumn<Restaurant, Long> idColumn;
    @FXML private TableColumn<Restaurant, String> nameColumn;
    @FXML private TableColumn<Restaurant, String> addressColumn;
    @FXML private TableColumn<Restaurant, String> phoneColumn;
    @FXML private TableColumn<Restaurant, Void> manageColumn;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Restaurant> restaurantList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        setupManageButtonColumn();
        loadRestaurants();
    }

    private void setupManageButtonColumn() {
        manageColumn.setCellFactory(param -> new TableCell<>() {
            private final Button manageButton = new Button("Manage");

            {
                manageButton.setOnAction(event -> {
                    Restaurant restaurant = getTableView().getItems().get(getIndex());
                    handleManageRestaurant(restaurant);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(manageButton);
                }
            }
        });
    }

    private void handleManageRestaurant(Restaurant restaurant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/restaurant-detail-view.fxml"));
            VBox page = loader.load();

            RestaurantDetailController controller = loader.getController();
            controller.initData(restaurant);
            AnchorPane contentPane = (AnchorPane) restaurantsTable.getParent().getParent();
            contentPane.getChildren().setAll(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadRestaurants() {
        new Thread(() -> {
            try {
                String response = apiService.getMyRestaurants(SessionManager.getAuthToken());
                JSONObject responseJson = new JSONObject(response);
                JSONArray restaurantsJson = responseJson.getJSONArray("restaurants");

                Platform.runLater(() -> {
                    restaurantList.clear();
                    for (int i = 0; i < restaurantsJson.length(); i++) {
                        JSONObject r = restaurantsJson.getJSONObject(i);
                        restaurantList.add(new Restaurant(
                                r.getLong("id"),
                                r.getString("name"),
                                r.getString("address"),
                                r.getString("phone"),
                                r.getDouble("tax_fee"),
                                r.getDouble("additional_fee"),
                                r.optString("logoBase64"),
                                r.optString("averageRating", "N/A")
                        ));

                    }
                    restaurantsTable.setItems(restaurantList);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddNewRestaurant() {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("Add New Restaurant");
        dialog.setHeaderText("Enter the details for the new restaurant.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(); nameField.setPromptText("Name");
        TextField addressField = new TextField(); addressField.setPromptText("Address");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone");
        TextField taxFeeField = new TextField(); taxFeeField.setPromptText("Tax Fee (e.g., 9.0)");
        TextField additionalFeeField = new TextField(); additionalFeeField.setPromptText("Additional Fee (e.g., 5000)");

        ImageView logoView = new ImageView();
        logoView.setFitHeight(80);
        logoView.setFitWidth(80);
        logoView.setPreserveRatio(true);
        Button chooseLogoButton = new Button("Choose Logo");
        final String[] logoBase64 = { "" };

        chooseLogoButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                try {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    logoBase64[0] = Base64.getEncoder().encodeToString(fileContent);
                    logoView.setImage(new Image(new ByteArrayInputStream(fileContent)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1); grid.add(addressField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("Tax Fee (%):"), 0, 3); grid.add(taxFeeField, 1, 3);
        grid.add(new Label("Packaging Fee:"), 0, 4); grid.add(additionalFeeField, 1, 4);
        grid.add(new Label("Logo:"), 0, 5); grid.add(new HBox(10, logoView, chooseLogoButton), 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                JSONObject result = new JSONObject();
                result.put("name", nameField.getText());
                result.put("address", addressField.getText());
                result.put("phone", phoneField.getText());
                result.put("tax_fee", Double.parseDouble(taxFeeField.getText()));
                result.put("additional_fee", Double.parseDouble(additionalFeeField.getText()));
                result.put("logoBase64", logoBase64[0]);
                return result;
            }
            return null;
        });

        Optional<JSONObject> result = dialog.showAndWait();

        result.ifPresent(restaurantData -> {
            new Thread(() -> {
                try {
                    apiService.createRestaurant(SessionManager.getAuthToken(), restaurantData);
                    Platform.runLater(this::loadRestaurants);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}