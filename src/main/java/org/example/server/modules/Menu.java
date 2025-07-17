package org.example.server.modules;

public class Menu {
    private String menuTitle;

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    private int menuID;
    private int restaurantID;

    public int getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(int restaurantID) {
        this.restaurantID = restaurantID;
    }

    public Menu() {

    }
    public Menu(int restaurantID, String menuTitle) {
        this.restaurantID = restaurantID;
        this.menuTitle = menuTitle;
    }
    public int getMenuID() {
        return menuID;
    }

    public void setMenuID(int menuID) {
        this.menuID = menuID;
    }

    public String getMenuTitle() {
        return menuTitle;
    }


}
