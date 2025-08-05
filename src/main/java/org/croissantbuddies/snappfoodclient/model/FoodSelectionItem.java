
package org.croissantbuddies.snappfoodclient.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.croissantbuddies.snappfoodclient.model.Food;

public class FoodSelectionItem {
    private final Food food;
    private final BooleanProperty selected = new SimpleBooleanProperty();

    public FoodSelectionItem(Food food, boolean isSelected) {
        this.food = food;
        this.selected.set(isSelected);
    }

    public Food getFood() {
        return food;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public String toString() {
        return food.getName();
    }
}