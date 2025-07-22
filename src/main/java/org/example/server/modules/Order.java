package org.example.server.modules;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Order {
    private int customerID;
    private String deliveryAddress;
    private int vendorID;
    private int courierID;
    private List<String> orderItemIDs;
    private double rawPrice;
    private double taxFee;
    private double courierFee;
    private double additionalFee;
    private double payPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Order(int customerID, String deliveryAddress, int vendorID, int courierID, List<String> orderItemIDs, double rawPrice, double taxFee, double courierFee, double additionalFee, double payPrice, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.customerID = customerID;
        this.deliveryAddress = deliveryAddress;
        this.vendorID = vendorID;
        this.courierID = courierID;
        this.orderItemIDs = orderItemIDs;
        this.rawPrice = rawPrice;
        this.taxFee = taxFee;
        this.courierFee = courierFee;
        this.additionalFee = additionalFee;
        this.payPrice = payPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public Order() {

    }
    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public int getVendorID() {
        return vendorID;
    }

    public void setVendorID(int vendorID) {
        this.vendorID = vendorID;
    }

    public int getCourierID() {
        return courierID;
    }

    public void setCourierID(int courierID) {
        this.courierID = courierID;
    }

    public List<String> getOrderItemIDs() {
        return orderItemIDs;
    }

    public void setOrderItemIDs(List<String> orderItemIDS) {
        this.orderItemIDs = orderItemIDs;
    }

    public double getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(double rawPrice) {
        this.rawPrice = rawPrice;
    }

    public double getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(double taxFee) {
        this.taxFee = taxFee;
    }

    public double getCourierFee() {
        return courierFee;
    }

    public void setCourierFee(double courierFee) {
        this.courierFee = courierFee;
    }

    public double getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(double additionalFee) {
        this.additionalFee = additionalFee;
    }

    public double getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(double payPrice) {
        this.payPrice = payPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
