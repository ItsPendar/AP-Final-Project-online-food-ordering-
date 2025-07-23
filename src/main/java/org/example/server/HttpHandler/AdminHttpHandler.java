package org.example.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.OrderController;
import org.example.server.Controller.UserController;
import org.example.server.dao.DatabaseConnectionManager;
import org.example.server.dao.TransactionDAO;
import org.example.server.dao.UserDAO;
import org.example.server.modules.Transaction;
import org.example.server.modules.User;
import org.example.server.Util.JWTHandler;
import io.jsonwebtoken.Claims;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.server.Controller.UserController;
import org.example.server.dao.OrderDAO;
import org.example.server.modules.Order;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        // بررسی JWT و نقش ADMIN
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
        if (claims == null || !"admin".equals(user.getUserRole())) {
            sendError(exchange, 403, "Access denied.");
            return;
        }

        // پردازش مسیر
        if (method.equalsIgnoreCase("GET") && path.equals("/admin/users")) {
            handleGetAllUsers(exchange);
        } else if (method.equalsIgnoreCase("GET") && path.equals("/admin/orders")) {
            handleGetAllOrders(exchange);
        } else if (method.equalsIgnoreCase("PUT") && pathParts.length == 5 && pathParts[1].equals("admin") && pathParts[2].equals("users") && pathParts[4].equals("status")) {
            try {
                System.out.println(pathParts[3]);
                int userId = Integer.parseInt(pathParts[3]);
                System.out.println(userId);
                handleUpdateUserStatus(exchange, userId);
            }
            catch (NumberFormatException e) {
                sendError(exchange, 400, "Invalid user ID in URL");
            }
        } else if (method.equalsIgnoreCase("GET") && path.equals("/admin/transactions")) {
            handleGetAllTransactions(exchange);
        } else {
            sendError(exchange, 404, "Endpoint not found.");
        }
    }

    private void handleGetAllUsers(HttpExchange exchange) throws IOException {
        try {
            List<User> users = new UserController().getAllUsers();
            JSONArray usersJson = new JSONArray();
            for (User user : users) {
                JSONObject userJson = new JSONObject();
                userJson.put("id", user.getUserID());
                userJson.put("name", user.getName());
                userJson.put("phone", user.getPhoneNumber());
                userJson.put("role", user.getUserRole());
                userJson.put("email", user.getEmail());
                usersJson.put(userJson);
            }
            byte[] response = usersJson.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleGetAllOrders(HttpExchange exchange) throws IOException {
        try {
            List<Order> orders = new OrderController().getAllOrders();
            JSONArray ordersJson = new JSONArray();
            for (Order order : orders) {
                JSONObject orderJson = new JSONObject();
                orderJson.put("customerid", order.getCustomerID());
                orderJson.put("deliveryAddress", order.getDeliveryAddress());
                orderJson.put("vendorid" , order.getVendorID());
                orderJson.put("courierid", order.getCourierID());
                orderJson.put("orderItemIDs", order.getOrderItemIDs());
                orderJson.put("rawPrice", order.getRawPrice());
                orderJson.put("taxFee", order.getTaxFee());
                orderJson.put("courierFee", order.getCourierFee());
                orderJson.put("additionalFee", order.getAdditionalFee());
                orderJson.put("payPrice", order.getPayPrice());
                orderJson.put("status", order.getStatus());
                orderJson.put("createdAt", order.getCreatedAt());
                orderJson.put("updatedAt", order.getUpdatedAt());
            }
            byte[] response = ordersJson.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            sendError(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleUpdateUserStatus(HttpExchange exchange ,int userId) throws IOException {
        System.out.println("Check2");
        try {
        String body = new String(exchange.getRequestBody().readAllBytes());
        JSONObject json = new JSONObject(body);
        boolean approved = json.getBoolean("approved");

        UserController userController = new UserController();
        userController.updateUserApprovalStatus(userId, approved);

        JSONObject response = new JSONObject();
        response.put("message", "User Approval status updated");

        byte[] responseBytes = response.toString().getBytes();
        exchange.sendResponseHeaders(200,responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
        } catch (Exception e) {
            sendError(exchange, 500 , "Server error: " + e.getMessage());
        }
    }

    private void handleGetAllTransactions(HttpExchange exchange) throws IOException {
        try {
            TransactionDAO transactionDAO = new TransactionDAO();
            List<Transaction> transactions = transactionDAO.getAllTransactions();
            ObjectMapper objectMapper = new ObjectMapper();
            String response = objectMapper.writeValueAsString(transactions);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200,response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, e.getMessage());
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
