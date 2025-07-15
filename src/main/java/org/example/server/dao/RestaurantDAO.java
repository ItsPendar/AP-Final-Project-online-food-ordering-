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
        PreparedStatement stmt = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS restaurants (" +
                        "restaurant_id SERIAL PRIMARY KEY, " +
                        "owner_id VARCHAR(255) NOT NULL, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "address TEXT, " +
                        "phone_number VARCHAR(20), " +
                        "working_hours VARCHAR(100), " +
                        "logo_image TEXT, " +
                        "is_approved BOOLEAN DEFAULT FALSE" +
                        ")"
        );
        stmt.executeUpdate();
    }


    public List<Restaurant> getAllRestaurants() throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();

        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM restaurants");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Restaurant restaurant = new Restaurant();
            restaurant.setRestaurantID(String.valueOf(rs.getInt("restaurant_id")));
            restaurant.setOwnerID(rs.getString("owner_id"));
            restaurant.setName(rs.getString("name"));
            restaurant.setAddress(rs.getString("address"));
            restaurant.setLogoImage(rs.getString("logo_image"));

            restaurants.add(restaurant);
        }

        return restaurants;
    }

    public void createRestaurant(Restaurant restaurant) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO restaurants (owner_id, name, address, phone_number, working_hours, logo_image, is_approved) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)"
        );

        stmt.setString(1, restaurant.getOwnerID());
        stmt.setString(2, restaurant.getName());
        stmt.setString(3, restaurant.getAddress());
        stmt.setString(4, restaurant.getPhoneNumber());
        stmt.setString(5, restaurant.getWorkingHours());
        stmt.setString(6, restaurant.getLogoImage());
        stmt.setBoolean(7, restaurant.isApproved());

        stmt.executeUpdate();
    }



}
