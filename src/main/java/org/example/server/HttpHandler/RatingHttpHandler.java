package org.example.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import org.example.server.Controller.UserController;
import org.example.server.Util.JWTHandler;
import org.example.server.dao.RatingDAO;
import org.example.server.modules.Rating;
import org.example.server.Controller.RatingController;
import org.example.server.modules.User;
import org.json.JSONObject;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class RatingHttpHandler implements HttpHandler {
    private final RatingDAO ratingDAO;
    public RatingHttpHandler() throws SQLException {
        ratingDAO = new RatingDAO();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("handle starts");
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(exchange, 401, "Missing or invalid token.");
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        Claims claims = JWTHandler.verifyToken(token);
        String userID = claims.getSubject();
        System.out.println(userID);

        User user = null;
        try {
            user = UserController.getUserByID(Integer.parseInt(userID));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(user.getUserRole());
        if (!"buyer".equals(user.getUserRole())) {
            System.out.println("here");
            sendError(exchange, 403, "Access denied.");
            return;
        }

        if (method.equalsIgnoreCase("PUT") && path.equals("/ratings")) {
            handleCreateRating(exchange);
        }
        else if (method.equalsIgnoreCase("GET") && path.matches("/ratings/items/\\d")) {
            handleGetAllReviews(exchange);
        }
        else if (path.matches("/ratings/\\d")) {
            if (method.equalsIgnoreCase("GET")) {
                handleGetRatingById(exchange);
            } else if (method.equalsIgnoreCase("PUT")) {
                handleUpdateRatingById(exchange);
            } else if (method.equalsIgnoreCase("DELETE")) {
                handleDeleteRatingById(exchange);
            }
        }


    }

    private void handleCreateRating(HttpExchange exchange) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            RatingController ratingController = new RatingController();
            Rating rating = mapper.readValue(exchange.getRequestBody(), Rating.class);

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String token = authHeader.substring("Bearer".length());
            Claims claims = JWTHandler.verifyToken(token);
            int userId = Integer.parseInt(claims.getSubject());
            rating.setUserId(userId);

            ratingController.createRating(rating);

            JSONObject responeJson = new JSONObject();
            responeJson.put("message", "Rating crated successfully");

            byte[] response = responeJson.toString().getBytes();
            exchange.sendResponseHeaders(201, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
        catch (Exception e) {
            sendError(exchange, 500, "Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllReviews(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            int itemId = Integer.parseInt(pathParts[pathParts.length - 1]);

            RatingController ratingController = new RatingController();
            List<Rating> ratings = ratingController.getRatingsByItemId(itemId);

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(ratings);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            exchange.getResponseBody().write(jsonResponse.getBytes());
            exchange.getResponseBody().close();

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleGetRatingById(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            int ratingId = Integer.parseInt(pathParts[pathParts.length - 1]);

            RatingController ratingController = new RatingController();
            Rating rating = ratingController.getRatingById(ratingId);

            if (rating == null) {
                sendError(exchange, 404, "Rating not found.");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(rating);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            exchange.getResponseBody().write(jsonResponse.getBytes());
            exchange.getResponseBody().close();
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleUpdateRatingById(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            int ratingId = Integer.parseInt(pathParts[pathParts.length - 1]);

            ObjectMapper mapper = new ObjectMapper();
            Rating updatedRating = mapper.readValue(exchange.getRequestBody(), Rating.class);
            updatedRating.setId(ratingId);

            RatingController ratingController = new RatingController();
            boolean success = ratingController.updateRating(updatedRating);

            if (!success) {
                sendError(exchange, 404, "Rating not found or not updated.");
                return;
            }

            String msg = "{\"message\": \"Rating updated successfully.\"}";
            exchange.sendResponseHeaders(200, msg.getBytes().length);
            exchange.getResponseBody().write(msg.getBytes());
            exchange.getResponseBody().close();
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleDeleteRatingById(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            int ratingId = Integer.parseInt(pathParts[pathParts.length - 1]);

            RatingController ratingController = new RatingController();
            boolean success = ratingController.deleteRating(ratingId);

            if (!success) {
                sendError(exchange, 404, "Rating not found or could not be deleted.");
                return;
            }

            String msg = "{\"message\": \"Rating deleted successfully.\"}";
            exchange.sendResponseHeaders(200, msg.getBytes().length);
            exchange.getResponseBody().write(msg.getBytes());
            exchange.getResponseBody().close();
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", message);
        byte[] response = errorJson.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }

}