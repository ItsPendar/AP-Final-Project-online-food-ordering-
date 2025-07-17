package org.example.server.Controller;

import org.example.server.dao.RestaurantDAO;
import org.example.server.modules.Restaurant;

import java.sql.SQLException;
import java.util.List;

public class RestaurantController {
    private static RestaurantDAO restaurantDAO = null;

    public RestaurantController() throws SQLException {
        restaurantDAO = new RestaurantDAO();
    }

    public List<Restaurant> getAllRestaurants() throws SQLException {
        return restaurantDAO.getAllRestaurants();
    }
    public List<Restaurant> getAnOwnersRestaurants(int ownerID) throws SQLException {
        return restaurantDAO.getAnOwnersRestaurants(ownerID);
    }

    public void createRestaurant(Restaurant restaurant) throws SQLException {
        restaurantDAO.createRestaurant(restaurant);
    }

    public static String getRestaurantIDByPhone(String phoneNumber) {
        return restaurantDAO.getRestaurantIDByPhoneNumber(phoneNumber);
    }

    public void updateRestaurant(Restaurant restaurant, int restaurantID) throws SQLException {
        RestaurantDAO.updateRestaurant(restaurant,restaurantID);
    }

    public static int getOwnerIDFromRestaurantID(int restaurantID) {
        return RestaurantDAO.getOwnerIDFromRestaurantID(restaurantID);
    }
    public static Restaurant getRestaurantByID(int restaurantID) {
        return RestaurantDAO.getRestaurantByID(restaurantID);
    }
    // بعداً:
    // public void addRestaurant(...)
    // public Restaurant getRestaurantById(...)
}
