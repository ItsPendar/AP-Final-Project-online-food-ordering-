package org.example.server;

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
        server.createContext("/auth/register", new NewUserHttpHandler());
        server.createContext("/auth/login", new LoginUserHttpHandler());
        server.createContext("/auth/profile", new ProfileHttpHandler());
        server.createContext("/restaurants", new RestaurantHttpHandler());
        server.createContext("/vendors", new VendorHttpHandler());
        server.createContext("/wallet", new WalletHttpHandler());
        server.createContext("/payment", new PaymentHttpHandler());
        server.createContext("/orders", new OrderHttpHandler());
        server.createContext("/transactions", new TransactionHttpHandler());
        server.createContext("/deliveries", new DeliHttpHandler());
        server.createContext("/admin", new AdminHttpHandler());
        server.createContext("/ratings", new RatingHttpHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running at: " + server.getAddress());

    }
}
