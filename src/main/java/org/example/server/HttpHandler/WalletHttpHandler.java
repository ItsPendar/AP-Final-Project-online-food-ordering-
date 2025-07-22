package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.TransactionController;
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

    public WalletHttpHandler() throws SQLException {
        transactionController = new TransactionController();
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
            transaction.setUserID(userID);
            transaction.setCreatedAt(LocalDateTime.now());
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);
            if(json.get("amount") == null) {
                ResponseHandler.sendErrorResponse(exchange,400,"Bad request : invalid request body");
                transaction.setStatus("Failed");
                transaction.setAmount(-1);
                //TODO : save the transaction to its table through TransactionDAO
            }
            double amount = json.getDouble("amount");
            //TODO : get user's current balance and add the top up amount to it
            //TODO : update the user after updating the balance using UserDAO
            transaction.setAmount(amount);
            transaction.setStatus("Success");
            transaction.setOrderID(0);
            ResponseHandler.sendResponse(exchange,200,"Wallet charged up successfully");
            //TODO : save the transaction using TransactionDAO
            try {
                transactionController.saveTransaction(transaction);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            //TODO : you should make an order in your order table that is only used for wallet top up
            //and give the ID of that order to this transaction's orderID field

        }
        else {
            ResponseHandler.sendErrorResponse(exchange,404,"Page not found!");
        }
    }
}
