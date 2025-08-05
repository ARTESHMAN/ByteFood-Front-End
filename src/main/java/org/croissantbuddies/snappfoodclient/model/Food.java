package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class Food {
    private final SimpleLongProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty description;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty supply;
    private final SimpleStringProperty keyWords;
    private final SimpleStringProperty imageBase64;
    private final SimpleStringProperty restaurantName;

    private final SimpleLongProperty restaurantId;
    private transient Image image;

    public Image getImage() {
        if (image == null && getImageBase64() != null && !getImageBase64().isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(getImageBase64());
                this.image = new Image(new ByteArrayInputStream(imageBytes));
            } catch (Exception e) {
                // Could not decode image, return null
                e.printStackTrace();
            }
        }
        return image;
    }
    public Food(long id, String name, String description, double price, int supply, String keyWords, String imageBase64, String restaurantName, long restaurantId) {
        this.id = new SimpleLongProperty(id);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
        this.supply = new SimpleIntegerProperty(supply);
        this.keyWords = new SimpleStringProperty(keyWords);
        this.imageBase64 = new SimpleStringProperty(imageBase64);
        this.restaurantName = new SimpleStringProperty(restaurantName);
        this.restaurantId = new SimpleLongProperty(restaurantId);
    }
    public long getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getDescription() { return description.get(); }
    public double getPrice() { return price.get(); }
    public int getSupply() { return supply.get(); }
    public String getImageBase64() { return imageBase64.get(); }
    public String getKeyWords(){return keyWords.get();}
    public String getRestaurantName() { return restaurantName.get(); }

    public long getRestaurantId() { return restaurantId.get(); }

    public SimpleStringProperty keyWordsProperty() {return keyWords;}
    public SimpleLongProperty idProperty() { return id; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleDoubleProperty priceProperty() { return price; }
    public SimpleIntegerProperty supplyProperty() { return supply; }
    public SimpleStringProperty descriptionProperty() { return description; }
    public SimpleStringProperty imageBase64Property() { return imageBase64; }

    @Override
    public String toString() {
        return getName();
    }
}