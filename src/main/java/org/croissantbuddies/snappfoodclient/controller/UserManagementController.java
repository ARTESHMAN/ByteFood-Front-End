package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.User;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class UserManagementController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> idColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    private final ApiService apiService = new ApiService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveButton = new Button("Approve");
            private final Button rejectButton = new Button("Reject");
            private final HBox pane = new HBox(5, approveButton, rejectButton);

            {
                pane.setAlignment(Pos.CENTER);
                approveButton.setOnAction(event -> handleUpdateStatus(getTableView().getItems().get(getIndex()), "VALID"));
                rejectButton.setOnAction(event -> handleUpdateStatus(getTableView().getItems().get(getIndex()), "INVALID"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if ("SELLER".equalsIgnoreCase(user.getRole()) || "COURIER".equalsIgnoreCase(user.getRole())) {
                        approveButton.setDisable("VALID".equalsIgnoreCase(user.getStatus()));
                        rejectButton.setDisable("INVALID".equalsIgnoreCase(user.getStatus()));
                        setGraphic(pane);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                String authToken = SessionManager.getAuthToken();
                if (authToken == null) return;

                String response = apiService.getUsers(authToken);
                JSONArray usersJson = new JSONArray(response);

                userList.clear();
                for (int i = 0; i < usersJson.length(); i++) {
                    JSONObject userObj = usersJson.getJSONObject(i);
                    userList.add(new User(
                            userObj.getLong("id"),
                            userObj.getString("full_name"),
                            userObj.getString("phone"),
                            userObj.optString("email", "N/A"),
                            userObj.getString("role"),
                            userObj.optString("status", "N/A")
                    ));
                }
                Platform.runLater(() -> usersTable.setItems(userList));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleUpdateStatus(User user, String newStatus) {
        new Thread(() -> {
            try {
                String authToken = SessionManager.getAuthToken();
                apiService.updateUserStatus(authToken, user.getId(), newStatus);
                Platform.runLater(this::loadUsers);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}