package org.example.server.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionManager
{
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "7584";

    private static Connection connection;


    private DatabaseConnectionManager() {
        // Private constructor to prevent instantiation
    }
     public static Connection getConnection() throws SQLException {
        if(connection == null || connection.isClosed()) {
            try {
                connection = java.sql.DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            } catch (java.sql.SQLException e) {
                throw new RuntimeException("Failed to connect to the database", e);
            }
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}
