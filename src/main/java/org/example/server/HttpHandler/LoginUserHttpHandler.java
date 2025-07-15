package org.example.server.HttpHandler;

import ch.qos.logback.core.subst.Token;
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
import java.util.LinkedHashMap;

public class LoginUserHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //Exactly like what we did in NewUserHttpHandler we need to send the response in JSON format, so we need to have a JSONObject
        JSONObject responseJson = new JSONObject(new LinkedHashMap<>());
        try{
            if (!exchange.getRequestMethod().equals("POST")) {
                sendErrorMessage(exchange, 405, "Method Not Allowed. Only POST requests are allowed.");
            } else {
                JSONObject requestBody = JsonHandler.getObject(exchange.getRequestBody()); //the request body in json format with phone number and pass
                if (!requestBody.has("phone") || !requestBody.has("password")) {
                    sendErrorMessage(exchange, 400, "Missing required fields: phoneNumber or password");
                }
                else {
                    String phoneNumber = requestBody.getString("phone");
                    String password = requestBody.getString("password");
                    // checking the user credentials against the database
                    if (UserController.doesUserExist(phoneNumber)) {
                        if (UserController.getUserByPhoneAndPassword(phoneNumber, password) == null) {
                            sendErrorMessage(exchange, 401, "Incorrect password. Please check your password.");
                        }
                        else {
                            // If user exists, send success response
                            User loggedInUser = UserController.getUserByPhoneAndPassword(phoneNumber, password);
                            JSONObject user = getJsonObject(loggedInUser);
                            responseJson.put("message", "You successfully logged in!");
                            responseJson.put("token", JWTHandler.generateToken(UserController.getUserIDByPhoneNumber(phoneNumber)));
                            responseJson.put("user",user);
                            System.out.println("User ID : " + UserController.getUserIDByPhoneNumber(phoneNumber));
                            byte[] responseBytes = responseJson.toString().getBytes(StandardCharsets.UTF_8);
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, responseBytes.length);
                            exchange.getResponseBody().write(responseBytes);
                        }
                    } else {
                        sendErrorMessage(exchange, 401, "User does not exist. Please check your phone number or try signing up.");
                    }
                }
            }
        }
        catch (Exception e) {
            // Handle any exceptions that occur during processing
            sendErrorMessage(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
        finally {
            exchange.close();
        }
    }

    private static JSONObject getJsonObject(User loggedInUser) {
        JSONObject bankInfo = new JSONObject(new LinkedHashMap<>());
        bankInfo.put("bank_name", loggedInUser.getBankName());
        bankInfo.put("account_number", loggedInUser.getBankAccountNumber());
        JSONObject user = new JSONObject(new LinkedHashMap<>());
        user.put("id", loggedInUser.getUserID());
        user.put("full_name", loggedInUser.getName());
        user.put("phone", loggedInUser.getPhoneNumber());
        user.put("email", loggedInUser.getEmail());
        user.put("role", loggedInUser.getUserRole());
        user.put("address", loggedInUser.getAddress());
        user.put("profileImageBase64", loggedInUser.getProfileImage() != null ? loggedInUser.getProfileImage() : "");
        user.put("bank_info", bankInfo);
        return user;
    }

    public static User getUserByToken(String token) throws SQLException, UnauthorizedException {
        try {
            Claims claims = JWTHandler.verifyToken(token);
            return UserDAO.getUserByID(Integer.parseInt(claims.getSubject()));
        } catch (Exception e) {
            throw new UnauthorizedException("Authentication failed.");
        }
    }

    static void sendErrorMessage(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("error", message);
        byte[] responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }
}
