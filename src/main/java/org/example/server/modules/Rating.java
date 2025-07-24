package org.example.server.modules;

import java.util.List;

public class Rating {
    private int id;
    private int itemId;
    private int rating;
    private String comment;
    private List<String> imageBase64;
    private int userId;
    private String createdAt;

    // Constructors
    public Rating() {}

    public Rating(int id, int itemId, int rating, String comment, List<String> imageBase64, int userId, String createdAt) {
        this.id = id;
        this.itemId = itemId;
        this.rating = rating;
        this.comment = comment;
        this.imageBase64 = imageBase64;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public List<String> getImageBase64() { return imageBase64; }
    public void setImageBase64(List<String> imageBase64) { this.imageBase64 = imageBase64; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}