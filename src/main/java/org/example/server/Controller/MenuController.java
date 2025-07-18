package org.example.server.Controller;

import org.example.server.dao.MenuDAO;
import org.example.server.modules.Menu;

import java.sql.SQLException;
import java.util.List;

public class MenuController {
    private static MenuDAO menuDAO;

    public MenuController() throws SQLException {
        menuDAO = new MenuDAO();
    }

    public boolean addMenu(Menu menu) throws SQLException {
        return menuDAO.addMenu(menu);
    }
    public static void deleteMenu(String menuTitle, int restaurantID) throws SQLException {
        menuDAO.deleteMenuByTitleAndRestaurantID(menuTitle,restaurantID);
    }
    public static List<String> getMenuTitlesOfARestaurant(int restaurantID) throws SQLException {
        return menuDAO.getMenuTitlesOfARestaurant(restaurantID);
    }
}
