package org.croissantbuddies.snappfoodclient.model;

public class CartItemModel {
    private long foodId;
    private String foodName;
    private int quantity;
    private double price;

    public CartItemModel(long foodId, String foodName, int quantity, double price) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.price = price;
    }

    public long getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotalPrice() { return quantity * price; }
}