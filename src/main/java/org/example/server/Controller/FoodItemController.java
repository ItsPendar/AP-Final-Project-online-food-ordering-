package org.example.server.Controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.example.server.dao.FoodItemDAO;
import org.example.server.modules.FoodItem;
import org.json.JSONArray;

import java.sql.SQLException;

public class FoodItemController {
    private static FoodItemDAO foodItemDAO;

    public FoodItemController() throws SQLException {
        foodItemDAO = new FoodItemDAO();
    }

    public int addFoodItem(FoodItem foodItem) throws SQLException {
        return foodItemDAO.addFoodItem(foodItem);
    }
    public void addItemToMenu(int itemID, String menuTitle) throws SQLException {
        foodItemDAO.addItemToMenu(itemID, menuTitle);
    }
    public void deleteFoodItemFromRestaurant(int foodItemID, int restaurantID) throws SQLException {
        foodItemDAO.deleteFoodItemFromRestaurant(foodItemID,restaurantID);
    }
    public void updateFoodItem(int itemID, FoodItem newFoodItem) throws SQLException {
        foodItemDAO.updateFoodItem(itemID,newFoodItem);
    }
    public void deleteItemFromMenu(int foodItemID, String menuTitle) throws SQLException {
        foodItemDAO.deleteItemFromMenu(foodItemID, menuTitle);
    }
    public  ArrayNode getItemsInAMenu(String menuTitle, int restaurantID) throws SQLException {
        return foodItemDAO.getItemsInAMenu(menuTitle,restaurantID);
    }
}
