package org.example.server.dao;

import org.example.server.dao.DatabaseConnectionManager;
import org.example.server.modules.Order;

import java.sql.*;

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
            order_items VARCHAR(50),
            createdAt TIMESTAMP NOT NULL,
            updatedAt TIMESTAMP,
            CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES users(userid) ON DELETE CASCADE,
            CONSTRAINT fk_vendor FOREIGN KEY (vendor_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
        );
    """;
//            CONSTRAINT fk_coupon FOREIGN KEY (couponID) REFERENCES coupons(couponID) ON DELETE SET NULL
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
//            CONSTRAINT fk_courier FOREIGN KEY (courier_id) REFERENCES users(userid) ON DELETE SET NULL
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
            System.out.println("customer id : " + order.getCustomerID());
            stmt.setInt(1, order.getCustomerID());
            System.out.println("delivery address : "+ order.getDeliveryAddress());
            stmt.setString(2, order.getDeliveryAddress());
            System.out.println("vendor id : " + order.getVendorID());
            stmt.setInt(3, order.getVendorID());

            if (order.getCourierID() != -1) {
                System.out.println("courier id : " + order.getCourierID());
                stmt.setInt(4, order.getCourierID());
            }
            else
                stmt.setNull(4, java.sql.Types.INTEGER);

            System.out.println("raw price : " + order.getRawPrice());
            stmt.setDouble(5, order.getRawPrice());
            System.out.println("tax fee : " + order.getTaxFee());
            stmt.setDouble(6, order.getTaxFee());
            System.out.println("courier fee : " + order.getCourierFee());
            stmt.setDouble(7, order.getCourierFee());
            System.out.println("additional fee : "+ order.getAdditionalFee());
            stmt.setDouble(8, order.getAdditionalFee());
            System.out.println("pay price : " + order.getPayPrice());
            stmt.setDouble(9, order.getPayPrice());
            System.out.println("status : " + order.getStatus());
            stmt.setString(10, order.getStatus());
            System.out.println("status has been set");
//            String[] itemIDsArray = order.getOrderItemIDs().toArray(new String[0]);
//            Array itemsArray = connection.createArrayOf("text", itemIDsArray);
            System.out.println("items array : " + "itemIDs");
            stmt.setString(11, "itemsIDs");
            System.out.println("created at : " + Timestamp.valueOf(order.getCreatedAt()));
            stmt.setTimestamp(12, Timestamp.valueOf(order.getCreatedAt()));
            System.out.println("updated at : " + Timestamp.valueOf(order.getUpdatedAt()));
            stmt.setTimestamp(13, Timestamp.valueOf(order.getUpdatedAt()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
}