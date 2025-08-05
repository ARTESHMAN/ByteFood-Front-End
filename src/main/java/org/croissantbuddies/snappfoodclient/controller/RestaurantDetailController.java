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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Food;
import org.croissantbuddies.snappfoodclient.model.Restaurant;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.croissantbuddies.snappfoodclient.model.Menu;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class RestaurantDetailController {

    @FXML
    private Label restaurantNameLabel;
    @FXML
    private TableView<Food> foodTableView;
    @FXML
    private TableColumn<Food, String> foodNameColumn;
    @FXML
    private TableColumn<Food, Double> foodPriceColumn;
    @FXML
    private TableColumn<Food, Integer> foodSupplyColumn;
    @FXML
    private TableColumn<Food, String> foodKeyWordColumn;
    @FXML
    private TableColumn<Food, Void> foodActionsColumn;
    @FXML private TableView<Menu> menuTableView;
    @FXML private TableColumn<Menu, String> menuNameColumn;
    @FXML private TableColumn<Menu, Void> menuActionsColumn;

    private Restaurant selectedRestaurant;
    private final ApiService apiService = new ApiService();
    private final ObservableList<Food> foodList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        foodNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        foodPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        foodSupplyColumn.setCellValueFactory(new PropertyValueFactory<>("supply"));
        menuNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        foodKeyWordColumn.setCellValueFactory(new PropertyValueFactory<>("keyWords"));
        setupMenuActionsColumn();

        foodActionsColumn.setCellFactory(param -> new TableCell<>() {

            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            {
                pane.setAlignment(Pos.CENTER);
                editButton.setOnAction(event -> {
                    Food food = getTableView().getItems().get(getIndex());
                    handleEditFoodItem(food);
                });
                deleteButton.setOnAction(event -> {
                    Food food = getTableView().getItems().get(getIndex());
                    handleDeleteFoodItem(food);
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }

        });

        foodTableView.setItems(foodList);
    }

    public void initData(Restaurant restaurant) {
        selectedRestaurant = restaurant;
        restaurantNameLabel.setText(selectedRestaurant.getName());
        loadRestaurantDetails();
    }

    private void loadRestaurantDetails() {
        new Thread(() -> {
            try {
                String detailsResponse = apiService.getRestaurantDetails(SessionManager.getAuthToken(), selectedRestaurant.getId());
                JSONObject detailsJson = new JSONObject(detailsResponse);

                List<Food> allFoods = new ArrayList<>();
                JSONArray foodsJson = detailsJson.getJSONArray("foods");
                for (int i = 0; i < foodsJson.length(); i++) {
                    JSONObject foodObj = foodsJson.getJSONObject(i);
                    JSONArray keywordsJson = foodObj.optJSONArray("keywords");
                    String keywords = "";
                    if (keywordsJson != null) {
                        List<String> keywordsList = new ArrayList<>();
                        for (int j = 0; j < keywordsJson.length(); j++) {
                            keywordsList.add(keywordsJson.getString(j));
                        }
                        keywords = String.join(", ", keywordsList);
                    }

                    allFoods.add(new Food(
                            foodObj.getLong("id"),
                            foodObj.getString("name"),
                            foodObj.optString("description", ""),
                            foodObj.getDouble("price"),
                            foodObj.getInt("supply"),
                            keywords,
                            foodObj.optString("imageBase64", ""),
                            selectedRestaurant.getName(),
                            selectedRestaurant.getId()
                    ));
                }

                String menusResponse = apiService.getMenusForRestaurant(SessionManager.getAuthToken(), selectedRestaurant.getId());

                JSONArray menusJson = new JSONArray(menusResponse);
                List<Menu> allMenus = new ArrayList<>();

                for (int i = 0; i < menusJson.length(); i++) {
                    JSONObject menuObj = menusJson.getJSONObject(i);
                    allMenus.add(new Menu(
                            menuObj.getLong("id"),
                            menuObj.getString("title")
                    ));
                }
                Platform.runLater(() -> {
                    foodList.setAll(allFoods);
                    menuTableView.setItems(FXCollections.observableArrayList(allMenus));
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "Could not load restaurant details."));
            }
        }).start();
    }

    private void showAddFoodToMenuDialog(Menu menu) {
        new Thread(() -> {
            try {
                String response = apiService.getFoodsInMenu(SessionManager.getAuthToken(), selectedRestaurant.getId(), menu.getId());
                JSONArray currentItemsJson = new JSONArray(response);
                List<Food> currentMenuItems = new ArrayList<>();
                for (int i = 0; i < currentItemsJson.length(); i++) {
                    JSONObject foodObj = currentItemsJson.getJSONObject(i);

                    currentMenuItems.add(new Food(
                            foodObj.getLong("id"),
                            foodObj.getString("name"),
                            "",
                            0,
                            0,
                            "",
                            foodObj.optString("imageBase64", ""),
                            selectedRestaurant.getName(),
                            selectedRestaurant.getId()
                    ));

                }

                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/manage-menu-items-dialog.fxml"));
                        BorderPane page = loader.load();

                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("Manage Items for '" + menu.getName() + "'");
                        dialogStage.initModality(Modality.WINDOW_MODAL);
                        dialogStage.initOwner(menuTableView.getScene().getWindow());
                        Scene scene = new Scene(page);
                        dialogStage.setScene(scene);

                        ManageMenuItemsDialogController controller = loader.getController();
                        controller.setDialogStage(dialogStage);
                        controller.setData(new ArrayList<>(foodList), currentMenuItems);

                        dialogStage.showAndWait();

                        List<Food> finalSelectedFoods = controller.getSelectedFoods();

                        if (finalSelectedFoods == null) {
                            return;
                        }

                        updateMenuItems(menu, currentMenuItems, finalSelectedFoods);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Could not fetch current menu items."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void handleEditRestaurant(ActionEvent event) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("Edit Restaurant Info");
        dialog.setHeaderText("Update the details for '" + selectedRestaurant.getName() + "'.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedRestaurant.getName());
        TextField addressField = new TextField(selectedRestaurant.getAddress());
        TextField phoneField = new TextField(selectedRestaurant.getPhone());
        TextField taxFeeField = new TextField(String.valueOf(selectedRestaurant.getTaxFee()));
        TextField additionalFeeField = new TextField(String.valueOf(selectedRestaurant.getAdditionalFee()));

        ImageView logoView = new ImageView();
        logoView.setFitHeight(80);
        logoView.setFitWidth(80);
        logoView.setPreserveRatio(true);
        if (selectedRestaurant.getLogoBase64() != null && !selectedRestaurant.getLogoBase64().isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(selectedRestaurant.getLogoBase64());
            logoView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
        }
        Button chooseLogoButton = new Button("Choose Logo");
        final String[] logoBase64 = {selectedRestaurant.getLogoBase64()};

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
                    apiService.updateRestaurant(SessionManager.getAuthToken(), selectedRestaurant.getId(), restaurantData);
                    Platform.runLater(() -> {
                        selectedRestaurant.setName(restaurantData.getString("name"));
                        selectedRestaurant.setAddress(restaurantData.getString("address"));
                        selectedRestaurant.setPhone(restaurantData.getString("phone"));
                        selectedRestaurant.setTaxFee(restaurantData.getDouble("tax_fee"));
                        selectedRestaurant.setAdditionalFee(restaurantData.getDouble("additional_fee"));
                        selectedRestaurant.setLogoBase64(restaurantData.optString("logoBase64"));
                        restaurantNameLabel.setText(selectedRestaurant.getName());
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Restaurant updated successfully!");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Update Failed", e.getMessage()));
                    e.printStackTrace();
                }
            }).start();
        });
    }

    @FXML
    void handleAddFoodItem(ActionEvent event) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("Add New Food Item");
        dialog.setHeaderText("Enter the details for the new food item.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(); nameField.setPromptText("Name");
        TextArea descriptionArea = new TextArea(); descriptionArea.setPromptText("Description");
        TextField priceField = new TextField(); priceField.setPromptText("Price");
        TextField supplyField = new TextField(); supplyField.setPromptText("Supply");
        TextField keywordsField = new TextField(); keywordsField.setPromptText("Keywords (comma-separated)");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);
        Button chooseImageButton = new Button("Choose Image");
        final String[] imageBase64 = {""};

        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                try {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    imageBase64[0] = Base64.getEncoder().encodeToString(fileContent);
                    imageView.setImage(new Image(new ByteArrayInputStream(fileContent)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Price:"), 0, 2); grid.add(priceField, 1, 2);
        grid.add(new Label("Supply:"), 0, 3); grid.add(supplyField, 1, 3);
        grid.add(new Label("Keywords:"), 0, 4); grid.add(keywordsField, 1, 4);
        grid.add(new Label("Image:"), 0, 5); grid.add(new HBox(10, imageView, chooseImageButton), 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                JSONObject result = new JSONObject();
                result.put("name", nameField.getText());
                result.put("description", descriptionArea.getText());
                result.put("price", Double.parseDouble(priceField.getText()));
                result.put("supply", Integer.parseInt(supplyField.getText()));
                result.put("keywords", Arrays.asList(keywordsField.getText().split(",")));
                result.put("imageBase64", imageBase64[0]);
                return result;
            }
            return null;
        });

        Optional<JSONObject> result = dialog.showAndWait();

        result.ifPresent(foodData -> {
            new Thread(() -> {
                try {
                    apiService.addFoodItem(SessionManager.getAuthToken(), selectedRestaurant.getId(), foodData);
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Food item added successfully!");
                        loadRestaurantDetails(); // Refresh the lists
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Add Failed", e.getMessage()));
                    e.printStackTrace();
                }
            }).start();
        });
    }
    private void handleEditFoodItem(Food foodToEdit) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("Edit Food Item");
        dialog.setHeaderText("Update details for '" + foodToEdit.getName() + "'");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(foodToEdit.getName());
        TextArea descriptionArea = new TextArea(foodToEdit.getDescription());
        TextField priceField = new TextField(String.valueOf(foodToEdit.getPrice()));
        TextField supplyField = new TextField(String.valueOf(foodToEdit.getSupply()));
        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Keywords (comma-separated)");
        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);

        final String[] imageBase64 = {foodToEdit.getImageBase64()};
        if (imageBase64[0] != null && !imageBase64[0].isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64[0]);
            imageView.setImage(new Image(new ByteArrayInputStream(imageBytes)));
        }

        Button chooseImageButton = new Button("Choose Image");
        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                try {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    imageBase64[0] = Base64.getEncoder().encodeToString(fileContent);
                    imageView.setImage(new Image(new ByteArrayInputStream(fileContent)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Price:"), 0, 2); grid.add(priceField, 1, 2);
        grid.add(new Label("Supply:"), 0, 3); grid.add(supplyField, 1, 3);
        grid.add(new Label("Keywords:"), 0, 4); grid.add(keywordsField, 1, 4);
        grid.add(new Label("Image:"), 0, 5); grid.add(new HBox(10, imageView, chooseImageButton), 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                JSONObject updatedFoodData = new JSONObject();
                updatedFoodData.put("name", nameField.getText());
                updatedFoodData.put("description", descriptionArea.getText());
                updatedFoodData.put("price", Double.parseDouble(priceField.getText()));
                updatedFoodData.put("supply", Integer.parseInt(supplyField.getText()));
                updatedFoodData.put("keywords", Arrays.asList(keywordsField.getText().split("\\s*,\\s*")));
                updatedFoodData.put("imageBase64", imageBase64[0]);
                return updatedFoodData;
            }
            return null;
        });

        Optional<JSONObject> result = dialog.showAndWait();

        result.ifPresent(foodData -> {
            new Thread(() -> {
                try {
                    apiService.updateFoodItem(SessionManager.getAuthToken(), selectedRestaurant.getId(), foodToEdit.getId(), foodData);
                    Platform.runLater(this::loadRestaurantDetails);
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Update Failed", e.getMessage()));
                }
            }).start();
        });
    }
    private void handleDeleteFoodItem(Food foodToDelete) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete Food Item");
        alert.setContentText("Are you sure you want to delete '" + foodToDelete.getName() + "'?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            new Thread(() -> {
                try {
                    apiService.deleteFoodItem(SessionManager.getAuthToken(), selectedRestaurant.getId(), foodToDelete.getId());
                    Platform.runLater(this::loadRestaurantDetails);
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage()));
                }
            }).start();
        }
    }
    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/restaurant-management-view.fxml"));
            Node page = loader.load();

            AnchorPane contentPane = (AnchorPane) restaurantNameLabel.getScene().lookup("#contentPane");

            contentPane.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAddMenu(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Menu");
        dialog.setHeaderText("Enter a name for the new menu category.");
        dialog.setContentText("Menu Name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Menu name cannot be empty.");
                return;
            }

            JSONObject menuData = new JSONObject();
            menuData.put("title", name);

            new Thread(() -> {
                try {
                    apiService.addMenu(SessionManager.getAuthToken(), selectedRestaurant.getId(), menuData);
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Menu added successfully!");
                        loadRestaurantDetails();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Add Failed", e.getMessage()));
                    e.printStackTrace();
                }
            }).start();
        });
    }
    private void setupMenuActionsColumn() {
        menuActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addFoodButton = new Button("Add Food");
            private final Button deleteMenuButton = new Button("Delete");
            private final HBox pane = new HBox(5, addFoodButton, deleteMenuButton);

            {
                pane.setAlignment(Pos.CENTER);
                addFoodButton.setOnAction(event -> {
                    Menu menu = getTableView().getItems().get(getIndex());
                    showAddFoodToMenuDialog(menu);
                });
                deleteMenuButton.setOnAction(event -> {
                    Menu menuToDelete = getTableView().getItems().get(getIndex());

                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the '" + menuToDelete.getName() + "' menu?", ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> result = confirmationAlert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                apiService.deleteMenu(SessionManager.getAuthToken(), selectedRestaurant.getId(), menuToDelete.getId());
                                Platform.runLater(() -> {
                                    showAlert(Alert.AlertType.INFORMATION, "Success", "Menu deleted successfully!");
                                    loadRestaurantDetails();
                                });
                            } catch (Exception e) {
                                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage()));
                                e.printStackTrace();
                            }
                        }).start();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateMenuItems(Menu menu, List<Food> oldList, List<Food> newList) {
        new Thread(() -> {
            try {

                for (Food newFood : newList) {
                    if (oldList.stream().noneMatch(oldFood -> oldFood.getId() == newFood.getId())) {
                        apiService.addFoodToMenu(SessionManager.getAuthToken(), selectedRestaurant.getId(), menu.getId(), newFood.getId());
                    }
                }
                for (Food oldFood : oldList) {
                    if (newList.stream().noneMatch(newFood -> newFood.getId() == oldFood.getId())) {
                        apiService.removeFoodFromMenu(SessionManager.getAuthToken(), selectedRestaurant.getId(), menu.getId(), oldFood.getId());
                    }
                }
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Menu items updated successfully!");
                    loadRestaurantDetails();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Update Failed", e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    void handleManageCoupons(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/croissantbuddies/snappfoodclient/fxml/seller-coupon-management-view.fxml"));
            Parent page = loader.load();

            SellerCouponManagementController controller = loader.getController();
            controller.initData(selectedRestaurant);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage Coupons for " + selectedRestaurant.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}