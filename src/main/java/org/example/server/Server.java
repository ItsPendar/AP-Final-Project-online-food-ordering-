package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.server.HttpHandler.*;
import org.example.server.dao.DatabaseConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class Server {

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws SQLException, IOException {

        DatabaseConnectionManager.getConnection();
        // Set up the HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost",8080), 0);
        server.createContext("/hello", new MyHandler());
        server.createContext("/auth/register", new NewUserHttpHandler());
        server.createContext("/auth/login", new LoginUserHttpHandler());
        server.createContext("/auth/profile", new ProfileHttpHandler());
        server.createContext("/restaurant/list", new RestaurantHttpHandler());
        server.createContext("/restaurant/create", new RestaurantHttpHandler()::handleCreateRestaurant);
        server.setExecutor(null);
        server.start();

        System.out.println("Server is running at: " + server.getAddress());
    }
    //a Handler class just for testing
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello, World!";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}
