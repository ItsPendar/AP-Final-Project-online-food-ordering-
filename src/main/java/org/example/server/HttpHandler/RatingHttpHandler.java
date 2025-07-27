package org.example.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import org.example.server.Controller.UserController;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.ResponseHandler;
import org.example.server.dao.RatingDAO;
import org.example.server.modules.Rating;
import org.example.server.Controller.RatingController;
import org.example.server.modules.User;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BooleanSupplier;

public class RatingHttpHandler implements HttpHandler {
    private final RatingDAO ratingDAO;
    private final RatingController ratingController;
    public RatingHttpHandler() throws SQLException {
        ratingDAO = new RatingDAO();
        ratingController = new RatingController();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

        User user = null;
        try {
            user = UserController.getUserByID(Integer.parseInt(userID));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(user.getUserRole());
        if (!"buyer".equals(user.getUserRole()) && !"seller".equals(user.getUserRole())) {
            System.out.println("here");
            sendError(exchange, 403, "Access denied.");
            return;
        }

        if (method.equalsIgnoreCase("POST") && path.equals("/ratings")) {
            handleCreateRating(exchange);
        }//add a rating✅
        else if (method.equalsIgnoreCase("GET") && path.matches("/ratings/items/\\d")) {
            handleGetAllReviews(exchange);
        }//get the ratings for an item
        else if (method.equalsIgnoreCase("GET") && path.matches("/ratings/\\d")) {
            if (method.equalsIgnoreCase("GET")) {
                handleGetRatingById(exchange);
            }//get the info of a rating
            else if (method.equalsIgnoreCase("PUT")) {
                handleUpdateRatingById(exchange);
            }//update a rating
            else if (method.equalsIgnoreCase("DELETE")) {
                handleDeleteRatingById(exchange);
            }//delete a rating
            else {
                ResponseHandler.sendErrorResponse(exchange,404,"Page not found");
            }
        }//get a specific rating by rating ID
        else if (method.equalsIgnoreCase("PUT") && path.matches("/ratings/\\d")) {
            handleUpdateRatingById(exchange);
        }
        else if(method.equalsIgnoreCase("GET") && path.matches("/ratings/orders/\\d")){
            try {
                getRatingOfAnOrder(exchange);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else if(method.equalsIgnoreCase("GET") && path.matches("/ratings/users")) {
            try {
                getAllRatingForUser(exchange);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            ResponseHandler.sendErrorResponse(exchange, 404, "Page not found");
        }
    }

    private void handleCreateRating(HttpExchange exchange) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            RatingController ratingController = new RatingController();
            Rating rating = mapper.readValue(exchange.getRequestBody(), Rating.class);;
            int userId = JWTHandler.getUserIDByToken(exchange);
            if (!UserController.getUserRoleByID(userId).equals("buyer")) {
                ResponseHandler.sendErrorResponse(exchange,401,"You can't add a comment duo to your user role");
            }
            rating.setUser_id(userId);
            ratingController.createRating(rating);

            JSONObject responseJson = new JSONObject();
            responseJson.put("message", "Rating crated successfully");

            byte[] response = responseJson.toString().getBytes();
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
            JSONArray commentsArray = new JSONArray();
            for (Rating rating : ratings) {
                JSONObject jsonRating = new JSONObject();
                jsonRating.put("id", rating.getId());
                jsonRating.put("item_ids", rating.getItem_ids());
                jsonRating.put("order_id",rating.getOrder_id());
                jsonRating.put("rating", rating.getRating());
                jsonRating.put("comment", rating.getComment());
                jsonRating.put("user_id", rating.getUser_id());
                jsonRating.put("created_at", rating.getCreated_at());
                jsonRating.put("imageBase64", new JSONArray(rating.getImageBase64()));
                commentsArray.put(jsonRating);
            }

            JSONObject responseJson = new JSONObject();
            responseJson.put("comments", commentsArray);
            ResponseHandler.sendResponse(exchange,200,responseJson);

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

    private void getRatingOfAnOrder(HttpExchange exchange) throws IOException, SQLException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int orderID = Integer.parseInt(pathParts[pathParts.length - 1]);
        System.out.println("order ID in rating Http handler is : " + orderID);
        Rating rating = ratingController.getRatingByOrderId(orderID);
        if(rating == null) {
            System.out.println("404 has been sent for : " + orderID);
            ResponseHandler.sendResponse(exchange,404,"this order has no ratings");
        }
        else {
            ObjectMapper mapper = new ObjectMapper();
            JSONObject responseBody = new JSONObject();
            responseBody.put("comment", rating.getComment());
            System.out.println("comment is : " + rating.getComment());
            responseBody.put("rating", rating.getRating());
            responseBody.put("user_id",rating.getUser_id());
            responseBody.put("created_at",rating.getCreated_at());
            responseBody.put("item_ids",rating.getItem_ids());
            responseBody.put("order_id",rating.getOrder_id());
            responseBody.put("vendor_id",rating.getVendor_id());
            responseBody.put("id",rating.getId());
            responseBody.put("imageBase64",rating.getImageBase64());
            System.out.println("json response for " +orderID + " : " + responseBody);
            ResponseHandler.sendResponse(exchange,200,responseBody);
        }
    }

    private void getAllRatingForUser(HttpExchange exchange) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        int userID = JWTHandler.getUserIDByToken(exchange);

        System.out.println("userID is : " + userID);
        List<Rating> ratings = ratingController.getRatingsByUserId(userID);
        System.out.println("size of ratings list : " + ratings.size());
        JSONArray commentsArray = new JSONArray();
        for (Rating rating : ratings) {
            JSONObject jsonRating = new JSONObject();
            jsonRating.put("id", rating.getId());
            jsonRating.put("item_ids", rating.getItem_ids());
            jsonRating.put("order_id",rating.getOrder_id());
            jsonRating.put("rating", rating.getRating());
            jsonRating.put("comment", rating.getComment());
            jsonRating.put("user_id", rating.getUser_id());
            jsonRating.put("created_at", rating.getCreated_at());
            jsonRating.put("imageBase64", new JSONArray(rating.getImageBase64()));
            commentsArray.put(jsonRating);
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("comments", commentsArray);
        ResponseHandler.sendResponse(exchange,200,responseJson);
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