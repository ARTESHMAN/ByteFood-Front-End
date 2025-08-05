package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class Coupon {
    private final SimpleLongProperty id;
    private final SimpleStringProperty couponCode;
    private final SimpleStringProperty type;
    private final SimpleDoubleProperty value;
    private final SimpleDoubleProperty minPrice;
    private final SimpleIntegerProperty userCount;
    private final SimpleStringProperty startDate;
    private final SimpleStringProperty endDate;

    public Coupon(long id, String couponCode, String type, double value, double minPrice, int userCount, String startDate, String endDate) {
        this.id = new SimpleLongProperty(id);
        this.couponCode = new SimpleStringProperty(couponCode);
        this.type = new SimpleStringProperty(type);
        this.value = new SimpleDoubleProperty(value);
        this.minPrice = new SimpleDoubleProperty(minPrice);
        this.userCount = new SimpleIntegerProperty(userCount);
        this.startDate = new SimpleStringProperty(startDate);
        this.endDate = new SimpleStringProperty(endDate);
    }

    public long getId() { return id.get(); }
    public SimpleLongProperty idProperty() { return id; }
    public String getCouponCode() { return couponCode.get(); }
    public SimpleStringProperty couponCodeProperty() { return couponCode; }
    public String getType() { return type.get(); }
    public SimpleStringProperty typeProperty() { return type; }
    public double getValue() { return value.get(); }
    public SimpleDoubleProperty valueProperty() { return value; }
    public double getMinPrice() { return minPrice.get(); }
    public SimpleDoubleProperty minPriceProperty() { return minPrice; }
    public int getUserCount() { return userCount.get(); }
    public SimpleIntegerProperty userCountProperty() { return userCount; }
    public String getStartDate() { return startDate.get(); }
    public SimpleStringProperty startDateProperty() { return startDate; }
    public String getEndDate() { return endDate.get(); }
    public SimpleStringProperty endDateProperty() { return endDate; }
}