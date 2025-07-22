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
}
