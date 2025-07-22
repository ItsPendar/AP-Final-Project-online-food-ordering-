package org.example.server.modules;

import java.time.LocalDate;

public class Coupon {
    private int couponID;

    public int getCouponID() {
        return couponID;
    }

    public void setCouponID(int couponID) {
        this.couponID = couponID;
    }

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getCouponValue() {
        return couponValue;
    }

    public void setCouponValue(double couponValue) {
        this.couponValue = couponValue;
    }

    public double getCouponMinPrice() {
        return couponMinPrice;
    }

    public void setCouponMinPrice(double couponMinPrice) {
        this.couponMinPrice = couponMinPrice;
    }

    public double getUser_count() {
        return user_count;
    }

    public void setUser_count(double user_count) {
        this.user_count = user_count;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    private String couponType;
    private String couponCode;
    private double couponValue;
    private double couponMinPrice;
    private double user_count;
    private LocalDate startDate;
    private LocalDate endDate;

    public Coupon(int couponID, String couponType, String couponCode, double couponValue, double couponMinPrice, double user_count, LocalDate startDate, LocalDate endDate) {
        this.couponID = couponID;
        this.couponType = couponType;
        this.couponCode = couponCode;
        this.couponValue = couponValue;
        this.couponMinPrice = couponMinPrice;
        this.user_count = user_count;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
