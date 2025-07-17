package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.RestaurantController;
import org.example.server.Controller.UserController;
import org.example.server.dao.UserDAO;
import org.example.server.modules.Restaurant;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.server.Util.JWTHandler;
import io.jsonwebtoken.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class RestaurantHttpHandler implements HttpHandler {

    private final RestaurantController restaurantController;

    public RestaurantHttpHandler() throws SQLException {
        this.restaurantController = new RestaurantController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase("GET")) { // create a new restaurant
            sendErrorResponse(exchange, 405, "Method Not Allowed.");
            return;
        }
        try {
            int userID = JWTHandler.getUserIDByToken(exchange);
            String userRole = UserController.getUserRoleByID(userID);
            if(userRole.equals("buyer")) {
                List<Restaurant> restaurants = restaurantController.getAllRestaurants();
                JSONArray responseArray = new JSONArray();
                for (Restaurant r : restaurants) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", r.getName());
                    obj.put("address", r.getAddress());
                    obj.put("logoBase64", r.getLogoBase64());
                    responseArray.put(obj);
                }
                byte[] responseBytes = responseArray.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
            }
            else if(userRole.equals("seller")) {
                List<Restaurant> restaurants = restaurantController.getAnOwnersRestaurants(userID);
                JSONArray responseArray = new JSONArray();
                for (Restaurant r : restaurants) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", r.getName());
                    obj.put("address", r.getAddress());
                    obj.put("logoBase64", r.getLogoBase64());
                    responseArray.put(obj);
                }
                byte[] responseBytes = responseArray.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
            }
            else{
                sendErrorResponse(exchange,400, "user role not identified" ); //not sure of the status code
            }
        } catch (SQLException e) {
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
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

    public void handleCreateRestaurant(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("POST")) {//adding a new restaurant
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
        else if(requestMethod.equalsIgnoreCase("PUT")) { //update restaurant info
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 3) {
                sendErrorResponse(exchange, 400, "Missing restaurant ID in path.");
                return;
            }
            String idStr = parts[2];
            int restaurantID;
            try {
                restaurantID = Integer.parseInt(idStr);

            } catch (NumberFormatException e) {
                sendErrorResponse(exchange,400,"Invalid restaurant ID");
                return;
            }
            int loggedInUserID = JWTHandler.getUserIDByToken(exchange);
            int restaurantOwnerID = restaurantController.getOwnerIDFromRestaurantID(restaurantID);
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
        else {
            sendErrorResponse(exchange, 405, "Request method not allowed");
            return;
        }

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


}
