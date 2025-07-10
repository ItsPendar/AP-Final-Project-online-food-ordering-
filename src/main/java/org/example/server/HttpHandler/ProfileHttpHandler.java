package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;

public class ProfileHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject responseJson;
        if (exchange.getRequestMethod().equals("GET")) {
            responseJson = new JSONObject(new LinkedHashMap<>());

            System.out.println("this will be implemented later");
        }
        else if(exchange.getRequestMethod().equals("PUT")){
            System.out.println("this will be implemented later");
        }
        else{
            sendErrorResponse(exchange, 405, "Method Not Allowed. Only POST requests are allowed.");
        }
    }
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        LoginUserHttpHandler.sendErrorMessage(exchange, statusCode, message);
    }
}
