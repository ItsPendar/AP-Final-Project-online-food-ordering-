package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.FoodItemController;
import org.example.server.Controller.RestaurantController;
import org.example.server.Util.JWTHandler;
import org.example.server.modules.FoodItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodItemHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if(requestMethod.equals("POST")) {
            int userID = JWTHandler.getUserIDByToken(exchange);
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
            if(!(userID  == restaurantOwnerID)) {
                sendErrorResponse(exchange, 401, "Unauthorized request"); //user doesn't own this restaurant
                return;
            }
            //checking the user owns the restaurant done
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);
            FoodItem newItem = new FoodItem();
            newItem.setName(json.getString("name"));
            newItem.setDescription(json.getString("description"));
            newItem.setPrice(Double.parseDouble(json.getString("price")));
            newItem.setSupply(Integer.parseInt(json.getString("supply")));
            JSONArray keywordsJson = json.getJSONArray("keywords");
            List<String> keywords = new ArrayList<>();
            for (int i = 0; i < keywordsJson.length(); i++) {
                keywords.add(keywordsJson.getString(i));
            }
            newItem.setKeyword(keywords);
            newItem.setImageBase64(json.getString("imageBase64"));
            newItem.setRestaurantID(restaurantID);
            try {
                FoodItemController.addFoodItem(newItem);//food item added to the database
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        else if(requestMethod.equals("PUT")) {
            //TODO : backend code for editing food item of a restaurant
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            int itemID = Integer.parseInt(parts[4]);
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                FoodItem newFoodItem = new FoodItem();
                newFoodItem.setName(json.getString("name"));
                newFoodItem.setImageBase64(json.getString("imageBase64"));
                newFoodItem.setDescription(json.getString("description"));
                newFoodItem.setPrice(json.getDouble("price"));
                newFoodItem.setSupply(json.getInt("supply"));
                JSONArray keywordsJson = json.getJSONArray("keywords");
                List<String> keywords = new ArrayList<>();
                for (int i = 0; i < keywordsJson.length(); i++) {
                    keywords.add(keywordsJson.getString(i));
                }
                newFoodItem.setKeyword(keywords);
                try {
                    FoodItemController.updateFoodItem(itemID,newFoodItem);
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
        else if(requestMethod.equals("DELETE")) {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            int itemID = Integer.parseInt(parts[4]);
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                try {
                    FoodItemController.deleteFoodItemFromRestaurant(itemID,restaurantID);
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
        else{
            String response = "Request method not allowed";
            exchange.sendResponseHeaders(405, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", message);
        byte[] response = errorJson.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
}
