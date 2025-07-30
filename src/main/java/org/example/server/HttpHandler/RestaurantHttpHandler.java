package org.example.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.FoodItemController;
import org.example.server.Controller.MenuController;
import org.example.server.Controller.RestaurantController;
import org.example.server.Controller.UserController;
import org.example.server.Util.ResponseHandler;
import org.example.server.dao.UserDAO;
import org.example.server.modules.FoodItem;
import org.example.server.modules.Menu;
import org.example.server.modules.Restaurant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.server.Util.JWTHandler;
import io.jsonwebtoken.*;
import org.xml.sax.ext.DeclHandler;

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
        System.out.println("initial path : " + path);
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
        }//create a restaurant✅
        else if (path.matches("/restaurants/\\d+/info") && requestMethod.equalsIgnoreCase("GET")) {
            System.out.println("get restaurant info request detected");
            int userID = JWTHandler.getUserIDByToken(exchange);
            int restaurantID = Integer.parseInt(exchange.getRequestURI().getPath().replace("/restaurants/", "").replace("/item", "").split("/")[0]);
            System.out.println("restaurant id in get res info : " + restaurantID);
            int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
            if(!(userID  == restaurantOwnerID)) {
                sendErrorResponse(exchange, 401, "Unauthorized request"); //user doesn't own this restaurant
                return;
            }
            JSONObject responseBody = new JSONObject();
            Restaurant restaurant = RestaurantController.getRestaurantByID(restaurantID);
            responseBody.put("name", restaurant.getName());
            responseBody.put("address", restaurant.getAddress());
            responseBody.put("phone", restaurant.getPhone());
            responseBody.put("logoBase64", restaurant.getLogoBase64());
            responseBody.put("tax_fee", restaurant.getTaxFee());
            responseBody.put("additional_fee", restaurant.getAdditionalFee());
            ResponseHandler.sendResponse(exchange,200, responseBody);
        }//get info of a restaurant✅
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
        }//get list of restaurants✅
        else if (path.matches("/restaurants/\\d+") && requestMethod.equalsIgnoreCase("PUT")) {
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
                    ResponseHandler.sendResponse(exchange,200, "updated restaurant info successfully");
                } catch (SQLException e) {
                    ResponseHandler.sendErrorResponse(exchange,500,"Internal server error : couldn't update restaurant info");
                    throw new RuntimeException(e);
                }
            }
        }//update restaurant info✅
        else if (path.matches("/restaurants/\\d+/item") && requestMethod.equals("POST")) {
            //TODO : add item to restaurant
            int userID = JWTHandler.getUserIDByToken(exchange);
            int restaurantID = Integer.parseInt(exchange.getRequestURI().getPath().replace("/restaurants/", "").replace("/item", "").split("/")[0]);
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
                    sendResponse(exchange,200,responseBody);
                }
                else{
                    sendErrorResponse(exchange,500,"Internal server error!");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }//add item to restaurant✅
        else if (path.matches("/restaurants/\\d+/item/\\d+") && requestMethod.equals("PUT")) {
            //TODO : edit an item of a restaurant
            int restaurantID = Integer.parseInt(path.split("/")[2]);
            int itemID = Integer.parseInt(path.split("/")[4]);
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
                JSONObject responseBody = new JSONObject();
                try {
                    if(foodItemController.updateFoodItem(itemID,newFoodItem)) {
                        responseBody.put("name", newFoodItem.getName());
                        responseBody.put("description",newFoodItem.getDescription());
                        responseBody.put("price",newFoodItem.getPrice());
                        responseBody.put("supply", newFoodItem.getSupply());
                        responseBody.put("keywords",newFoodItem.getKeyword());
                        responseBody.put("vendor_id", restaurantID);
                        responseBody.put("id",itemID);
                        responseBody.put("imageBase64",newFoodItem.getImageBase64());
                        sendResponse(exchange,200,responseBody);
                    }
                    else{
                        sendErrorResponse(exchange,500,"internal server error. Couldn't update the item");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                sendErrorResponse(exchange,401,"Unauthorized request");
            }
        }//edit an item of a restaurant✅
        else if (path.matches("/restaurants/\\d+/item/\\d+") && requestMethod.equals("DELETE")) {
            //TODO : delete an item of a restaurant
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            int itemID = Integer.parseInt(parts[4]);
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                try {
                    if(foodItemController.deleteFoodItemFromRestaurant(itemID,restaurantID)){
                        JSONObject response = new JSONObject();
                        response.put("message", "food item deleted successfully");
                        sendResponse(exchange,200,response);
                    }
                    else{
                        sendErrorResponse(exchange,500,"internal server error : couldn't delete the item");
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                sendErrorResponse(exchange,401,"Unauthorized request");
            }
        }//delete an item of a restaurant✅
        else if (path.matches("/restaurants/\\d+/menu") && requestMethod.equals("POST")) {
            //TODO : add a menu to restaurant
            int userID = JWTHandler.getUserIDByToken(exchange);
            int restaurantID = extractId(path, "/restaurants/", "/menu");
            int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
            if(!(userID  == restaurantOwnerID)) {
                sendErrorResponse(exchange,401,"Unauthorized request");//user doesn't own this restaurant
            }
            else{
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                Menu newMenu = new Menu();
                newMenu.setMenuTitle(json.getString("title"));
                newMenu.setRestaurantID(restaurantID);
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
        }//add a new menu✅
        else if (path.matches("/restaurants/\\d+/menu/[^/]+") && requestMethod.equals("DELETE")) {
            int restaurantID = extractId(path, "/restaurants/", "/menu/");
            String menuTitle = path.substring(path.lastIndexOf("/") + 1);
            //TODO : delete a menu
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                    try {
                        MenuController.deleteMenu(menuTitle, restaurantID);
                        sendResponse(exchange,200,"menu deleted successfully");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
            }
            else{
                String response = "Unauthorized request1";
                sendErrorResponse(exchange,401,response);
            }
        }//delete a menu✅
        else if (path.matches("/restaurants/\\d+/menu/[^/]+") && requestMethod.equals("PUT")) {
            int restaurantID = extractId(path, "/restaurants/", "/menu/");
            String menuTitle = path.substring(path.lastIndexOf("/") + 1);
            //TODO : add an item to a menu
            if(JWTHandler.doesUserOwnRestaurant(exchange,restaurantID)) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                int itemID = json.getInt("item_id");
                try {
                    if(foodItemController.addItemToMenu(itemID, menuTitle)) {
                        sendResponse(exchange,200,"added item to menu successfully");
                    }
                    else {
                        sendErrorResponse(exchange,500,"internal server error : couldn't add the item to menu");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                sendErrorResponse(exchange,401,"Unauthorized request");
            }
        }//add an item to a menu✅
        else if (path.matches("/restaurants/\\d+/menu/[^/]+/\\d+") && requestMethod.equals("DELETE")) {
            String[] parts = path.split("/");
            int restaurantID = Integer.parseInt(parts[2]);
            String menuTitle = parts[4];
            int itemID = Integer.parseInt(parts[5]);
            //TODO : remove item from menu
            try {
                if(foodItemController.deleteItemFromMenu(itemID, menuTitle)) {
                    sendResponse(exchange,200,"deleted the item from menu successfully");
                }
                else {
                    sendErrorResponse(exchange,500,"Internal server error : failed to delete item from menu");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }//remove item from menu✅
        else{
            sendErrorResponse(exchange,405,"Request method not allowed");
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

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject messageJson = new JSONObject();
        messageJson.put("message", message);
        byte[] response = messageJson.toString().getBytes();
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
