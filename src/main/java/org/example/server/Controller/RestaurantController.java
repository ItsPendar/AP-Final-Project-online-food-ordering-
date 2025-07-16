package org.example.server.Controller;

import org.example.server.dao.RestaurantDAO;
import org.example.server.modules.Restaurant;

import java.sql.SQLException;
import java.util.List;

public class RestaurantController {
    private final RestaurantDAO restaurantDAO;

    public RestaurantController() throws SQLException {
        this.restaurantDAO = new RestaurantDAO();
    }

    public List<Restaurant> getAllRestaurants() throws SQLException {
        return restaurantDAO.getAllRestaurants();
    }

    public void createRestaurant(Restaurant restaurant) throws SQLException {
        restaurantDAO.createRestaurant(restaurant);
    }

    public String getRestaurantIDByPhone(String phoneNumber) {
        return restaurantDAO.getRestaurantIDByPhoneNumber(phoneNumber);
    }



    // بعداً:
    // public void addRestaurant(...)
    // public Restaurant getRestaurantById(...)
}
