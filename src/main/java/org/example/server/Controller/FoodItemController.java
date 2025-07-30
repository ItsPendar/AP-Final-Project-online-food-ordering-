package org.example.server.Controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.example.server.dao.FoodItemDAO;
import org.example.server.modules.FoodItem;
import org.json.JSONArray;

import java.sql.SQLException;
import java.util.List;

public class FoodItemController {
    private static FoodItemDAO foodItemDAO;

    public FoodItemController() throws SQLException {
        foodItemDAO = new FoodItemDAO();
    }

    public int addFoodItem(FoodItem foodItem) throws SQLException {
        return foodItemDAO.addFoodItem(foodItem);
    }
    public boolean addItemToMenu(int itemID, String menuTitle) throws SQLException {
        return foodItemDAO.addItemToMenu(itemID, menuTitle);
    }
    public boolean deleteFoodItemFromRestaurant(int foodItemID, int restaurantID) throws SQLException {
        return foodItemDAO.deleteFoodItemFromRestaurant(foodItemID,restaurantID);
    }
    public boolean updateFoodItem(int itemID, FoodItem newFoodItem) throws SQLException {
        return foodItemDAO.updateFoodItem(itemID,newFoodItem);
    }
    public boolean deleteItemFromMenu(int foodItemID, String menuTitle) throws SQLException {
        return foodItemDAO.deleteItemFromMenu(foodItemID, menuTitle);
    }
    public  ArrayNode getItemsInAMenu(String menuTitle, int restaurantID) throws SQLException {
        return foodItemDAO.getItemsInAMenu(menuTitle,restaurantID);
    }
    public ArrayNode getMenusOfAnItem(int itemID) throws SQLException {
        return foodItemDAO.getMenusOfAnItem(itemID);
    }
    public List<Integer> getItemIDsInARestaurant(int restaurantID) throws SQLException {
        return foodItemDAO.getItemIDsInARestaurant(restaurantID);
    }

    public FoodItem getFoodItemByID(int itemID) throws SQLException {
        return foodItemDAO.getFoodItemByID(itemID);
    }
}
