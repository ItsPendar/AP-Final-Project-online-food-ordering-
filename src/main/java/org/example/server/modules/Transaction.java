package org.example.server.modules;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionID;
    private int userID;
    private String method;
    private String status;
    private int orderID;
    private LocalDateTime createdAt;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    private double amount;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Transaction(int transactionID, int orderID, int userID, String method, String status, LocalDateTime createdAt, double amount) {
        this.transactionID = transactionID;
        this.orderID = orderID;
        this.userID = userID;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
        this.amount = amount;
    }
    public Transaction() {

    }
    public int getTransactionID() {
        return transactionID;
    }
    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }
    public int getOrderID() {
        return orderID;
    }
    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }
    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
