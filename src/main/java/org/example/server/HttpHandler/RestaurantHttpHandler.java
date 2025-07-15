package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.RestaurantController;
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
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendErrorResponse(exchange, 405, "Method Not Allowed. Only GET requests are allowed.");
            return;
        }

        try {
            List<Restaurant> restaurants = restaurantController.getAllRestaurants();

            JSONArray responseArray = new JSONArray();
            for (Restaurant r : restaurants) {
                JSONObject obj = new JSONObject();
                obj.put("name", r.getName());
                obj.put("address", r.getAddress());
                obj.put("logobase64", r.getLogoBase64());
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

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", message);
        byte[] response = errorJson.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }

    public void handleCreateRestaurant(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendErrorResponse(exchange, 405, "Only POST requests are allowed.");
            return;
        }

        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);

            Restaurant restaurant = new Restaurant();
            restaurant.setName(json.getString("name"));
            restaurant.setAddress(json.getString("address"));
            restaurant.setPhone(json.getString("phone"));
            restaurant.setLogoBase64(json.getString("logoBase64"));
            restaurant.setTaxFee(json.getInt("tax_fee"));
            restaurant.setAdditionalFee(json.getInt("additional_fee"));

            restaurantController.createRestaurant(restaurant);

            JSONObject responseJson = new JSONObject();
            responseJson.put("message", "Restaurant created successfully");

            byte[] response = responseJson.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();

        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
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
