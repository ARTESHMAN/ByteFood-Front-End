
package org.croissantbuddies.snappfoodclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import org.croissantbuddies.snappfoodclient.model.Food;
import org.croissantbuddies.snappfoodclient.model.FoodSelectionItem;

import java.util.List;
import java.util.stream.Collectors;

public class ManageMenuItemsDialogController {

    @FXML
    private ListView<FoodSelectionItem> foodListView;

    private Stage dialogStage;
    private boolean isSaved = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setData(List<Food> allFoods, List<Food> menuItems) {

        foodListView.setCellFactory(CheckBoxListCell.forListView(FoodSelectionItem::selectedProperty));

        List<FoodSelectionItem> foodSelectionList = allFoods.stream()
                .map(food -> new FoodSelectionItem(
                        food,
                        menuItems.stream().anyMatch(mf -> mf.getId() == food.getId()))
                ).collect(Collectors.toList());

        foodListView.getItems().setAll(foodSelectionList);
    }

    public List<Food> getSelectedFoods() {
        if (!isSaved) {
            return null;
        }
        return foodListView.getItems().stream()
                .filter(FoodSelectionItem::isSelected)
                .map(FoodSelectionItem::getFood)
                .collect(Collectors.toList());
    }

    @FXML
    private void handleSave() {
        isSaved = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        isSaved = false;
        dialogStage.close();
    }
}