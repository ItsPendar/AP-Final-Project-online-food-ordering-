package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.UserController;
import org.example.server.Util.JWTHandler;
import org.example.server.modules.User;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

public class ProfileHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject responseJson = new JSONObject();
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");



        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring("Bearer ".length());

            Claims claims = JWTHandler.verifyToken(token);

            if (claims == null) {
                sendErrorResponse(exchange, 401, "invalid or expired token");
                return;
            }

            String phone = claims.getSubject();

            User user = UserController.getUserByPhoneAndPassword(phone, phone);


            try {
                UserController userController = new UserController();

                if (user == null) {
                    sendErrorResponse(exchange, 404, "User not found");
                    return;
                }

                JSONObject userJson = new JSONObject();
                userJson.put("name", user.getName());
                userJson.put("phone", user.getPhoneNumber());
                userJson.put("email", user.getEmail());
                userJson.put("role", user.getUserRole());
                userJson.put("address", user.getAddress());
                userJson.put("profile_image", user.getProfileImage());
                userJson.put("bank_info", new JSONObject()
                        .put("bank_name", user.getBankName())
                        .put("account_number", user.getBankAccountNumber()));

                byte[] responseBytes = userJson.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();

            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
        else if (exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);

            String phone = json.optString("phone", null);
            String password = json.optString("password", null);

            if (phone == null || password == null) {
                sendErrorResponse(exchange, 400, "Phone and password are required for authentication.");
                return;
            }

            try {
                UserController userController = new UserController();
                User user = userController.getUserByPhoneAndPassword(phone, password);

                if (user == null) {
                    sendErrorResponse(exchange, 404, "User not found.");
                    return;
                }

                String name = json.optString("full_name", null);
                String address = json.optString("address", null);
                String email = json.optString("email", null);
                String profileImage = json.optString("profile_image", null);

                JSONObject bankInfoJson = json.optJSONObject("bank_info");
                String bankName = null;
                String accountNumber = null;
                if (bankInfoJson != null) {
                    bankName = bankInfoJson.optString("bank_name", null);
                    accountNumber = bankInfoJson.optString("account_number", null);
                }

                if (name != null) user.setName(name);
                if (address != null) user.setAddress(address);
                if (email != null) user.setEmail(email);
                if (profileImage != null) user.setProfileImage(profileImage);
                if (bankName != null) user.setBankName(bankName);
                if (accountNumber != null) user.setBankAccountNumber(accountNumber);

                userController.updateUser(user);

                responseJson.put("message", "User profile updated successfully");

                byte[] responseBytes = responseJson.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();

            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
            }
        }
        else {
            sendErrorResponse(exchange, 405, "Method Not Allowed. Only GET and PUT requests are allowed.");
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

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}