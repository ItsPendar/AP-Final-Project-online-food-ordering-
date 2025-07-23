package org.example.server.dao;

import org.example.server.dao.DatabaseConnectionManager;
import org.example.server.modules.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    private static final Connection connection;

    static {
        try {
            connection = DatabaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public OrderDAO() throws SQLException {
        this.createOrdersTable(connection);
    }

    public void createOrdersTable(Connection connection) throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS orders (
            order_id SERIAL PRIMARY KEY,
            customer_id INTEGER NOT NULL,
            delivery_address TEXT,
            vendor_id INTEGER NOT NULL,
            courier_id INTEGER,
            rawPrice DOUBLE PRECISION NOT NULL,
            taxFee DOUBLE PRECISION NOT NULL,
            courierFee DOUBLE PRECISION NOT NULL,
            additionalFee DOUBLE PRECISION NOT NULL,
            payPrice DOUBLE PRECISION NOT NULL,
            status VARCHAR(50) NOT NULL,
            order_items VARCHAR(400),
            createdAt TIMESTAMP NOT NULL,
            updatedAt TIMESTAMP,
            CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES users(userid) ON DELETE CASCADE,
            CONSTRAINT fk_vendor FOREIGN KEY (vendor_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE,
            CONSTRAINT fk_courier FOREIGN KEY (courier_id) REFERENCES users(userid) ON DELETE SET NULL
        );
    """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    public int addOrder(Order order) throws SQLException {
        System.out.println("we are inside add order method");
        String sql = """
        INSERT INTO orders (
            customer_id, delivery_address, vendor_id, courier_id,
            rawPrice, taxFee, courierFee, additionalFee, payPrice,
            status, order_items, createdAt, updatedAt
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING order_id;
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, order.getCustomerID());
            stmt.setString(2, order.getDeliveryAddress());
            stmt.setInt(3, order.getVendorID());

            if (order.getCourierID() != -1) {
                stmt.setInt(4, order.getCourierID());
            }
            else
                stmt.setNull(4, java.sql.Types.INTEGER);

            stmt.setDouble(5, order.getRawPrice());
            stmt.setDouble(6, order.getTaxFee());
            stmt.setDouble(7, order.getCourierFee());
            stmt.setDouble(8, order.getAdditionalFee());
            stmt.setDouble(9, order.getPayPrice());
            stmt.setString(10, order.getStatus());
            List<String> itemIDs = order.getOrderItemIDs();
            String itemsString = (itemIDs != null && !itemIDs.isEmpty()) ? String.join(",", itemIDs) : "";
            stmt.setString(11, itemsString);
            stmt.setTimestamp(12, Timestamp.valueOf(order.getCreatedAt()));
            if (order.getUpdatedAt() != null) {
                stmt.setTimestamp(13, Timestamp.valueOf(order.getUpdatedAt()));
            } else {
                stmt.setNull(13, Types.TIMESTAMP);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            else {
                throw new SQLException("Inserting order failed: no ID returned.");
            }
        }
    }
    public List<Map<String, Object>> getOrderHistory(int userID, String search, String vendor) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
        SELECT 
            o.order_id,
            o.delivery_address,
            o.customer_id,
            o.vendor_id,
            o.rawPrice,
            o.taxFee,
            o.courierFee,
            o.additionalFee,
            o.payPrice,
            o.status,
            o.createdAt,
            o.updatedAt,
            o.courier_id,
            o.order_items,
            r.name AS vendor_name
        FROM orders o
        JOIN restaurants r ON o.vendor_id = r.restaurant_id
        WHERE o.customer_id = ?
        """ +
                (vendor != null && !vendor.isEmpty() ? " AND LOWER(r.name) LIKE LOWER(?) " : "");

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;
            stmt.setInt(index++, userID);
            if (vendor != null && !vendor.isEmpty()) {
                stmt.setString(index++, "%" + vendor + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", rs.getInt("order_id"));
                    order.put("delivery_address", rs.getString("delivery_address"));
                    order.put("customer_id", rs.getInt("customer_id"));
                    order.put("vendor_id", rs.getInt("vendor_id"));
                    order.put("raw_price", rs.getDouble("rawPrice"));
                    order.put("tax_fee", rs.getDouble("taxFee"));
                    order.put("courier_fee", rs.getDouble("courierFee"));
                    order.put("additional_fee", rs.getDouble("additionalFee"));
                    order.put("pay_price", rs.getDouble("payPrice"));
                    order.put("status", rs.getString("status"));
                    order.put("created_at", rs.getTimestamp("createdAt").toString());
                    order.put("updated_at", rs.getTimestamp("updatedAt") != null ?
                            rs.getTimestamp("updatedAt").toString() : null);
                    order.put("courier_id", rs.getObject("courier_id") != null ? rs.getInt("courier_id") : null);

                    // Parse order_items
                    String itemString = rs.getString("order_items");
                    List<String> itemIDs = new ArrayList<>();
                    if (itemString != null && !itemString.isEmpty()) {
                        for (String id : itemString.split(",")) {
                            itemIDs.add(id.trim());
                        }
                    }

                    // Optional: filter by `search` (item name) using Java-side lookup
                    if (search != null && !search.isEmpty()) {
                        boolean matchFound = false;
                        for (String id : itemIDs) {
                            String itemName = getItemNameById(id); // implement this
                            if (itemName != null && itemName.toLowerCase().contains(search.toLowerCase())) {
                                matchFound = true;
                                break;
                            }
                        }
                        if (!matchFound) continue;
                    }

                    order.put("item_ids", itemIDs);
                    result.add(order);
                }
            }
        }
        return result;
    }
    public String getItemNameById(String itemId) throws SQLException {
        String sql = "SELECT name FROM items WHERE item_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }

    public List<Map<String, Object>> getOrdersByStatus(String status) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
        SELECT 
            o.order_id,
            o.delivery_address,
            o.customer_id,
            o.vendor_id,
            o.rawPrice,
            o.taxFee,
            o.courierFee,
            o.additionalFee,
            o.payPrice,
            o.status,
            o.createdAt,
            o.updatedAt,
            o.courier_id,
            o.order_items
        FROM orders o
        JOIN restaurants r ON o.vendor_id = r.restaurant_id
        WHERE o.status = ?
    """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            System.out.println("status in DAO : " + status);
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("got in to the while loop");
                    Map<String, Object> order = new HashMap<>();
                    System.out.println("id : " + rs.getInt("order_id"));
                    order.put("id", rs.getInt("order_id"));
                    System.out.println("address: " + rs.getString("delivery_address"));
                    order.put("delivery_address", rs.getString("delivery_address"));
                    System.out.println("customer id  : " + rs.getInt("customer_id"));

                    order.put("customer_id", rs.getInt("customer_id"));
                    System.out.println("vendor id : " + rs.getInt("vendor_id"));

                    order.put("vendor_id", rs.getInt("vendor_id"));
                    System.out.println("raw price : " + rs.getDouble("rawPrice"));

                    order.put("raw_price", rs.getDouble("rawPrice"));
                    System.out.println("tax fee  : " + rs.getDouble("taxFee"));

                    order.put("tax_fee", rs.getDouble("taxFee"));
                    System.out.println("courier fee : " + rs.getDouble("courierFee"));

                    order.put("courier_fee", rs.getDouble("courierFee"));
                    System.out.println("additional fee : " + rs.getDouble("additionalFee"));

                    order.put("additional_fee", rs.getDouble("additionalFee"));
                    System.out.println("pay price : " + rs.getDouble("payPrice"));

                    order.put("pay_price", rs.getDouble("payPrice"));
                    System.out.println("status : " + rs.getString("status"));

                    order.put("status", rs.getString("status"));
                    System.out.println("created at : " +  rs.getTimestamp("createdAt").toString());

                    order.put("created_at", rs.getTimestamp("createdAt").toString());
                    System.out.println("updated at : " +  rs.getTimestamp("updatedAt"));

                    order.put("updated_at", rs.getTimestamp("updatedAt") != null ?
                            rs.getTimestamp("updatedAt").toString() : null);
                    System.out.println("courier id  : " + rs.getInt("courier_id"));

                    order.put("courier_id", rs.getObject("courier_id") != null ? rs.getInt("courier_id") : null);
                    System.out.println("order_items : " + rs.getString("order_items"));

                    String itemString = rs.getString("order_items");
                    List<String> itemIDs = new ArrayList<>();
                    if (itemString != null && !itemString.isEmpty()) {
                        for (String id : itemString.split(",")) {
                            itemIDs.add(id.trim());
                        }
                    }
                    order.put("item_ids", itemIDs);
                    result.add(order);
                }
            }
        }
        return result;
    }

    public List<Map<String, Object>> getOrdersByVendorId(int vendorId) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
        SELECT 
            o.order_id,
            o.delivery_address,
            o.customer_id,
            o.vendor_id,
            o.rawPrice,
            o.taxFee,
            o.courierFee,
            o.additionalFee,
            o.payPrice,
            o.status,
            o.createdAt,
            o.updatedAt,
            o.courier_id,
            o.order_items
        FROM orders o
        JOIN restaurants r ON o.vendor_id = r.restaurant_id
        WHERE o.vendor_id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vendorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", rs.getInt("order_id"));
                    order.put("delivery_address", rs.getString("delivery_address"));
                    order.put("customer_id", rs.getInt("customer_id"));
                    order.put("vendor_id", rs.getInt("vendor_id"));
                    order.put("raw_price", rs.getDouble("rawPrice"));
                    order.put("tax_fee", rs.getDouble("taxFee"));
                    order.put("courier_fee", rs.getDouble("courierFee"));
                    order.put("additional_fee", rs.getDouble("additionalFee"));
                    order.put("pay_price", rs.getDouble("payPrice"));
                    order.put("status", rs.getString("status"));
                    order.put("created_at", rs.getTimestamp("createdAt").toString());
                    order.put("updated_at", rs.getTimestamp("updatedAt") != null ?
                            rs.getTimestamp("updatedAt").toString() : null);
                    order.put("courier_id", rs.getObject("courier_id") != null ? rs.getInt("courier_id") : null);

                    String itemString = rs.getString("order_items");
                    List<String> itemIDs = new ArrayList<>();
                    if (itemString != null && !itemString.isEmpty()) {
                        for (String id : itemString.split(",")) {
                            itemIDs.add(id.trim());
                        }
                    }
                    order.put("item_ids", itemIDs);
                    result.add(order);
                }
            }
        }
        return result;
    }

    public boolean updateOrderStatus(int orderId, String newStatus, int courierID) throws SQLException {
        String sql = """
        UPDATE orders
        SET status = ?,courier_id = ?, updatedAt = CURRENT_TIMESTAMP
        WHERE order_id = ?
    """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, courierID);
            stmt.setInt(3, orderId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // return true if update was successful
        }
    }

    public List<Map<String, Object>> getOrdersByCourierId(int courierId) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = """
        SELECT 
            o.order_id,
            o.delivery_address,
            o.customer_id,
            o.vendor_id,
            o.rawPrice,
            o.taxFee,
            o.courierFee,
            o.additionalFee,
            o.payPrice,
            o.status,
            o.createdAt,
            o.updatedAt,
            o.courier_id,
            o.order_items
        FROM orders o
        JOIN restaurants r ON o.vendor_id = r.restaurant_id
        WHERE o.courier_id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            System.out.println("courier id in DAO : " + courierId);
            stmt.setInt(1, courierId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", rs.getInt("order_id"));
                    order.put("delivery_address", rs.getString("delivery_address"));
                    order.put("customer_id", rs.getInt("customer_id"));
                    order.put("vendor_id", rs.getInt("vendor_id"));
                    order.put("raw_price", rs.getDouble("rawPrice"));
                    order.put("tax_fee", rs.getDouble("taxFee"));
                    order.put("courier_fee", rs.getDouble("courierFee"));
                    order.put("additional_fee", rs.getDouble("additionalFee"));
                    order.put("pay_price", rs.getDouble("payPrice"));
                    order.put("status", rs.getString("status"));
                    order.put("created_at", rs.getTimestamp("createdAt").toString());
                    order.put("updated_at", rs.getTimestamp("updatedAt") != null ?
                            rs.getTimestamp("updatedAt").toString() : null);
                    order.put("courier_id", rs.getInt("courier_id"));

                    String itemString = rs.getString("order_items");
                    List<String> itemIDs = new ArrayList<>();
                    if (itemString != null && !itemString.isEmpty()) {
                        for (String id : itemString.split(",")) {
                            itemIDs.add(id.trim());
                        }
                    }
                    order.put("item_ids", itemIDs);

                    result.add(order);
                }
            }
        }

        return result;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders";

        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setCustomerID(rs.getInt("customer_id"));
            order.setDeliveryAddress(rs.getString("delivery_address"));
            order.setVendorID(rs.getInt("vendor_id"));
            order.setCourierID(rs.getInt("courier_id"));
            order.setRawPrice(rs.getDouble("rawprice"));
            order.setTaxFee(rs.getDouble("taxfee"));
            order.setCourierFee(rs.getDouble("courierfee"));
            order.setAdditionalFee(rs.getDouble("additionalfee"));
            order.setPayPrice(rs.getDouble("payprice"));
            order.setStatus(rs.getString("status"));
            order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            String itemString = rs.getString("order_items");
            List<String> itemIDs = new ArrayList<>();
            if (itemString != null && !itemString.isEmpty()) {
                for (String id : itemString.split(",")) {
                    itemIDs.add(id.trim());
                }
            }
            order.setOrderItemIDs(itemIDs);
            orders.add(order);
        }
        return orders;
    }
}