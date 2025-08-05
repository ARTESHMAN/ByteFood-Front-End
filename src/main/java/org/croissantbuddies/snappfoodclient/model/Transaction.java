package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class Transaction {
    private final SimpleLongProperty id;
    private final SimpleLongProperty orderId;
    private final SimpleLongProperty userId;
    private final SimpleStringProperty method;
    private final SimpleStringProperty status;

    private final SimpleStringProperty buyerName;
    private final SimpleStringProperty sellerName;

    public Transaction(long id, long orderId, long userId, String method, String status, String buyerName, String sellerName) {
        this.id = new SimpleLongProperty(id);
        this.orderId = new SimpleLongProperty(orderId);
        this.userId = new SimpleLongProperty(userId);
        this.method = new SimpleStringProperty(method);
        this.status = new SimpleStringProperty(status);
        this.buyerName = new SimpleStringProperty(buyerName);
        this.sellerName = new SimpleStringProperty(sellerName);
    }

    public String getBuyerName() { return buyerName.get(); }
    public SimpleStringProperty buyerNameProperty() { return buyerName; }
    public String getSellerName() { return sellerName.get(); }
    public SimpleStringProperty sellerNameProperty() { return sellerName; }

    public long getId() { return id.get(); }
    public SimpleLongProperty idProperty() { return id; }
    public long getOrderId() { return orderId.get(); }
    public SimpleLongProperty orderIdProperty() { return orderId; }
    public long getUserId() { return userId.get(); }
    public SimpleLongProperty userIdProperty() { return userId; }
    public String getMethod() { return method.get(); }
    public SimpleStringProperty methodProperty() { return method; }
    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
}