package org.example.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.example.server.modules.User;

public class UserDAO {
    private final Connection connection;
    public UserDAO() throws SQLException {
        connection = DatabaseConnectionManager.getConnection();
        createUserTable();

    }

    public boolean doesUserExistByPhoneNumber(String phoneNumber) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE phone_number = ?"
        );
        preparedStatement.setString(1, phoneNumber);

        var resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1) > 0; // Returns true if count > 0
        }
        return false;
    }

    public void createUserTable() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS users (" +
            "userID SERIAL PRIMARY KEY, " +
            "name VARCHAR(255) NOT NULL, " +
            "phone_number VARCHAR(20) NOT NULL, " +
            "email VARCHAR(255) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "user_role VARCHAR(50) NOT NULL, " +
            "address TEXT, " +
            "profileImage TEXT, " +
            "bank_name VARCHAR(100), " +
            "bank_account_number VARCHAR(50)" +
            ")"
        );
        preparedStatement.executeUpdate();
    }

    public void saveUser(User user) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
            "INSERT INTO users (name, phone_number, email, password, user_role, address, profileImage, bank_name, bank_account_number) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getPhoneNumber());
        preparedStatement.setString(3, user.getEmail());
        preparedStatement.setString(4, user.getPassword());
        preparedStatement.setString(5, user.getUserRole());
        preparedStatement.setString(6, user.getAddress());
        preparedStatement.setString(7, user.getProfileImage());
        preparedStatement.setString(8, user.getBankName());
        preparedStatement.setString(9, user.getBankAccountNumber());

        preparedStatement.executeUpdate();
    }

    public User getUserByPhoneAndPassword(String phoneNumber, String password) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "SELECT * FROM users WHERE phone_number = ? AND password = ?"
        );
        preparedStatement.setString(1, phoneNumber);
        preparedStatement.setString(2, password);

        var resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            User user = new User();
            user.setName(resultSet.getString("name"));
            user.setPhoneNumber(resultSet.getString("phone_number"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            user.setUserRole(resultSet.getString("user_role"));
            user.setAddress(resultSet.getString("address"));
            user.setProfileImage(resultSet.getString("profileImage"));
            user.setBankName(resultSet.getString("bank_name"));
            user.setBankAccountNumber(resultSet.getString("bank_account_number"));
            return user;
        }
        return null; // User not found
    }
}
