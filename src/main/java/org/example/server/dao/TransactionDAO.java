package org.example.server.dao;

import org.example.server.modules.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {
    private static final Connection connection;
    static {
        try {
            connection = DatabaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public TransactionDAO() throws SQLException {
        this.createTransactionTable();
    }
    public void createTransactionTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS transactions (
            transaction_id SERIAL PRIMARY KEY,
            order_id INTEGER,
            user_id INTEGER NOT NULL,
            tr_method VARCHAR(50) NOT NULL,
            status VARCHAR(50) NOT NULL,
            created_at TIMESTAMP NOT NULL,
            amount DOUBLE PRECISION NOT NULL,
            CONSTRAINT fk_user
                FOREIGN KEY (user_id)
                REFERENCES users(userid)
                ON DELETE CASCADE
        );
    """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    public int saveTransaction(Transaction transaction) throws SQLException {
        String sql = """
        INSERT INTO transactions (order_id, user_id, tr_method, status, created_at, amount)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING transaction_id;
    """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transaction.getOrder_id());
            preparedStatement.setInt(2, transaction.getUser_id());
            preparedStatement.setString(3, transaction.getMethod());
            preparedStatement.setString(4, transaction.getStatus());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(transaction.getCreated_at()));
            preparedStatement.setDouble(6, transaction.getAmount());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt("transaction_id");
            } else {
                throw new SQLException("Transaction insertion failed, no ID returned.");
            }
        }
    }
    public void updateOrderIDField(int orderID, int transactionID) throws SQLException {
        String sql = "UPDATE transactions SET order_id = ? WHERE transaction_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, orderID);
            preparedStatement.setInt(2, transactionID);
            preparedStatement.executeUpdate();
        }
    }
    public List<Map<String, Object>> getTransactionHistoryAsMapList(int userID) throws SQLException {
        System.out.println("inside get transaction history method in DAO");
        String sql = """
        SELECT transaction_id, order_id, user_id, tr_method, status, created_at, amount
        FROM transactions
        WHERE user_id = ?
        ORDER BY created_at DESC;
    """;

        List<Map<String, Object>> result = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userID);
            System.out.println("userID : " + userID);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                System.out.println("in while loop");
                Map<String, Object> transactionMap = new HashMap<>();
                transactionMap.put("id", rs.getInt("transaction_id"));
                System.out.println("transactionID : " + rs.getInt("transaction_id"));
                transactionMap.put("order_id", rs.getInt("order_id"));
                System.out.println("orderID : " + rs.getInt("order_id"));
                transactionMap.put("user_id", rs.getInt("user_id"));
                System.out.println("useRID : " + rs.getInt("user_id"));
                transactionMap.put("method", rs.getString("tr_method"));
                System.out.println("method : " +rs.getString("tr_method") );
                transactionMap.put("status", rs.getString("status"));
                System.out.println("status : " + rs.getString("status"));
                transactionMap.put("created_at", rs.getTimestamp("created_at").toString());
                System.out.println("created at : " + rs.getTimestamp("created_at").toString());
                transactionMap.put("amount", rs.getDouble("amount"));
                System.out.println("amount : " + rs.getDouble("amount"));

                result.add(transactionMap);
                System.out.println("added rs");
            }
        }

        return result;
    }

}

