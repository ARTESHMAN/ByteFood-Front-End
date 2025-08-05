package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleLongProperty id;
    private final SimpleStringProperty fullName;
    private final SimpleStringProperty phone;
    private final SimpleStringProperty email;
    private final SimpleStringProperty role;
    private final SimpleStringProperty status;


    public User(long id, String fullName, String phone, String email, String role, String status) {
        this.id = new SimpleLongProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.phone = new SimpleStringProperty(phone);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.status = new SimpleStringProperty(status);
    }

    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public long getId() { return id.get(); }
    public SimpleLongProperty idProperty() { return id; }
    public String getFullName() { return fullName.get(); }
    public SimpleStringProperty fullNameProperty() { return fullName; }
    public String getPhone() { return phone.get(); }
    public SimpleStringProperty phoneProperty() { return phone; }
    public String getEmail() { return email.get(); }
    public SimpleStringProperty emailProperty() { return email; }
    public String getRole() { return role.get(); }
    public SimpleStringProperty roleProperty() { return role; }
}