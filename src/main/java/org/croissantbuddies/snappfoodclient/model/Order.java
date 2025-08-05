package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class Order {
    private final SimpleLongProperty id;
    private final SimpleStringProperty deliveryAddress;
    private final SimpleLongProperty customerId;
    private final SimpleStringProperty buyerName;
    private final SimpleLongProperty vendorId;
    private final SimpleStringProperty vendorName;
    private final SimpleDoubleProperty payPrice;
    private final SimpleStringProperty status;
    private final SimpleStringProperty createdAt;
    private final SimpleBooleanProperty rated;

    public Order(long id, String deliveryAddress, long customerId, String buyerName, long vendorId, String vendorName, double payPrice, String status, String createdAt) {
        this.id = new SimpleLongProperty(id);
        this.deliveryAddress = new SimpleStringProperty(deliveryAddress);
        this.customerId = new SimpleLongProperty(customerId);
        this.buyerName = new SimpleStringProperty(buyerName);
        this.vendorId = new SimpleLongProperty(vendorId);
        this.vendorName = new SimpleStringProperty(vendorName);
        this.payPrice = new SimpleDoubleProperty(payPrice);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.rated = new SimpleBooleanProperty(false);
    }
    public long getId() { return id.get(); }
    public SimpleLongProperty idProperty() { return id; }
    public String getDeliveryAddress() { return deliveryAddress.get(); }
    public SimpleStringProperty deliveryAddressProperty() { return deliveryAddress; }
    public long getCustomerId() { return customerId.get(); }
    public SimpleLongProperty customerIdProperty() { return customerId; }
    public String getBuyerName() { return buyerName.get(); }
    public SimpleStringProperty buyerNameProperty() { return buyerName; }
    public long getVendorId() { return vendorId.get(); }
    public SimpleLongProperty vendorIdProperty() { return vendorId; }
    public String getVendorName() { return vendorName.get(); }
    public SimpleStringProperty vendorNameProperty() { return vendorName; }
    public double getPayPrice() { return payPrice.get(); }
    public SimpleDoubleProperty payPriceProperty() { return payPrice; }
    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
    public String getCreatedAt() { return createdAt.get(); }
    public SimpleStringProperty createdAtProperty() { return createdAt; }
    public boolean isRated() { return rated.get(); }
    public SimpleBooleanProperty ratedProperty() { return rated; }
    public void setRated(boolean rated) { this.rated.set(rated); }
}