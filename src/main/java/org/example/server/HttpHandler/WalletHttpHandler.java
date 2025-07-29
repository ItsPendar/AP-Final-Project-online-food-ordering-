package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.TransactionController;
import org.example.server.Controller.UserController;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.ResponseHandler;
import org.example.server.dao.UserDAO;
import org.example.server.modules.Transaction;
import org.example.server.modules.User;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class WalletHttpHandler implements HttpHandler {
    private final TransactionController transactionController;
    private final UserController userController;

    public WalletHttpHandler() throws SQLException {
        transactionController = new TransactionController();
        this.userController = new UserController();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (path.equals("/wallet/top-up") && method.equalsIgnoreCase("POST")) {
            int userID = JWTHandler.getUserIDByToken(exchange);
            User user = UserDAO.getUserByID(userID);
            if(user == null) {
                ResponseHandler.sendErrorResponse(exchange,401,"Unauthorized request");
                return;
            }
            Transaction transaction = new Transaction();
            transaction.setUser_id(userID);
            transaction.setCreated_at(LocalDateTime.now());
            transaction.setMethod("wallet");
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);
            if(json.get("amount") == null) {
                ResponseHandler.sendErrorResponse(exchange,400,"Bad request : invalid request body");
                transaction.setStatus("Failed");
                transaction.setAmount(-1);
                try {
                    transactionController.saveTransaction(transaction);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            double amount = json.getDouble("amount");
            double currentBalance = 0.0;
            try {
                currentBalance = userController.addToWalletBalance(userID,amount);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            transaction.setAmount(amount);
            transaction.setStatus("Success");
            transaction.setOrder_id(0);
            try {
                transactionController.saveTransaction(transaction);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            JSONObject responseBody = new JSONObject();
            responseBody.put("current_balance", currentBalance);
            ResponseHandler.sendResponse(exchange,200,responseBody);

        }//charge the user's walley
        else if(path.equals("/wallet/balance") && method.equalsIgnoreCase("GET")){
            int userID = JWTHandler.getUserIDByToken(exchange);
            User user = UserDAO.getUserByID(userID);
            if(user == null || !user.getUserRole().equals("buyer")) {
                System.out.println("user role is : " + user.getUserRole());
                ResponseHandler.sendErrorResponse(exchange,401,"Unauthorized request");
                return;
            }
            double currentBalance = 0.0;
            try {
                currentBalance = userController.getWalletBalanceByUserID(userID);
                System.out.println("current balance is : " + currentBalance);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            JSONObject responseBody = new JSONObject();
            responseBody.put("current_balance", currentBalance);
            ResponseHandler.sendResponse(exchange,200,responseBody);
        }//get the user's current wallet balance. this returns the wallet balance of the user that has sent the request
        else {
            ResponseHandler.sendErrorResponse(exchange,404,"Page not found!");
        }
    }
}
