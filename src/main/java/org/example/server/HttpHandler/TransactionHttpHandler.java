package org.example.server.HttpHandler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.server.Controller.TransactionController;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.ResponseHandler;
import org.example.server.modules.Transaction;
import org.example.server.modules.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionHttpHandler implements HttpHandler {
    private final TransactionController transactionController;
    public TransactionHttpHandler() throws SQLException {
        transactionController = new TransactionController();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        if (path.equals("/transactions") && requestMethod.equals("GET")) {
            User user = null;
            try {
                user = JWTHandler.getUserByToken(exchange);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(user == null){
                ResponseHandler.sendErrorResponse(exchange,401,"Unauthorized request");
                return;
            }
            if(!user.getUserRole().equals("buyer")){
                ResponseHandler.sendErrorResponse(exchange,403,"Forbidden request");
                return;
            }
            JSONArray response = new JSONArray();
            System.out.println("got here in transaction handler");
            try {
                List<Map<String, Object>> history = transactionController.getTransactionHistoryAsMapList(JWTHandler.getUserIDByToken(exchange));
                System.out.println("transaction history was fetched from DB");
                for (Map<String, Object> order : history) {
                    response.put(new JSONObject(order));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            ResponseHandler.sendResponse(exchange,200,response);
        }//get the transaction history of the user✅
        else {
            ResponseHandler.sendErrorResponse(exchange,404,"Page was not found!");
        }
    }
}
