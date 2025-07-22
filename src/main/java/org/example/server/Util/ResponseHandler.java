package org.example.server.Util;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import java.io.IOException;

public class ResponseHandler {
    public static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", message);
        byte[] response = errorJson.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
    public static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject messageJson = new JSONObject();
        messageJson.put("message", message);
        byte[] response = messageJson.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
    public static void sendResponse(HttpExchange exchange, int statusCode, JSONObject responseBody) throws IOException {
        byte[] response = responseBody.toString().getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
}
