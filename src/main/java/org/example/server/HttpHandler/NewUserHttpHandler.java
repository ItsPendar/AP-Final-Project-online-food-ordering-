package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import org.example.server.Controller.UserController;
import org.example.server.Util.Exceptions.UnauthorizedException;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.JsonHandler;
import org.example.server.dao.UserDAO;
import org.example.server.modules.User;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class NewUserHttpHandler implements HttpHandler {
    private final UserController userController;

    public NewUserHttpHandler() throws SQLException {
        userController = new UserController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //String response;
        JSONObject jsonResponse;
        String token;
        byte[] responseBytes;
        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "POST":
                    // Adding the user by passing the data one layer up to Controller layer
                    //JSONObject jsonObject = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
                    JSONObject jsonObject = JsonHandler.getObject(exchange.getRequestBody());
                    System.out.println(jsonObject);
                    // Validate required fields
                    if (jsonObject.getString("email").isEmpty() || jsonObject.getString("password").isEmpty() || jsonObject.isNull("email") || jsonObject.isNull("password")) {
                        sendErrorResponse(exchange, 400, "Missing required fields: email or password");
                        break;
                    }

                    User newUser = new User(jsonObject);
                    if (userController.addUser(newUser)) {
                        // If the user is added successfully, send a 200 OK response
                        jsonResponse = new JSONObject();
                        String userID = userController.getUserIDByPhoneNumber(newUser.getPhoneNumber());
                        String PhoneNumber = jsonResponse.getString("phone");
                        if(userID != null && !userID.isEmpty())
                            newUser.setUserID(userID);
                        else {
                            newUser.setUserID("0");
                            exchange.sendResponseHeaders(200, 0);
                        }
                        jsonResponse.put("message", "User registered successfully");
                        jsonResponse.put("userID", newUser.getUserID());
                        jsonResponse.put("token", JWTHandler.generateToken(PhoneNumber));//JWT token generation should be implemented later
                        responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                        break;
                    } else {
                        sendErrorResponse(exchange, 409, "A user with this phone number or email already exists.");
                    }
                    break;
                case "GET":
                    // Handle user retrieval logic here
                    exchange.sendResponseHeaders(200, 0);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
        catch (Exception e) {
            // If there is an error in adding the user, send a 500 internal server error response
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        LoginUserHttpHandler.sendErrorMessage(exchange, statusCode, message);
    }

}
