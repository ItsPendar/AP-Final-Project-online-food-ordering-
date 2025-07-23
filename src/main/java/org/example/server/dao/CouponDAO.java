package org.example.server.dao;

import org.example.server.modules.Coupon;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CouponDAO {
    private static final Connection connection;
    static {
        try {
            connection = DatabaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public CouponDAO() throws SQLException {
        this.createCouponsTable(connection);
    }
    public void createCouponsTable(Connection connection) throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS coupons (
            coupon_id SERIAL PRIMARY KEY,
            couponType VARCHAR(50) NOT NULL,
            couponCode VARCHAR(100) NOT NULL UNIQUE,
            couponValue REAL PRECISION NOT NULL,
            couponMinPrice REAL NOT NULL,
            user_count INTEGER NOT NULL,
            startDate DATE NOT NULL,
            endDate DATE NOT NULL
        );
    """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
