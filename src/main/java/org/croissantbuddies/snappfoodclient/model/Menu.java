
package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class Menu {
    private final SimpleLongProperty id;
    private final SimpleStringProperty name;

    public Menu(long id, String name) {
        this.id = new SimpleLongProperty(id);
        this.name = new SimpleStringProperty(name);
    }

    public long getId() {
        return id.get();
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }


    @Override
    public String toString() {
        return name.get();
    }
}