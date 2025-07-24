package org.example.server.HttpHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.FoodItemController;
import org.example.server.Controller.MenuController;
import org.example.server.Controller.RestaurantController;
import org.example.server.Controller.UserController;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.ResponseHandler;
import org.example.server.dao.RestaurantDAO;
import org.example.server.modules.Restaurant;
import org.example.server.modules.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VendorHttpHandler implements HttpHandler {
    private final FoodItemController foodItemController;
    private final RestaurantController restaurantController;
    public VendorHttpHandler() throws SQLException {
        this.foodItemController = new FoodItemController();
        this.restaurantController = new RestaurantController();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (path.matches("/vendors/\\d+") && requestMethod.equals("GET")) {
            //TODO : get list of menus and items ✅
            int vendorID = extractId(path, "/vendors/", "");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode vendorInfoJson = mapper.createObjectNode();
            ObjectNode response = mapper.createObjectNode();
            ArrayNode menuItemsArrayNode;
            //TODO : add vendor info to vendorInfoJson and add vendorInfoJson to response ✅
            Restaurant vendor = new Restaurant();
            vendor = RestaurantDAO.getRestaurantByID(vendorID);
            vendorInfoJson.put("name", vendor.getName());
            vendorInfoJson.put("address", vendor.getAddress());
            vendorInfoJson.put("phone", vendor.getPhone());
            vendorInfoJson.put("tax_fee", vendor.getTaxFee());
            vendorInfoJson.put("additional_fee", vendor.getAdditionalFee());
            vendorInfoJson.put("logoBase64", vendor.getLogoBase64());
            vendorInfoJson.put("id", vendorID);
            response.set("vendor", vendorInfoJson);
            //TODO : add menu titles to menuTitlesJson and add menuTitlesJson to response ✅
            //and TODO : add food items to a json named by the menu title and add this json to response ✅
            ArrayNode arrayNode = mapper.createArrayNode();
            try {
                for (String title : MenuController.getMenuTitlesOfARestaurant(vendorID)) {
                    menuItemsArrayNode = foodItemController.getItemsInAMenu(title,vendorID);
                    response.set(title, menuItemsArrayNode);
                    arrayNode.add(title);
                }
                for(int id : foodItemController.getItemIDsInARestaurant(vendorID)) {
                    response.set(String.valueOf(id),foodItemController.getMenusOfAnItem(id));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            response.set("menu_titles", arrayNode);
            sendResponse(exchange,200,response);
        }//get list of menus and items for a restaurant✅
        else if(path.matches("/vendors") && requestMethod.equals("POST")) {
            System.out.println("search request detected");
            User user = null;
            try {
                user = JWTHandler.getUserByToken(exchange);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(user == null){
                ResponseHandler.sendErrorResponse(exchange,401,"Unauthorized request");
                return;
            }
            if(!user.getUserRole().equals("buyer")){
                ResponseHandler.sendErrorResponse(exchange,403,"Forbidden request");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(body);
            String searchText = json.get("search").asText();
            List<Restaurant> restaurants = new ArrayList<>();
            try {
                restaurants = restaurantController.searchRestaurantsByText(searchText);
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
        }
        else{
            ResponseHandler.sendErrorResponse(exchange,405 , "Request method not allowed");
        }
    }
    private int extractId(String path, String prefix, String suffix) {
        try {
            String temp = path.substring(prefix.length());
            if (!suffix.isEmpty() && temp.contains(suffix)) {
                temp = temp.substring(0, temp.indexOf(suffix));
            }
            return Integer.parseInt(temp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format for extracting ID");
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
    private void sendResponse(HttpExchange exchange, int statusCode, ObjectNode responseBody) throws IOException {
        byte[] response = responseBody.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }

}
