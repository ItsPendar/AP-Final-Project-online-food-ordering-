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
                        "menu_title TEXT[], " +
                        "FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)" +
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
        Array keyWordArray = connection.createArrayOf("text", foodItem.getKeyword().toArray(new String[0]));
        stmt.setArray(5, keyWordArray);
        stmt.setString(6, foodItem.getImageBase64());
        stmt.setInt(7, foodItem.getRestaurantID());
        Array menuArray = connection.createArrayOf("text", foodItem.getMenuTitle().toArray(new String[0]));
        stmt.setArray(8, menuArray);
        stmt.executeUpdate();
    }
    public static void addItemToMenu(int itemID, String menuTitle) throws SQLException {
        String sql = "UPDATE foods SET menu_title = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        List<String> menuTitles = getMenusOfAnItem(itemID);
        menuTitles.add(menuTitle);
        Array menuArray = connection.createArrayOf("text", menuTitles.toArray(new String[0]));
        preparedStatement.setArray(1, menuArray);
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
        String sql = "UPDATE foods SET name = ?,imageBase64 = ?, description = ?, keywords = ?, price = ?, supply = ?, menu_title = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setString(1, foodItem.getName());
        preparedStatement.setString(2, foodItem.getImageBase64());
        preparedStatement.setString(3, foodItem.getDescription());
        Array keyWordArray = connection.createArrayOf("text", foodItem.getKeyword().toArray(new String[0]));
        preparedStatement.setArray(4, keyWordArray);
        preparedStatement.setDouble(5, foodItem.getPrice());
        preparedStatement.setInt(6, foodItem.getSupply());
        Array menuArray = connection.createArrayOf("text", foodItem.getMenuTitle().toArray(new String[0]));
        preparedStatement.setArray(7, menuArray);
        preparedStatement.setInt(8, itemID);
        preparedStatement.executeUpdate();
    }
    public static void deleteItemFromMenu(int itemID, String menuTitle) throws SQLException {
        String sql = "UPDATE foods SET menu_title = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        List<String> menuTitles = getMenusOfAnItem(itemID);
        menuTitles.remove(menuTitle);
        preparedStatement.setString(1, String.join(",", menuTitles));
        preparedStatement.setInt(2,itemID);
        preparedStatement.executeUpdate();
    }
    public static List<String> getMenusOfAnItem(int itemID) {
        String listOfMenus = null;
        try {
            String query = "SELECT menu_title FROM foods WHERE food_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, itemID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                listOfMenus =  rs.getString("menu_title");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        assert listOfMenus != null;
        String[] parts = listOfMenus.split(",");
        List<String> menus = new ArrayList<>(Arrays.asList(parts));
        return menus;
    }
    public static ArrayNode getItemsInAMenu(String menuTitle) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode foodItems = mapper.createArrayNode();

        String query = "SELECT * FROM foods WHERE ? = ANY(menu_title)";

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

                    foodItems.add(item);
                }
            }
        }
        return foodItems;
    }
}
