package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.FoodItemController;
import org.example.server.Controller.MenuController;
import org.example.server.Controller.RestaurantController;
import org.example.server.Util.JWTHandler;
import org.example.server.modules.Menu;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class MenuHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if(requestMethod.equals("POST")) {
            //TODO : backend code for adding menu to restaurant
            int userID = JWTHandler.getUserIDByToken(exchange);
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
            if(!(userID  == restaurantOwnerID)) {
                String response = "Unauthorized request";
                exchange.sendResponseHeaders(401, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();//user doesn't own this restaurant
            }
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);
            Menu newMenu = new Menu();
            newMenu.setMenuTitle(json.getString("title"));
            newMenu.setRestaurantID(restaurantID);
            try {
                MenuController.addMenu(newMenu);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        else if(requestMethod.equals("PUT")){
            //TODO : backend code for adding a food item to a menu
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                String menuTitle = parts[4].trim();
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                int itemID = json.getInt("item_id");
                try {
                    FoodItemController.addItemToMenu(itemID, menuTitle);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                String response = "Unauthorized request";
                exchange.sendResponseHeaders(401, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();//user doesn't own this restaurant
            }
        }
        else if(requestMethod.equals("DELETE")){
            //TODO : backend code for deleting a menu
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                if(parts.length == 4) {//deleting menu from restaurant
                    String menuTitle = parts[4].trim();
                    try {
                        MenuController.deleteMenu(menuTitle, restaurantID);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if(parts.length == 5) {//deleting food item from menu
                    String menuTitle = parts[4];
                    int itemID = Integer.parseInt(parts[5]);
                    try {
                        FoodItemController.deleteItemFromMenu(itemID);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    String response = "Page not found";
                    exchange.sendResponseHeaders(404, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();//URI not found
                }
            }
            else{
                String response = "Unauthorized request";
                exchange.sendResponseHeaders(401, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();//user doesn't own this restaurant
            }
        }
        else{
            String response = "Request method not allowed";
            exchange.sendResponseHeaders(405, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}
