package org.example.server.Controller;
import org.example.server.modules.User;
import org.example.server.dao.UserDAO;

import java.sql.SQLException;

public class UserController {
    private UserDAO userDAO;
    public UserController() throws SQLException {
        this.userDAO = new UserDAO();
    }
    public boolean createUser(String name, String phoneNumber, String email, String password, String userRole, String address, String profileImage, String bankName, String bankAccountNumber) {
        try {
            if(userDAO.doesUserExistByPhoneNumber(phoneNumber)) {
                //throw new RuntimeException("User with this phone number already exists.");
                return false; // User already exists, return false
            }
            User newUser = new User(name, phoneNumber, email, password, userRole, address, profileImage, bankName, bankAccountNumber);
            userDAO.saveUser(newUser); // Save the new user
            return true; // User created successfully
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    public boolean doesUserExist(String phoneNumber, String password) {
        try {
            User user = userDAO.getUserByPhoneAndPassword(phoneNumber, password);
            return user != null; // Returns true if user exists, false otherwise
        } catch (SQLException e) {
            throw new RuntimeException("Error checking user existence: " + e.getMessage(), e);
        }
    }
    public User getUserByPhoneAndPassword(String phoneNumber, String password) {
        try {
            return userDAO.getUserByPhoneAndPassword(phoneNumber, password);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user: " + e.getMessage(), e);
        }
    }
}
