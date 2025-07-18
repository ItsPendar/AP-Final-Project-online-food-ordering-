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

    public static void addFoodItem(FoodItem foodItem) throws SQLException {
        FoodItemDAO.addFoodItem(foodItem);
    }
    public static void addItemToMenu(int itemID, String menuTitle) throws SQLException {
        FoodItemDAO.addItemToMenu(itemID, menuTitle);
    }
    public static void deleteFoodItemFromRestaurant(int foodItemID, int restaurantID) throws SQLException {
        FoodItemDAO.deleteFoodItemFromRestaurant(foodItemID,restaurantID);
    }
    public static void updateFoodItem(int itemID, FoodItem newFoodItem) throws SQLException {
        FoodItemDAO.updateFoodItem(itemID,newFoodItem);
    }
    public static void deleteItemFromMenu(int foodItemID, String menuTitle) throws SQLException {
        FoodItemDAO.deleteItemFromMenu(foodItemID, menuTitle);
    }
    public static ArrayNode getItemsInAMenu(String menuTitle) throws SQLException {
        return FoodItemDAO.getItemsInAMenu(menuTitle);
    }
}
