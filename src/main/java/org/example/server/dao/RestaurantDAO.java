package org.example.server.dao;

import org.example.server.modules.Restaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {
    private final Connection connection = DatabaseConnectionManager.getConnection();

    public RestaurantDAO() throws SQLException {
        this.createRestaurantTable();
    }

    public void createRestaurantTable() throws SQLException {
        String query = """
        CREATE TABLE IF NOT EXISTS restaurants (
            restaurant_id SERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            address TEXT,
            phone VARCHAR(20),
            logo_base64 TEXT,
            tax_fee INTEGER DEFAULT 9,
            additional_fee INTEGER DEFAULT 2,
            owner_id INTEGER,
            FOREIGN KEY (owner_id) REFERENCES users(userID)
        )
    """;

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.executeUpdate();
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
        String query = "INSERT INTO restaurants (name, address, phone, logo_base64, tax_fee, additional_fee) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, restaurant.getName());
        stmt.setString(2, restaurant.getAddress());
        stmt.setString(3, restaurant.getPhone());
        stmt.setString(4, restaurant.getLogoBase64());
        stmt.setInt(5, restaurant.getTaxFee());
        stmt.setInt(6, restaurant.getAdditionalFee());
        stmt.executeUpdate();
    }




}
