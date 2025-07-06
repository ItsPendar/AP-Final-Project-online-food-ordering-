package org.example.server;

import com.sun.net.httpserver.HttpServer;
import org.example.server.HttpHandler.*;
import org.example.server.dao.DatabaseConnectionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import static spark.Spark.*;

public class Server {

    public static void main(String[] args) throws SQLException, IOException {
        // Initialize handlers
        UserHttpHandler userHttpHandler = new UserHttpHandler();

        //port(8080);]
        get("/", (req, res) -> "Welcome to the User Management API");
        post("/auth/register", userHttpHandler::handleCreateUser);
        post("/auth/login", userHttpHandler::handleLoginUser);
        get("/auth/profile", userHttpHandler::handleGetUser);
        post("/auth/profile", userHttpHandler::handleUpdateUser);
        post("/auth/logout", (req, res) -> "not implemented yet");
        get("/restaurant/mine", (req, res) -> "not implemented yet");
        post("/auth/logout", (req, res) -> "not implemented yet");
    }
}
