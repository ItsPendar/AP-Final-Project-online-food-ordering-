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
                obj.put("id", r.getRestaurantID());
                obj.put("name", r.getName());
                obj.put("address", r.getAddress());
                obj.put("logo_image", r.getLogoImage());
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

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(exchange, 401, "Missing or invalid Authorization header.");
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        Claims claims = JWTHandler.verifyToken(token);
        if (claims == null) {
            sendErrorResponse(exchange, 401, "Invalid or expired token.");
            return;
        }

        String userID = claims.getSubject();
        String userRole = UserDAO.getUserRoleByUserID(userID);


        if (userRole == null || !userRole.equals("seller")) {
            sendErrorResponse(exchange, 403, "Only sellers can create restaurants.");
            return;
        }

        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);

            String name = json.optString("name");
            String address = json.optString("address");
            String phoneNumber = json.optString("phone");
            String workingHours = json.optString("working_hours");
            String logoImage = json.optString("logo_image");

            if (name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
                sendErrorResponse(exchange, 400, "Missing required fields.");
                return;
            }

            Restaurant restaurant = new Restaurant();
            restaurant.setOwnerID(userID);
            restaurant.setName(name);
            restaurant.setAddress(address);
            restaurant.setPhoneNumber(phoneNumber);
            restaurant.setWorkingHours(workingHours);
            restaurant.setLogoImage(logoImage);
            restaurant.setApproved(false); // تایید نشده تا بررسی ادمین

            restaurantController.createRestaurant(restaurant);

            JSONObject responseJson = new JSONObject();
            responseJson.put("message", "Restaurant submitted successfully. Waiting for admin approval.");

            byte[] response = responseJson.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();

        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

}
