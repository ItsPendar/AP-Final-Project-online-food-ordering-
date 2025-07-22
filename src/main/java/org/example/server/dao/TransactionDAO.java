package org.example.server.dao;

import org.example.server.modules.Transaction;

import java.sql.*;

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
            createdAt TIMESTAMP NOT NULL,
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
        INSERT INTO transactions (order_id, user_id, tr_method, status, createdAt, amount)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING transaction_id;
    """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transaction.getOrderID());
            preparedStatement.setInt(2, transaction.getUserID());
            preparedStatement.setString(3, transaction.getMethod());
            preparedStatement.setString(4, transaction.getStatus());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(transaction.getCreatedAt()));
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
}
