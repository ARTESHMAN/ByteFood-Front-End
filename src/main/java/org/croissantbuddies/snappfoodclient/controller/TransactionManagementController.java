package org.croissantbuddies.snappfoodclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.croissantbuddies.snappfoodclient.manager.SessionManager;
import org.croissantbuddies.snappfoodclient.model.Transaction;
import org.croissantbuddies.snappfoodclient.service.ApiService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class TransactionManagementController {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Long> idColumn;
    @FXML private TableColumn<Transaction, Long> orderIdColumn;
    @FXML private TableColumn<Transaction, String> buyerNameColumn;
    @FXML private TableColumn<Transaction, String> sellerNameColumn;
    @FXML private TableColumn<Transaction, String> methodColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;

    private final ApiService apiService = new ApiService();
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        buyerNameColumn.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        sellerNameColumn.setCellValueFactory(new PropertyValueFactory<>("sellerName"));

        loadTransactions();
    }

    private void loadTransactions() {
        new Thread(() -> {
            try {
                String authToken = SessionManager.getAuthToken();
                if (authToken == null) return;

                String response = apiService.getTransactions(authToken);
                JSONArray transactionsJson = new JSONArray(response);

                transactionList.clear();
                for (int i = 0; i < transactionsJson.length(); i++) {
                    JSONObject transObj = transactionsJson.getJSONObject(i);
                    transactionList.add(new Transaction(
                            transObj.getLong("id"),
                            transObj.getLong("order_id"),
                            transObj.getLong("user_id"),
                            transObj.getString("method"),
                            transObj.getString("status"),
                            transObj.optString("buyerName", "N/A"),
                            transObj.optString("sellerName", "N/A")
                    ));
                }

                Platform.runLater(() -> transactionsTable.setItems(transactionList));

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}