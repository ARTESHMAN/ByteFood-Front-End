package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class Restaurant {
    private final SimpleLongProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty address;
    private final SimpleStringProperty phone;
    private final SimpleDoubleProperty taxFee;
    private final SimpleDoubleProperty additionalFee;
    private final SimpleStringProperty logoBase64;
    private final SimpleStringProperty averageRating;
    private transient Image logo;
    private final SimpleBooleanProperty favorite = new SimpleBooleanProperty(false);

    public Restaurant(long id, String name, String address, String phone, double taxFee, double additionalFee, String logoBase64, String averageRating) {
        this.id = new SimpleLongProperty(id);
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.phone = new SimpleStringProperty(phone);
        this.taxFee = new SimpleDoubleProperty(taxFee);
        this.additionalFee = new SimpleDoubleProperty(additionalFee);
        this.logoBase64 = new SimpleStringProperty(logoBase64);
        this.averageRating = new SimpleStringProperty(averageRating);

    }

    public void setName(String name) { this.name.set(name); }
    public void setAddress(String address) { this.address.set(address); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setTaxFee(double taxFee) { this.taxFee.set(taxFee); }
    public void setAdditionalFee(double additionalFee) { this.additionalFee.set(additionalFee); }
    public void setLogoBase64(String logoBase64) {
        this.logoBase64.set(logoBase64);
        this.logo = null;
    }

    public long getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getAddress() { return address.get(); }
    public String getPhone() { return phone.get(); }
    public double getTaxFee() { return taxFee.get(); }
    public double getAdditionalFee() { return additionalFee.get(); }
    public String getLogoBase64() { return logoBase64.get(); }
    public String getAverageRating() { return averageRating.get(); }
    public boolean isFavorite() { return favorite.get(); }
    public SimpleBooleanProperty favoriteProperty() { return favorite; }
    public void setFavorite(boolean isFavorite) { this.favorite.set(isFavorite); }

    public Image getLogo() {
        if (logo == null && getLogoBase64() != null && !getLogoBase64().isEmpty()) {
            byte[] imageBytes = Base64.getDecoder().decode(getLogoBase64());
            this.logo = new Image(new ByteArrayInputStream(imageBytes));
        }
        return logo;
    }


    public SimpleLongProperty idProperty() { return id; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty addressProperty() { return address; }
    public SimpleStringProperty phoneProperty() { return phone; }
    public SimpleDoubleProperty taxFeeProperty() { return taxFee; }
    public SimpleDoubleProperty additionalFeeProperty() { return additionalFee; }
    public SimpleStringProperty averageRatingProperty() { return averageRating; }
}