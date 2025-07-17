package org.example.server.Controller;

import org.example.server.dao.MenuDAO;
import org.example.server.modules.Menu;

import java.sql.SQLException;
import java.util.List;

public class MenuController {
    public static void addMenu(Menu menu) throws SQLException {
        MenuDAO.addMenu(menu);
    }
    public static void deleteMenu(String menuTitle, int restaurantID) throws SQLException {
        MenuDAO.deleteMenuByTitleAndRestaurantID(menuTitle,restaurantID);
    }
    public static List<String> getMenuTitlesOfARestaurant(int restaurantID) throws SQLException {
        return MenuDAO.getMenuTitlesOfARestaurant(restaurantID);
    }
}
