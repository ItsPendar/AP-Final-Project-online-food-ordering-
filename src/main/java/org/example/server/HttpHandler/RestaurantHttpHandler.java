package org.example.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.FoodItemController;
import org.example.server.Controller.MenuController;
import org.example.server.Controller.RestaurantController;
import org.example.server.Controller.UserController;
import org.example.server.dao.UserDAO;
import org.example.server.modules.FoodItem;
import org.example.server.modules.Menu;
import org.example.server.modules.Restaurant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.server.Util.JWTHandler;
import io.jsonwebtoken.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class RestaurantHttpHandler implements HttpHandler {

    private final RestaurantController restaurantController;
    private final MenuController menuController;
    private final FoodItemController foodItemController;

    public RestaurantHttpHandler() throws SQLException {
        this.restaurantController = new RestaurantController();
        this.menuController = new MenuController();
        this.foodItemController = new FoodItemController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/restaurants") && requestMethod.equalsIgnoreCase("POST")) {
            //TODO : create a restaurant
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                Restaurant restaurant = new Restaurant();
                restaurant.setName(json.getString("name"));
                restaurant.setAddress(json.getString("address"));
                restaurant.setPhone(json.getString("phone"));
                restaurant.setLogoBase64(json.getString("logoBase64"));
                restaurant.setTaxFee(json.getDouble("tax_fee"));
                restaurant.setAdditionalFee(json.getDouble("additional_fee"));
                int ownerID = JWTHandler.getUserIDByToken(exchange);
                restaurant.setOwnerID(ownerID);
                restaurantController.createRestaurant(restaurant);
                JSONObject responseJson = new JSONObject();
                responseJson.put("message", "Restaurant created successfully");
                responseJson.put("name", restaurant.getName());
                responseJson.put("address", restaurant.getAddress());
                responseJson.put("phone", restaurant.getPhone());
                responseJson.put("logoBase64",restaurant.getPhone());
                responseJson.put("tax_fee",restaurant.getTaxFee());
                responseJson.put("additional_fee",restaurant.getAdditionalFee());
                responseJson.put("id", RestaurantController.getRestaurantIDByPhone(restaurant.getPhone()));
                responseJson.put("ownerID",ownerID);
                //System.out.println("new restaurant response from server body : " + responseJson);
                byte[] response = responseJson.toString().getBytes();
                exchange.sendResponseHeaders(201, response.length);
                exchange.getResponseBody().write(response);
                exchange.getResponseBody().close();

            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
        else if (path.equals("/restaurants/mine") && requestMethod.equalsIgnoreCase("GET")) {
            //TODO : get list of restaurants
            try {
                int userID = JWTHandler.getUserIDByToken(exchange);
                String userRole = UserController.getUserRoleByID(userID);
                List<Restaurant> restaurants = new ArrayList<>();
                if(userRole.equals("buyer")) {
                    restaurants = restaurantController.getAllRestaurants();
                }
                else if(userRole.equals("seller")) {
                    restaurants = restaurantController.getAnOwnersRestaurants(userID);
                }
                else{
                    sendErrorResponse(exchange,400, "user role not identified" ); //not sure of the status code
                }
                JSONArray responseArray = new JSONArray();
                for (Restaurant r : restaurants) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", r.getName());
                    obj.put("address", r.getAddress());
                    obj.put("phone", r.getPhone());
                    obj.put("tax_fee",r.getTaxFee());
                    obj.put("additional_fee",r.getAdditionalFee());
                    obj.put("id",RestaurantController.getRestaurantIDByPhone(r.getPhone()));
                    obj.put("logoBase64", r.getLogoBase64());
                    responseArray.put(obj);
                }
                byte[] responseBytes = responseArray.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();

            } catch (SQLException e) {
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
        else if (path.matches("/restaurants/\\d+") && requestMethod.equalsIgnoreCase("PUT")) {
            //TODO : update restaurant info
            int id =  Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
            int restaurantID;
            try {
                restaurantID = id;
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange,400,"Invalid restaurant ID");
                return;
            }
            int loggedInUserID = JWTHandler.getUserIDByToken(exchange);
            int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
            if(!(loggedInUserID == restaurantOwnerID)) {
                sendErrorResponse(exchange, 401, "Unauthorized request"); //user doesn't own this restaurant
            }
            else{
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                Restaurant restaurant = new Restaurant();
                restaurant.setName(json.getString("name"));
                restaurant.setAddress(json.getString("address"));
                restaurant.setPhone(json.getString("phone"));
                restaurant.setLogoBase64(json.getString("logoBase64"));
                restaurant.setTaxFee(json.getDouble("tax_fee"));
                restaurant.setAdditionalFee(json.getDouble("additional_fee"));
                try {
                    restaurantController.updateRestaurant(restaurant, restaurantID);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else if (path.matches("/restaurants/\\d+/item") && requestMethod.equals("POST")) {
            System.out.println("add item request detected");
            //TODO : add item to restaurant
            int userID = JWTHandler.getUserIDByToken(exchange);
            int restaurantID = Integer.parseInt(exchange.getRequestURI().getPath().replace("/restaurants/", "").replace("/item", "").split("/")[0]);
            System.out.println("restaurantId is : " + restaurantID);
            int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
            if(!(userID  == restaurantOwnerID)) {
                sendErrorResponse(exchange, 401, "Unauthorized request"); //user doesn't own this restaurant
                return;
            }
            //checking the user owns the restaurant done
            String body = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("body of the request is : " + "sample request body");
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
                int itemID = foodItemController.addFoodItem(newItem);
                System.out.println("item ID is : " + itemID);
                if(itemID > 0){
                    JSONObject responseBody = new JSONObject();
                    responseBody.put("name", newItem.getName());
                    responseBody.put("description", newItem.getDescription());
                    responseBody.put("price", newItem.getPrice());
                    responseBody.put("supply", newItem.getSupply());
                    responseBody.put("keywords", keywordsJson);
                    responseBody.put("vendor_id", restaurantID);
                    responseBody.put("id", itemID);
                    responseBody.put("imageBase64", newItem.getImageBase64());
                    System.out.println("response body is : " + "sampleResponseBody");
                    sendResponse(exchange,200,responseBody);
                }
                else{
                    sendErrorResponse(exchange,500,"Internal server error!");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        else if (path.matches("/restaurants/\\d+/item/\\d+") && requestMethod.equals("PUT")) {
            //TODO : edit an item of a restaurant
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
                    foodItemController.updateFoodItem(itemID,newFoodItem);
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
        else if (path.matches("/restaurants/\\d+/item/\\d+") && requestMethod.equals("DELETE")) {
            //TODO : delete an item of a restaurant
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            int itemID = Integer.parseInt(parts[4]);
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                try {
                    foodItemController.deleteFoodItemFromRestaurant(itemID,restaurantID);
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
        else if (path.matches("/restaurants/\\d+/menu") && requestMethod.equals("POST")) {
            //TODO : add a menu to restaurant
            int userID = JWTHandler.getUserIDByToken(exchange);
            int restaurantID = extractId(path, "/restaurants/", "/menu");
            int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
            System.out.println("ownerId is : " + restaurantOwnerID);
            if(!(userID  == restaurantOwnerID)) {
                sendErrorResponse(exchange,401,"Unauthorized request");//user doesn't own this restaurant
            }
            else{
                String body = new String(exchange.getRequestBody().readAllBytes());
                //System.out.println("request body is : " + body);
                JSONObject json = new JSONObject(body);
                Menu newMenu = new Menu();
                newMenu.setMenuTitle(json.getString("title"));
                newMenu.setRestaurantID(restaurantID);
                //System.out.println("title of the menu is : " + newMenu.getMenuTitle());
                try {
                    if(menuController.addMenu(newMenu)) {
                        JSONObject responseBody = new JSONObject();
                        responseBody.put("title", newMenu.getMenuTitle());
                        sendResponse(exchange,200,responseBody);
                    }
                    else {
                        sendErrorResponse(exchange,500,"Internal server error!");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }//add a new menu
        else if (path.matches("/restaurants/\\d+/menu/[^/]+") && requestMethod.equals("DELETE")) {
            int restaurantID = extractId(path, "/restaurants/", "/menu/");
            String menuTitle = path.substring(path.lastIndexOf("/") + 1);
            //TODO : delete a menu
            String[] parts = path.split("/");
            //int restaurantID = Integer.parseInt(parts[2]);
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                    try {
                        MenuController.deleteMenu(menuTitle, restaurantID);
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
        else if (path.matches("/restaurants/\\d+/menu/[^/]+") && requestMethod.equals("PUT")) {
            int restaurantID = extractId(path, "/restaurants/", "/menu/");
            String menuTitle = path.substring(path.lastIndexOf("/") + 1);
            //TODO : add an item to a menu
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                int itemID = json.getInt("item_id");
                try {
                    foodItemController.addItemToMenu(itemID, menuTitle);
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
        else if (path.matches("/restaurants/\\d+/menu/[^/]+/\\d+") && requestMethod.equals("DELETE")) {
            String[] parts = path.split("/");
            int itemID = Integer.parseInt(parts[2]);
            String menuTitle = parts[4];
            int itemId = Integer.parseInt(parts[5]);
            //TODO : remove item from menu
            try {
                foodItemController.deleteItemFromMenu(itemID, menuTitle);
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
    private void sendResponse(HttpExchange exchange, int statusCode, JSONObject responseBody) throws IOException {
        byte[] response = responseBody.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
    public void handleListRestaurants(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendErrorResponse(exchange, 405, "Only GET requests are allowed.");
            return;
        }

        try {
            List<Restaurant> restaurants = restaurantController.getAllRestaurants();
            JSONArray responseArray = new JSONArray();

            for (Restaurant r : restaurants) {
                JSONObject json = new JSONObject();
                json.put("name", r.getName());
                json.put("address", r.getAddress());
                json.put("phone", r.getPhone());
                json.put("logoBase64", r.getLogoBase64());
                json.put("tax_fee", r.getTaxFee());
                json.put("additional_fee", r.getAdditionalFee());
                responseArray.put(json);
            }

            byte[] response = responseArray.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }
    private int extractId(String path, String prefix, String suffix) {
        try {
            if (!path.startsWith(prefix)) {
                throw new IllegalArgumentException("Path does not start with expected prefix");
            }

            String temp = path.substring(prefix.length());

            if (suffix != null && !suffix.isEmpty()) {
                int endIndex = temp.indexOf(suffix);
                if (endIndex != -1) {
                    temp = temp.substring(0, endIndex);
                }
            }

            return Integer.parseInt(temp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format for extracting ID: " + path);
        }
    }
}
