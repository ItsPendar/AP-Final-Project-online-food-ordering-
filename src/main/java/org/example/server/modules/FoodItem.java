package org.example.server.modules;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FoodItem {
    private int foodItemID;
    private int restaurantID;
    private List<String> menuTitle;
    private String name;
    private String description;
    private double price;
    private int supply;
    private List<String> keyword = new ArrayList<>();
    private String imageBase64;

    public FoodItem() {

    }

    public FoodItem(int foodItemID, int restaurantID, List<String> menuTitle, String name, String description, double price, int supply, List<String> keyword, String imageBase64) {
        this.foodItemID = foodItemID;
        this.restaurantID = restaurantID;
        this.menuTitle = menuTitle;
        this.name = name;
        this.description = description;
        this.price = price;
        this.supply = supply;
        this.keyword = keyword;
        this.imageBase64 = imageBase64;
    }

    public int getFoodItemID() {
        return foodItemID;
    }

    public void setFoodItemID(int foodItemID) {
        this.foodItemID = foodItemID;
    }

    public int getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(int restaurantID) {
        this.restaurantID = restaurantID;
    }

    public List<String> getMenuTitle() {
        return menuTitle;
    }

    public void setMenuTitle(List<String> menuTitle) {
        this.menuTitle = menuTitle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    @Override
    public String toString() {
        return "FoodItem{" +
                "foodItemID=" + foodItemID +
                ", restaurantID=" + restaurantID +
                ", menuTitle='" + menuTitle + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", supply=" + supply +
                ", keyword=" + keyword +
                ", imageBase64='" + imageBase64 + '\'' +
                '}';
    }
}
