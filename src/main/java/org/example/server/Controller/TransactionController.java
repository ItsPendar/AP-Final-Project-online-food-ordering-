package org.example.server.Controller;

import org.example.server.dao.TransactionDAO;
import org.example.server.modules.Transaction;

import java.sql.SQLException;

public class TransactionController {
    private static TransactionDAO transactionDAO = null;
    public TransactionController() throws SQLException {
        transactionDAO = new TransactionDAO();
    }
    public int saveTransaction(Transaction transaction) throws SQLException {
        return transactionDAO.saveTransaction(transaction);
    }
}
