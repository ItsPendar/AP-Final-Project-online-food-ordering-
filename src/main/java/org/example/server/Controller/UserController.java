package org.example.server.Controller;
import org.example.server.modules.User;
import org.example.server.dao.UserDAO;
import java.util.List;
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

    public void setUserApprovalStatus(int userID, boolean isApproved) throws SQLException {
        userDAO.setUserApprovalStatus(userID, isApproved);
    }

    public void deleteUserByID(int userID) throws SQLException {
        userDAO.deleteUserByID(userID);
    }

    public static User getUserByPhone(String phone) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return UserDAO.getUserByPhone(phone);
    }

    public static User getUserByID(int ID) throws SQLException {
        UserDAO userDAO = new UserDAO();
        return UserDAO.getUserByID(ID);
    }

    public double getWalletBalanceByUserID(int userID) throws SQLException {
        return userDAO.getWalletBalanceByUserID(userID);
    }

    public double addToWalletBalance(int userID, double amount) throws SQLException {
        return userDAO.addToWalletBalance(userID,amount);
    }

    public double deductFromWalletBalance(int userID, double amount) throws SQLException {
        return userDAO.deductFromWalletBalance(userID,amount);
    }

    public void updateUser(User user) throws SQLException {
        UserDAO userDAO = new UserDAO();
        UserDAO.updateUser(user);
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

    public static int getUserIDByPhoneNumber(String phoneNumber) {
        return userDAO.getUserIDByPhoneNumber(phoneNumber);
    }

    public static String getUserRoleByID(int userID) throws SQLException {
        return getUserByID(userID).getUserRole();
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }
}
