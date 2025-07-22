package org.example.server.Controller;

import org.example.server.dao.CouponDAO;

import java.sql.SQLException;

public class CouponController {
    private static CouponDAO couponDAO = null;

    public CouponController() throws SQLException {
        couponDAO = new CouponDAO();
    }


}
