package org.example.server.HttpHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.OrderController;
import org.example.server.Controller.TransactionController;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.QueryHandler;
import org.example.server.Util.ResponseHandler;
import org.example.server.modules.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderHttpHandler implements HttpHandler {
    private final OrderController orderController;
    private final TransactionController transactionController;
    private List<String> itemIDs = new ArrayList<>();

    public OrderHttpHandler() throws SQLException {
        this.orderController = new OrderController();
        this.transactionController = new TransactionController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/orders") && requestMethod.equals("POST")) {
            User user = null;
            try {
                user = JWTHandler.getUserByToken(exchange);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(user == null) {
                ResponseHandler.sendErrorResponse(exchange,401,"Unauthorized request");
                return;
            }
            if(!user.getUserRole().equals("buyer")) {
                ResponseHandler.sendErrorResponse(exchange,403,"forbidden request");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(body);
            Order order = new Order();
            //TODO : fill the order fields
            order.setDeliveryAddress(json.get("delivery_address").asText());
            order.setVendorID(json.get("vendor_id").asInt());
            itemIDs = new ArrayList<>();
            //TODO : fill the array list and pass it to the order object
            JsonNode itemsArray = json.get("items");
            for (JsonNode item : itemsArray) {
                itemIDs.add(item.get("item_id").asText());
            }
            System.out.println("item ids list in Handler class: " + itemIDs);
            order.setOrderItemIDs(itemIDs);
            order.setCustomerID(JWTHandler.getUserIDByToken(exchange));
            order.setCourierFee(0);//this should be a constant number for now
            order.setCourierID(JWTHandler.getUserIDByToken(exchange));
            order.setRawPrice(json.get("raw_price").asDouble());
            order.setTaxFee(json.get("tax_fee").asDouble());
            order.setAdditionalFee(json.get("additional_fee").asDouble());
            order.setPayPrice(json.get("pay_price").asDouble());
            order.setStatus(String.valueOf(Status.WAITING_VENDOR).toLowerCase());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            System.out.println("got here right before saving the order");
            int orderID = -1;
            try {
                orderID = orderController.addOrder(order);
                System.out.println("saved the order. order ID is : " + orderID);
            } catch (SQLException e) {
                // THIS IS THE MOST IMPORTANT CHANGE
                System.err.println("SQL Error while adding order:");
                e.printStackTrace(); // This will print the full error from the database
                ResponseHandler.sendErrorResponse(exchange, 500, "SQL Error: " + e.getMessage());
                return; // Add a return statement here
            }
            if(orderID > 0){
                //TODO : send 200 status code✅
                JSONObject response = new JSONObject();
                response.put("id", orderID);
                response.put("delivery_address", user.getAddress());
                response.put("customer_id", user.getUserID());
                //TODO : the list of item IDs should be put in the response body
                ArrayNode itemIdsArray = mapper.createArrayNode();
                for(String itemID : itemIDs) {
                    itemIdsArray.add(Integer.parseInt(itemID));
                }
                response.put("item_ids",itemIdsArray);
                response.put("vendor_id", json.get("vendor_id").asInt());
                response.put("raw_price",order.getRawPrice());
                response.put("tax_fee",order.getTaxFee());
                response.put("additional_fee",order.getAdditionalFee());
                response.put("pay_price",order.getPayPrice());
                response.put("courier_fee",order.getCourierFee());
                response.put("status", order.getStatus());
                response.put("created_at", order.getCreatedAt().toString());
                response.put("updated_at",order.getUpdatedAt().toString());//the order status hasn't been updated yet
                response.put("courier_id",order.getCourierID());//the order doesn't have a courier yet

                ResponseHandler.sendResponse(exchange,200,response);
                //TODO : update the orderId field in transaction table✅
                try {
                    transactionController.updateOrderIDField(orderID,Integer.parseInt(String.valueOf(json.get("transaction_id"))));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                ResponseHandler.sendErrorResponse(exchange,500,"Internal server error : couldn't submit the order");
            }
        }//save order✅
        else if(path.equals("/orders/history") && requestMethod.equals("GET")) {
            System.out.println("get history request detected");
            try {
                User user = JWTHandler.getUserByToken(exchange);
                if(user == null){
                    ResponseHandler.sendErrorResponse(exchange,401,"Unauthorized request");
                    return;
                }
                if(!user.getUserRole().equals("buyer") && !user.getUserRole().equals("seller") && !user.getUserRole().equals("courier")){
                    ResponseHandler.sendErrorResponse(exchange,403,"Forbidden request");
                    return;
                }
                Map<String, String> queryParams = QueryHandler.getQueryParams(exchange.getRequestURI().getRawQuery());
                String search = queryParams.get("search");
                String vendor = queryParams.get("vendor");
                List<Map<String, Object>> history = new ArrayList<>();
                int vendorID;
                System.out.println("user role in get history : " + user.getUserRole());
                if(user.getUserRole().equals("buyer"))
                    history = orderController.getOrderHistory(JWTHandler.getUserIDByToken(exchange), null, null);
                else if(user.getUserRole().equals("seller")) {
                    vendorID = JWTHandler.getRestaurantIDByOwnerID(exchange);
                    history = orderController.getOrdersByVendorId(vendorID);
                }
                else if(user.getUserRole().equals("courier")) {
                    System.out.println("starting to get courier order history");
                    history = orderController.getOrdersByCourierId(JWTHandler.getUserIDByToken(exchange));
                }
                else
                    System.out.println("user role not identified");

                JSONArray response = new JSONArray();
                for (Map<String, Object> order : history) {
                    response.put(new JSONObject(order));
                }
                System.out.println("response to client : " + response);
                ResponseHandler.sendResponse(exchange,200,response);
            } catch (SQLException e) {
                ResponseHandler.sendResponse(exchange,500,"Internal server error : Couldn't fetch the order history");
                throw new RuntimeException(e);
            }
        }//get history of orders✅
        else if(path.matches("/orders/\\d+") && requestMethod.equals("GET")){
            int id = Integer.parseInt(path.substring("/orders/".length()));
        }//get details of an order
        else{
            ResponseHandler.sendErrorResponse(exchange,404,"Page not found");
        }
    }
}
