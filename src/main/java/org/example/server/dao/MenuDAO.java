package org.example.server.dao;

import org.example.server.modules.FoodItem;
import org.example.server.modules.Menu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
    private static final Connection connection;
    static {
        try {
            connection = DatabaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MenuDAO() throws SQLException {
        this.createMenuTable();
    }
    public void createMenuTable() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS menus (" +
                        "menu_id SERIAL PRIMARY KEY, " +
                        "title VARCHAR(255) , " +
                        "restaurant_id INTEGER NOT NULL, " +
                        //"title VARCHAR(255) UNIQUE NOT NULL, " +
                        "FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)" +
                        ")"
        );
        preparedStatement.executeUpdate();
    }
    public boolean addMenu(Menu menu) throws SQLException {
        String query = "INSERT INTO menus (title, restaurant_id) VALUES (?, ?)";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1,menu.getMenuTitle());
        stmt.setInt(2, menu.getRestaurantID());
        stmt.executeUpdate();
        return true;
    }
    public boolean doesMenuExist(int restaurantID, String title) {
        String query = "SELECT 1 FROM menus WHERE restaurant_id = ? AND title = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, restaurantID);
            stmt.setString(2, title);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // if a result exists, the menu exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // or throw new RuntimeException(e);
        }
    }
    public int getMenuIDByTitleAndRestaurantID(String menuTitle, int restaurantID) {
        try {
            String query = "SELECT menu_id FROM menus WHERE title = ? AND restaurant_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, menuTitle);
            stmt.setInt(2, restaurantID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("menu_id");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void deleteMenuByTitleAndRestaurantID(String title, int restaurantID) throws SQLException {
        int menuID = getMenuIDByTitleAndRestaurantID(title, restaurantID);
        String sql = "DELETE from menus WHERE menu_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setInt(1, menuID);
        preparedStatement.executeUpdate();
    }
    public List<String> getMenuTitlesOfARestaurant(int restaurantID) throws SQLException {
        String query = "SELECT title FROM menus WHERE restaurant_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, restaurantID);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> menuTitles = new ArrayList<>();
                while (rs.next()) {
                    menuTitles.add(rs.getString("title"));
                }
                return menuTitles;
            }
        }
    }

}
