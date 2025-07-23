package org.example.server.modules;

import org.example.server.dao.TransactionDAO;

import java.time.LocalDateTime;

public class Transaction {

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
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

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
    private int id;

    public Transaction(int id, int user_id, String method, String status, int order_id, LocalDateTime created_at, double amount) {
        this.id = id;
        this.user_id = user_id;
        this.method = method;
        this.status = status;
        this.order_id = order_id;
        this.created_at = created_at;
        this.amount = amount;
    }

    public Transaction() {

    }

    private int user_id;
    private String method;
    private String status;
    private int order_id;
    private LocalDateTime created_at;
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
