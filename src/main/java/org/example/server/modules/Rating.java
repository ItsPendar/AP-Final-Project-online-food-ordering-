package org.example.server.modules;

import java.util.List;

public class Rating {

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(List<String> imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    private int id;

    public Rating(int id, int rating, String comment, List<String> imageBase64, int user_id, int order_id, String created_at, String item_ids, int vendor_id) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.imageBase64 = imageBase64;
        this.user_id = user_id;
        this.order_id = order_id;
        this.created_at = created_at;
        this.item_ids = item_ids;
        this.vendor_id = vendor_id;
    }

    private int rating;
    private String comment;
    private List<String> imageBase64;
    private int user_id;
    private int order_id;
    private String created_at;
    private String item_ids;
    private int vendor_id;

    public int getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(int vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getItem_ids() {
        return item_ids;
    }

    public void setItem_ids(String item_ids) {
        this.item_ids = item_ids;
    }

    public Rating() {}

}