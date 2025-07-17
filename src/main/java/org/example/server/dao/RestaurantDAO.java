package org.example.server.dao;

import org.example.server.modules.Restaurant;
import org.example.server.modules.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {
    private static final Connection connection;

    static {
        try {
            connection = DatabaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public RestaurantDAO() throws SQLException {
        this.createRestaurantTable();
    }

    public void createRestaurantTable() throws SQLException {
//            FOREIGN KEY (owner_id) REFERENCES users(userID)
        PreparedStatement preparedStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS restaurants (" +
                        "restaurant_id SERIAL PRIMARY KEY, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "phone VARCHAR(20) NOT NULL, " +
                        "address TEXT, " +
                        "logo_base64 TEXT, " +
                        "tax_fee REAL, " +
                        "additional_fee REAL, " +
                        "owner_id INTEGER, " +
                        "FOREIGN KEY (owner_id) REFERENCES users(userid)" +
                        ")"
        );
        preparedStatement.executeUpdate();
    }

    public List<Restaurant> getAllRestaurants() throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = "SELECT * FROM restaurants";
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Restaurant restaurant = new Restaurant();
            restaurant.setName(rs.getString("name"));
            restaurant.setAddress(rs.getString("address"));
            restaurant.setPhone(rs.getString("phone"));
            restaurant.setLogoBase64(rs.getString("logo_base64"));
            restaurant.setTaxFee(rs.getInt("tax_fee"));
            restaurant.setAdditionalFee(rs.getInt("additional_fee"));
            restaurants.add(restaurant);
        }

        return restaurants;
    }


    public void createRestaurant(Restaurant restaurant) throws SQLException {
        String query = "INSERT INTO restaurants (name, address, phone, logo_base64, tax_fee, additional_fee, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, restaurant.getName());
        stmt.setString(2, restaurant.getAddress());
        stmt.setString(3, restaurant.getPhone());
        stmt.setString(4, restaurant.getLogoBase64());
        stmt.setDouble(5, restaurant.getTaxFee());
        stmt.setDouble(6, restaurant.getAdditionalFee());
        stmt.setInt(7, restaurant.getOwnerID());
        stmt.executeUpdate();
    }
    public String getRestaurantIDByPhoneNumber(String phoneNumber) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT restaurant_id FROM restaurants WHERE phone = ?"
            );
            preparedStatement.setString(1, phoneNumber);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("restaurant_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateRestaurant(Restaurant restaurant, int restaurantID) throws SQLException {
        String sql = "UPDATE users SET name = ?,phone_number = ?, address = ?, logo_base64 = ?, tax_fee = ?, additional_fee = ? WHERE restaurant_id = ?";
        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setString(1, restaurant.getName());
        preparedStatement.setString(2,restaurant.getPhone());
        preparedStatement.setString(3,restaurant.getAddress());
        preparedStatement.setString(4,restaurant.getLogoBase64());
        preparedStatement.setDouble(5,restaurant.getTaxFee());
        preparedStatement.setDouble(6,restaurant.getAdditionalFee());
        preparedStatement.setInt(7,restaurantID);
        preparedStatement.executeUpdate();
    }
    public static int getOwnerIDFromRestaurantID(int restaurantID) {
        try {
            String query = "SELECT owner_id FROM restaurants WHERE restaurant_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, restaurantID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Integer.parseInt(rs.getString("owner_id"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Restaurant getRestaurantByID(int restaurantID) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM restaurants WHERE restaurant_id = ?"
            );
            preparedStatement.setInt(1, restaurantID);

            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Restaurant restaurant = new Restaurant();
                restaurant.setRestaurantID(resultSet.getInt("restaurant_id"));
                return getrestaurant(resultSet, restaurant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // User not found
    }

    private static Restaurant getrestaurant(ResultSet resultSet, Restaurant restaurant) throws SQLException {
        restaurant.setName(resultSet.getString("name"));
        restaurant.setPhone(resultSet.getString("phone"));
        restaurant.setAddress(resultSet.getString("address"));
        restaurant.setTaxFee(resultSet.getDouble("tax_fee"));
        restaurant.setAdditionalFee(resultSet.getDouble("additional_fee"));
        restaurant.setLogoBase64(resultSet.getString("logoBase64"));
        return restaurant;
    }
}
