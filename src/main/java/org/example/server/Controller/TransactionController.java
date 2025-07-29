package org.example.server.Controller;

import org.example.server.dao.TransactionDAO;
import org.example.server.modules.Transaction;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TransactionController {
    private static TransactionDAO transactionDAO = null;
    public TransactionController() throws SQLException {
        transactionDAO = new TransactionDAO();
    }
    public int saveTransaction(Transaction transaction) throws SQLException {
        return transactionDAO.saveTransaction(transaction);
    }
    public void updateOrderIDField(int orderID, int transactionID) throws SQLException {
        transactionDAO.updateOrderIDField(orderID,transactionID);
    }
    public List<Map<String, Object>> getTransactionHistoryAsMapList(int userID) throws SQLException {
        return transactionDAO.getTransactionHistoryAsMapList(userID);
    }

    public List<Map<String, Object>> getAllTransactionsAsMapList() throws SQLException {
        return transactionDAO.getAllTransactionsAsMapList();
    }

    public List<Map<String, Object>> getTransactionsByUserMethodBeforeDate(int userID, String paymentMethod, LocalDateTime beforeDate) throws SQLException {
        return transactionDAO.getTransactionsByUserMethodBeforeDate(userID,paymentMethod,beforeDate);
    }
}
