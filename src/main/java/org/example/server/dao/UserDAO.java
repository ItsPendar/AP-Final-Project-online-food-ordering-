package org.example.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.server.modules.User;

public class UserDAO {
    private static Connection connection = null;
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

    public static void saveUser(User user) throws SQLException {
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
            return getUser(resultSet, user);
        }
        return null; // User not found
    }

    public static String getUserRoleByUserID(String userID) {
        try {
            String query = "SELECT user_role FROM users WHERE userID = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userID));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("user_role");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static User getUserByPhone(String phoneNumber) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE phone_number = ?");
        preparedStatement.setString(1,phoneNumber);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            User user = new User();
            return getUser(resultSet, user);
        }
        return null;
    }

    public static User getUserByID(int userID) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM users WHERE userID = ?"
            );
            preparedStatement.setInt(1, userID);

            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserID(resultSet.getString("userID"));
                return getUser(resultSet, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // User not found
    }

    private static User getUser(ResultSet resultSet, User user) throws SQLException {
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

    public boolean doesUserExistByEmail(String email) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE email = ?"
            );
            preparedStatement.setString(1, email);

            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Returns true if count > 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // User does not exist
    }

    public String getUserIDByPhoneNumber(String phoneNumber) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT userID FROM users WHERE phone_number = ?"
            );
            preparedStatement.setString(1, phoneNumber);

            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("userID"); // Return the userID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // User not found
    }

    public boolean doesUserExistByPhone(String phoneNumber) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE phone_number = ?"
        );
        preparedStatement.setString(1, phoneNumber);

        var resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1) > 0; // Returns true if count > 0
        }
        return false; // User does not exist
    }

    public static void updateUser(User user,String newPhoneNumber, String oldPhoneNumber) throws SQLException {
//        String sql = "UPDATE users SET name = ?,phone_number = ?, email = ?, address = ?, profileimage = ?, bank_name = ?, bank_account_number = ? WHERE phone_number = ?";
//        PreparedStatement preparedStatement =
//                connection.prepareStatement(sql);
//        preparedStatement.setString(1, user.getName());
//        preparedStatement.setString(2,newPhoneNumber);
//        preparedStatement.setString(3,user.getEmail());
//        preparedStatement.setString(4,user.getAddress());
//        preparedStatement.setString(5,user.getProfileImage());
//        preparedStatement.setString(6,user.getBankName());
//        preparedStatement.setString(7,user.getBankAccountNumber());
//        preparedStatement.setString(8,oldPhoneNumber);
//        preparedStatement.executeUpdate();
        User newUser = getUserByPhone(oldPhoneNumber);
        newUser.setPhoneNumber(newPhoneNumber);
        String deleteSql = "DELETE FROM users WHERE phone_number = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setString(1, oldPhoneNumber.trim());
            int deleted = deleteStmt.executeUpdate();
            System.out.println("Deleted rows: " + deleted);
        }

        saveUser(newUser);
//        String insertSql = "INSERT INTO users (name, phone_number, email, address, profileimage, bank_name, bank_account_number, user_role, password) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
//            insertStmt.setString(1, newUser.getName());
//            insertStmt.setString(2, newPhoneNumber.trim());
//            insertStmt.setString(3, newUser.getEmail());
//            insertStmt.setString(4, newUser.getAddress());
//            insertStmt.setString(5, newUser.getProfileImage());
//            insertStmt.setString(6, newUser.getBankName());
//            insertStmt.setString(7, newUser.getBankAccountNumber());
//            insertStmt.setString(8, user.getUserRole());
//            insertStmt.setString(9, user.getPassword()); // Or some default if not stored in User
//
//            int inserted = insertStmt.executeUpdate();
//            System.out.println("Inserted rows: " + inserted);
//        }
    }
}
