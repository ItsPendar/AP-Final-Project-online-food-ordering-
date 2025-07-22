package org.example.server.Controller;

import org.example.server.dao.OrderDAO;
import org.example.server.modules.Order;

import java.sql.SQLException;

public class OrderController {
    private static OrderDAO orderDAO = null;
    public OrderController() throws SQLException {
        orderDAO = new OrderDAO();
    }
    public int addOrder(Order order) throws SQLException {
        return orderDAO.addOrder(order);
    }
}
