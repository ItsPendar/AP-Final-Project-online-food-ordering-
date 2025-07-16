package org.example.server.Controller;
import org.example.server.modules.User;
import org.example.server.dao.UserDAO;

import java.sql.SQLException;

public class UserController {
    private static UserDAO userDAO;

    public UserController() throws SQLException {
        userDAO = new UserDAO();
    }

    public boolean addUser(User user) throws SQLException {
        if (userDAO.doesUserExistByPhoneNumber(user.getPhoneNumber()) ||
                userDAO.doesUserExistByEmail(user.getEmail())) {
            return false; // User already exists, return false
        } else {
            UserDAO.saveUser(user);
            return true; // User added successfully
        }

    }

    public static User getUserByPhone(String phone) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return UserDAO.getUserByPhone(phone);
    }

    public static User getUserByID(int ID) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return UserDAO.getUserByID(ID);
    }

    public void updateUser(User user, String newPhoneNumber, String oldPhoneNumber) throws SQLException {
        UserDAO userDAO = new UserDAO();
        UserDAO.updateUser(user, newPhoneNumber, oldPhoneNumber);
    }

    public static boolean doesUserExist(String phoneNumber) {
        try {
            // User does not exist
            return userDAO.doesUserExistByPhone(phoneNumber); // User exists
        } catch (SQLException e) {
            throw new RuntimeException("Error checking user existence: " + e.getMessage(), e);
        }
    }

    public static User getUserByPhoneAndPassword(String phoneNumber, String password) {
        try {
            return userDAO.getUserByPhoneAndPassword(phoneNumber, password);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user: " + e.getMessage(), e);
        }
    }

    public static String getUserIDByPhoneNumber(String phoneNumber) {
        return userDAO.getUserIDByPhoneNumber(phoneNumber);
    }

}
