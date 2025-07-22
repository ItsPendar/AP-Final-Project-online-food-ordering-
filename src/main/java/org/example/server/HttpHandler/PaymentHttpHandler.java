package org.example.server.HttpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.eclipse.jetty.http.HttpParser;
import org.example.server.Controller.TransactionController;
import org.example.server.Util.JWTHandler;
import org.example.server.Util.ResponseHandler;
import org.example.server.modules.Transaction;
import org.example.server.modules.User;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class PaymentHttpHandler implements HttpHandler {
    private final TransactionController transactionController;

    public PaymentHttpHandler() throws SQLException {
        this.transactionController = new TransactionController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (path.equals("/payment/online") && method.equalsIgnoreCase("POST")) {
            User user = null;
            try {
                user = JWTHandler.getUserByToken(exchange);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(user == null){
               ResponseHandler.sendErrorResponse(exchange,401,"Unauthorized request!");
               return;
            }
            if(!user.getUserRole().equals("buyer")) {
                ResponseHandler.sendErrorResponse(exchange,403,"forbidden request");
                return;
            }
            //TODO : get the payment method from the request body✅
            String body = new String(exchange.getRequestBody().readAllBytes());
            JSONObject json = new JSONObject(body);
            String paymentMethod = json.getString("method");
            //TODO : create a transaction object and save it into the table in DB and get transactionID back✅
            Transaction newTransaction = new Transaction();
            newTransaction.setMethod(paymentMethod);
            newTransaction.setOrderID(-1);
            newTransaction.setStatus("success");
            newTransaction.setAmount(json.getDouble("amount"));
            newTransaction.setCreatedAt(LocalDateTime.now());
            newTransaction.setUserID(JWTHandler.getUserIDByToken(exchange));
            /// save the transaction
            int transactionID = -1;
            try {
                transactionID = transactionController.saveTransaction(newTransaction);
                System.out.println("transactionId is : "+ transactionID);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            //TODO : send response back to client with the transactionID in the response body✅
            if(transactionID > 0) {
                JSONObject responseBody = new JSONObject();
                responseBody.put("id",transactionID);
                responseBody.put("order_id", -1);
                responseBody.put("user_id",JWTHandler.getUserIDByToken(exchange));
                responseBody.put("method",paymentMethod);
                responseBody.put("status","success");
                ResponseHandler.sendResponse(exchange,200,responseBody);
            }
            else{
                ResponseHandler.sendErrorResponse(exchange,500,"Internal server error : Failed to save the transaction");
            }
        } else {
            ResponseHandler.sendErrorResponse(exchange,404,"page not found!");
        }
    }
}
