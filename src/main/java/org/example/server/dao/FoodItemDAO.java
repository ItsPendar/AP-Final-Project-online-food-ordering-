package org.example.server.dao;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.stream.Collectors;

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
                        "supply INTEGER, " +
                        "keywords TEXT[], " +
                        "image_base64 TEXT, " +
                        "menu_title TEXT[], " +
                        "FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)" +
                        ")"
        );
        preparedStatement.executeUpdate();
    }
    public int addFoodItem(FoodItem foodItem) throws SQLException {
        String query = "INSERT INTO foods " +
                "(name, description, price, supply, keywords, image_base64, restaurant_id, menu_title) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, foodItem.getName());
        stmt.setString(2, foodItem.getDescription());
        stmt.setDouble(3, foodItem.getPrice());
        stmt.setInt(4, foodItem.getSupply());
        Array keyWordArray = connection.createArrayOf("text", foodItem.getKeyword().toArray(new String[0]));
        stmt.setArray(5, keyWordArray);
        stmt.setString(6, foodItem.getImageBase64());
        stmt.setInt(7, foodItem.getRestaurantID());
        Array menuArray = connection.createArrayOf("text", new String[] { "all" });
        stmt.setArray(8, menuArray);
        stmt.executeUpdate();
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1); // return the newly generated food_id
        } else {
            throw new SQLException("Creating food item failed, no ID obtained.");
        }
    }
    public boolean addItemToMenu(int itemID, String menuTitle) throws SQLException {
        String sql = "UPDATE foods SET menu_title = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        List<String> menuTitles = getMenusOfAnItemAsList(itemID);
        menuTitles.add(menuTitle);
        Array menuArray = connection.createArrayOf("text", menuTitles.toArray(new String[0]));
        preparedStatement.setArray(1, menuArray);
        preparedStatement.setInt(2,itemID);
        preparedStatement.executeUpdate();
        return true;
    }
    public boolean deleteFoodItemFromRestaurant(int foodItemID, int restaurantID) throws SQLException {
        String sql = "DELETE from foods WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setInt(1, foodItemID);
        preparedStatement.executeUpdate();
        return true;
    }
    public boolean updateFoodItem(int itemID, FoodItem foodItem) throws SQLException {
        String sql = "UPDATE foods SET name = ?,image_base64 = ?, description = ?, keywords = ?, price = ?, supply = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setString(1, foodItem.getName());
        preparedStatement.setString(2, foodItem.getImageBase64());
        preparedStatement.setString(3, foodItem.getDescription());
        Array keyWordArray = connection.createArrayOf("text", foodItem.getKeyword().toArray(new String[0]));
        preparedStatement.setArray(4, keyWordArray);
        preparedStatement.setDouble(5, foodItem.getPrice());
        preparedStatement.setInt(6, foodItem.getSupply());
        preparedStatement.setInt(7, itemID);
        System.out.println("item id in DAO : " + itemID);
        preparedStatement.executeUpdate();
        return true;
    }
    public boolean deleteItemFromMenu(int itemID, String menuTitle) throws SQLException {
        String sql = "UPDATE foods SET menu_title = ? WHERE food_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        List<String> menuTitles = getMenusOfAnItemAsList(itemID);
        menuTitles.remove(menuTitle);
        Array menuArray = connection.createArrayOf("text", menuTitles.toArray(new String[0]));
        preparedStatement.setArray(1, menuArray);
        preparedStatement.setInt(2,itemID);
        preparedStatement.executeUpdate();
        return true;
    }
    public ArrayNode getMenusOfAnItem(int itemID) throws SQLException {
       List<String> listOfMenus = getMenusOfAnItemAsList(itemID);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        for (String item : listOfMenus) {
            arrayNode.add(item);
        }
        return arrayNode;
    }
    public List<String> getMenusOfAnItemAsList(int itemID) throws SQLException {
        List<String> listOfMenus = new ArrayList<>();
        String query = "SELECT menu_title FROM foods WHERE food_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, itemID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String raw = rs.getString("menu_title");
            if (raw != null) {
                String[] array = raw.replace("{", "").replace("}", "").split(",");
                listOfMenus = Arrays.stream(array)
                        .map(String::trim)
                        .collect(Collectors.toList());
            }
        }
        return listOfMenus;
    }//keep one of these methods with the same name
    public ArrayNode getItemsInAMenu(String menuTitle, int restaurantID) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode foodItems = mapper.createArrayNode();
        String query = "SELECT * FROM foods WHERE ? = ANY(menu_title) AND restaurant_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, menuTitle);
            stmt.setInt(2,restaurantID);
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
                    item.put("vendor_id", rs.getInt("restaurant_id"));
                    item.put("id", rs.getInt("food_id"));
                    item.put("imageBase64", rs.getString("image_Base64"));
                    foodItems.add(item);
                }
            }
        }
        return foodItems;
    }
    public List<Integer> getItemIDsInARestaurant(int restaurantID) throws SQLException {
        List<Integer> itemIDsList = new ArrayList<>();
        String query = "SELECT food_id FROM foods WHERE restaurant_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, restaurantID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itemIDsList.add(rs.getInt("food_id"));
                }
            }
        }
        return itemIDsList;
    }
    public FoodItem getFoodItemByID(int itemID) throws SQLException {
        String query = "SELECT * FROM foods WHERE food_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, itemID);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            FoodItem item = new FoodItem();
            item.setFoodItemID(itemID);
            item.setName(rs.getString("name"));
            item.setDescription(rs.getString("description"));
            item.setPrice(rs.getDouble("price"));
            item.setSupply(rs.getInt("supply"));
            Array keywordArray = rs.getArray("keywords");
            if (keywordArray != null) {
                item.setKeyword(List.of((String[]) keywordArray.getArray()));
            }
            item.setImageBase64(rs.getString("image_base64"));
            item.setRestaurantID(rs.getInt("restaurant_id"));
            return item;
        }
        return null;
    }
}
