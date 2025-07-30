package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.eclipse.jetty.http.HttpParser;
import org.example.server.Controller.RestaurantController;
import org.example.server.Controller.UserController;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.ResponseHandler;
import org.example.server.dao.OrderDAO;
import org.example.server.modules.Status;
import org.example.server.modules.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeliHttpHandler implements HttpHandler {
    private final OrderDAO orderDAO;
    public DeliHttpHandler() throws SQLException {
        orderDAO = new OrderDAO();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("now inside handle");
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String override = exchange.getRequestHeaders().getFirst("X-HTTP-Method-Override");
        if (override != null) {
            method = override.toUpperCase();
        }
        System.out.println("request method is : " + method);
        if (path.matches("/deliveries/\\d+") && method.equalsIgnoreCase("PATCH")) {
            System.out.println("change status request detected");
            int id = extractId(path, "/deliveries/");
            System.out.println("orderID in change order status: " + id);
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
            if(!user.getUserRole().equals("seller") && !user.getUserRole().equals("courier")){
                ResponseHandler.sendErrorResponse(exchange,403,"Forbidden request");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);
            String status = json.getString("status");
            System.out.println("status in change status is : " + status);
            int courierID = -1;
            if(user.getUserRole().equals("courier")) {
                courierID = JWTHandler.getUserIDByToken(exchange);
            }
            else
                courierID = 1;
            try {
                if(orderDAO.updateOrderStatus(id,status,courierID)){
                    ResponseHandler.sendResponse(exchange,200,"Updated the order status successfully");
                }
                else{
                    ResponseHandler.sendErrorResponse(exchange,500,"Internal server error : Failed to update the order status");
                }
            } catch (SQLException e) {
                ResponseHandler.sendErrorResponse(exchange,500,"Internal server error");
                throw new RuntimeException(e);
            }
        }//change status of an order
        else if(path.matches("/deliveries/available") && method.equalsIgnoreCase("GET")){
            System.out.println("get available deliveries request detected");
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
            if(!user.getUserRole().equals("courier")){
                ResponseHandler.sendErrorResponse(exchange,403,"Forbidden request");
                return;
            }
            List<Map<String, Object>> ordersListSubmitted;
            List<Map<String, Object>> ordersListOnTheWay;
            try {
                ordersListSubmitted = orderDAO.getOrdersByStatus(Status.SUBMITTED.toString().toLowerCase());
                ordersListOnTheWay = orderDAO.getOrdersByStatus(Status.ON_THE_WAY.toString().toLowerCase());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            JSONArray response = new JSONArray();
            for (Map<String, Object> order : ordersListSubmitted) {
                order.put("vendor_name", RestaurantController.getRestaurantByID(Integer.parseInt(order.get("vendor_id").toString())).getName());
                User customer;
                try {
                    customer = UserController.getUserByID(Integer.parseInt(order.get("customer_id").toString()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                order.put("customer_name", customer.getName());
                order.put("contact",  customer.getPhoneNumber());
                response.put(new JSONObject(order));
            }
            for (Map<String, Object> order : ordersListOnTheWay) {
                order.put("vendor_name", RestaurantController.getRestaurantByID(Integer.parseInt(order.get("vendor_id").toString())).getName());
                User customer;
                try {
                    customer = UserController.getUserByID(Integer.parseInt(order.get("customer_id").toString()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                order.put("customer_name", customer.getName());
                order.put("contact",  customer.getPhoneNumber());
                response.put(new JSONObject(order));
            }
            ResponseHandler.sendResponse(exchange,200,response);
        }
        else {
            ResponseHandler.sendErrorResponse(exchange,404,"Page not found!");
        }
    }
    private int extractId(String path, String prefix) {
        try {
            String trimmed = path.substring(prefix.length());
            return Integer.parseInt(trimmed.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't extract orderID from request path");
        }
    }
}
