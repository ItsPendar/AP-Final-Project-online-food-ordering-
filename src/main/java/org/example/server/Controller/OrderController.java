package org.example.server.Controller;

import org.example.server.dao.OrderDAO;
import org.example.server.modules.Order;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderController {
    private static OrderDAO orderDAO = null;
    public OrderController() throws SQLException {
        orderDAO = new OrderDAO();
    }
    public int addOrder(Order order) throws SQLException {
        return orderDAO.addOrder(order);
    }
    public List<Map<String, Object>> getOrderHistory(int userID, String search, String vendor) throws SQLException {
        return orderDAO.getOrderHistory(userID,search,vendor);
    }
    public List<Map<String, Object>> getOrdersByStatus(String status) throws SQLException {
        return orderDAO.getOrdersByStatus(status);
    }
    public List<Map<String, Object>> getOrdersByVendorId(int vendorId) throws SQLException {
        return orderDAO.getOrdersByVendorId(vendorId);
    }
    public boolean updateOrderStatus(int orderId, String newStatus, int courierID) throws SQLException {
        return orderDAO.updateOrderStatus(orderId,newStatus,courierID);
    }
    public List<Map<String, Object>> getOrdersByCourierId(int courierId) throws SQLException {
        return orderDAO.getOrdersByCourierId(courierId);
    }

    public List<Map<String, Object>> getAllOrdersAsMapList() throws SQLException {
        return orderDAO.getAllOrdersAsMapList();
    }
 }
