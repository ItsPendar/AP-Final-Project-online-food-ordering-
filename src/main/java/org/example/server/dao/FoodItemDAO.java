package org.example.server.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.server.modules.FoodItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FoodItemDAO {
    private static final Connection connection;

    static {
        try {
            connection = DatabaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public FoodItemDAO() throws SQLException {
        this.createFoodTable();
    }
    public void createFoodTable() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS foods (" +
                        "food_id SERIAL PRIMARY KEY, " +
                        "restaurant_id INTEGER NOT NULL, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "description TEXT, " +
                        "price REAL, " +
                        "supply BIGINT, " +
                        "keywords TEXT[], " +
                        "image_base64 TEXT, " +
                        "menu_title VARCHAR(255), " +  // Nullable initially
                        "FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id), " +
                        //"FOREIGN KEY (menu_title) REFERENCES menus(title)" +
                        //I commented the line above because the user should be able to add food item to
                        //a restaurant even when there is no menu added to the restaurant
                        ")"
        );
        preparedStatement.executeUpdate();
    }
    public static void addFoodItem(FoodItem foodItem) throws SQLException {
        String query = "INSERT INTO foods (name, description, price, supply, keywords, image_base64, restaurant_id, menu_title) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, foodItem.getName());
        stmt.setString(2, foodItem.getDescription());
        stmt.setDouble(3, foodItem.getPrice());
        stmt.setInt(4, foodItem.getSupply());
        stmt.setString(5, String.join(",", foodItem.getKeyword())); //the words in the list are first joined by ,'s and then
        //passed to DB (["spicy", "vegan"] → "spicy,vegan")
        stmt.setString(6, foodItem.getImageBase64());
        stmt.setInt(7, foodItem.getRestaurantID());
        stmt.setString(8, foodItem.getMenuTitle());
        stmt.executeUpdate();
    }
    public static void addItemToMenu(int itemID, String menuTitle) throws SQLException {
        String sql = "UPDATE foods SET menu_title = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setString(1, menuTitle);
        preparedStatement.setInt(2,itemID);
        preparedStatement.executeUpdate();
    }
    public static void deleteFoodItemFromRestaurant(int foodItemID, int restaurantID) throws SQLException {
        String sql = "DELETE * from foods WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setInt(1, foodItemID);
        preparedStatement.executeUpdate();
    }
    public static void updateFoodItem(int itemID, FoodItem foodItem) throws SQLException {
        String sql = "UPDATE foods SET name = ?,imageBase64 = ?, description = ?, keywords = ?, price = ?, supply = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setString(1, foodItem.getName());
        preparedStatement.setString(2, foodItem.getImageBase64());
        preparedStatement.setString(3, foodItem.getDescription());
        preparedStatement.setString(4, String.join(",", foodItem.getKeyword()));
        preparedStatement.setDouble(5, foodItem.getPrice());
        preparedStatement.setInt(6, foodItem.getSupply());
        preparedStatement.setInt(7, itemID);
        preparedStatement.executeUpdate();
    }
    public static void deleteItemFromMenu(int itemID) throws SQLException {
        String sql = "UPDATE foods SET menu_title = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setString(1, null);
        preparedStatement.setInt(2,itemID);
        preparedStatement.executeUpdate();
    }
    //TODO : we need a method that gets the title of the menu and return a JsonArray containing every food object in that menu
    public static ArrayNode getItemsInAMenu(String menuTitle) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode foodItems = mapper.createArrayNode();

        String query = "SELECT * FROM foods WHERE menu_title = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, menuTitle);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ObjectNode item = mapper.createObjectNode();

                    item.put("name", rs.getString("name"));
                    item.put("description", rs.getString("description"));
                    item.put("price", rs.getInt("price"));
                    item.put("supply", rs.getInt("supply"));

                    Array sqlArray = rs.getArray("keywords");
                    if (sqlArray != null) {
                        String[] keywordsArray = (String[]) sqlArray.getArray();
                        ArrayNode keywordsNode = mapper.createArrayNode();
                        for (String kw : keywordsArray) {
                            keywordsNode.add(kw);
                        }
                        item.set("keywords", keywordsNode);
                    } else {
                        item.set("keywords", mapper.createArrayNode());
                    }

                    item.put("vendor_id", rs.getInt("vendor_id"));
                    item.put("id", rs.getInt("id"));
                    item.put("imageBase64", rs.getString("imageBase64"));

                    foodItems.add(item); // ✅ compatible types
                }
            }
        }

        return foodItems;
    }
}
