package org.example.server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.server.modules.Rating;


public class RatingDAO {
    private static final Connection connection;

    static {
        try {
            connection = DatabaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public RatingDAO() throws SQLException {
        this.createRatingsTable(connection);
        this.createRatingsImageTable(connection);
    }

    public void createRatingsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS ratings (
                    id SERIAL PRIMARY KEY,
                    order_id INTEGER NOT NULL,
                    vendor_id INTEGER NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 0 AND rating <= 5),
                    comment TEXT,
                    item_ids VARCHAR(400),
                    user_id INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void createRatingsImageTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS rating_images (
                    id SERIAL PRIMARY KEY,
                    rating_id INTEGER REFERENCES ratings(id) ON DELETE CASCADE,
                    image_base64 TEXT
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public int insertRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (order_id, rating, comment, user_id, item_ids, vendor_id) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating.getOrder_id());
            stmt.setInt(2, rating.getRating());
            stmt.setString(3, rating.getComment());
            stmt.setInt(4, rating.getUser_id());
            stmt.setString(5, rating.getItem_ids());
            stmt.setInt(6, rating.getVendor_id());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    public void insertRatingImages(int ratingId, List<String> imageBase64List) throws SQLException {
        String sql = "INSERT INTO rating_images (rating_id, image_base64) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String image : imageBase64List) {
                stmt.setInt(1, ratingId);
                stmt.setString(2, image);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void saveRatingWithImages(Rating rating) throws SQLException {
        int ratingId = insertRating(rating);
        if (ratingId != -1 && rating.getImageBase64() != null && !rating.getImageBase64().isEmpty()) {
            insertRatingImages(ratingId, rating.getImageBase64());
        }
    }


    public Rating getRatingByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE order_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setOrder_id(rs.getInt("order_id"));
                rating.setRating(rs.getInt("rating"));
                rating.setComment(rs.getString("comment"));
                rating.setUser_id(rs.getInt("user_id"));
                rating.setCreated_at(rs.getTimestamp("created_at").toString());
                rating.setItem_ids(rs.getString("item_ids"));
                rating.setVendor_id(rs.getInt("vendor_id"));
                rating.setImageBase64(getImagesForRating(rating.getId()));
                return rating;
            }
        }
        return null;
    }

    public List<Rating> getRatingsByUserId(int userId) throws SQLException {
        List<Rating> comments = new ArrayList<>();

        String sql = "SELECT id, comment, user_id, created_at, rating, item_ids, order_id, vendor_id FROM ratings WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("I'm in the while loop in RatingDAO");
                int ratingId = rs.getInt("id");
                String commentText = rs.getString("comment");
                String createdAt = rs.getTimestamp("created_at").toString();
                String itemIDs = rs.getString("item_ids");
                List<String> images = getImagesForRating(ratingId);
                int rating = rs.getInt("rating");
                int orderID = rs.getInt("order_id");
                int vendorID = rs.getInt("vendor_id");

                if (commentText != null && !commentText.trim().isEmpty()) {
                    comments.add(new Rating(ratingId,rating,commentText,images,userId,orderID,createdAt,itemIDs,vendorID));
                }
            }
        }

        return comments;
    }

    public List<Rating> getRatingsByItemId(int itemId) throws SQLException {
        List<Rating> comments = new ArrayList<>();

        String sql = "SELECT id, comment, user_id, created_at, rating, item_ids, order_id, vendor_id FROM ratings WHERE ',' || item_ids || ',' LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%," + itemId + ",%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int ratingId = rs.getInt("id");
                String commentText = rs.getString("comment");
                int userId = rs.getInt("user_id");
                String createdAt = rs.getTimestamp("created_at").toString();
                String itemIDs = rs.getString("item_ids");
                List<String> images = getImagesForRating(ratingId);
                int rating = rs.getInt("rating");
                int orderID = rs.getInt("order_id");
                int vendorID = rs.getInt("vendor_id");

                if (commentText != null && !commentText.trim().isEmpty()) {
                    comments.add(new Rating(ratingId,rating,commentText,images,userId,orderID,createdAt,itemIDs,vendorID));
                }
            }
        }

        return comments;
    }

    private List<String> getImagesForRating(int ratingId) throws SQLException {
        List<String> images = new ArrayList<>();
        String sql = "SELECT image_base64 FROM rating_images WHERE rating_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ratingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                images.add(rs.getString("image_base64"));
            }
        }
        return images;
    }

    public Rating getRatingById(int ratingId) throws SQLException {
        String query = "SELECT * FROM ratings WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1,ratingId);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            Rating rating = new Rating();
            rating.setId(rs.getInt("id"));
            rating.setOrder_id(rs.getInt("order_id"));
            rating.setRating(rs.getInt("rating"));
            rating.setComment(rs.getString("comment"));
            rating.setUser_id(rs.getInt("user_id"));
            rating.setCreated_at(rs.getTimestamp("created_at").toString());
            rating.setItem_ids(rs.getString("item_ids"));
            rating.setVendor_id(rs.getInt("vendor_id"));

            List<String> imageBase64List = new ArrayList<>();
            String imgQuery = "SELECT image_base64 FROM rating_images WHERE rating_id = ?";
            PreparedStatement imgStmt = connection.prepareStatement(imgQuery);
            imgStmt.setInt(1,ratingId);
            ResultSet imgRs = imgStmt.executeQuery();
            while(imgRs.next()) {
                imageBase64List.add(imgRs.getString("image_base64"));
            }
            rating.setImageBase64(imageBase64List);

            return rating;
        }
        return null;
    }

    public boolean deleteRating(int ratingId) throws SQLException {
        String deleteImageQuery = "DELETE FROM rating_images WHERE rating_id = ?";
        PreparedStatement imgStmt = connection.prepareStatement(deleteImageQuery);
        imgStmt.setInt(1, ratingId);
        imgStmt.executeUpdate();

        String deleteRatingQuery = "DELETE FROM ratings WHERE id = ?";
        PreparedStatement ratingStmt = connection.prepareStatement(deleteRatingQuery);
        ratingStmt.setInt(1, ratingId);
        int affectedRows = ratingStmt.executeUpdate();

        return affectedRows > 0;
    }

    public boolean updateRating(Rating rating) throws SQLException {
        // 1. Update the Rating (comment and rating)
        String updateRatingQuery = "UPDATE ratings SET rating = ?, comment = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateRatingQuery)) {
            stmt.setInt(1, rating.getRating());
            stmt.setString(2, rating.getComment());
            stmt.setInt(3, rating.getId());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false; // No rows were updated
            }

            // 2. Delete existing images
            String deleteImagesQuery = "DELETE FROM rating_images WHERE rating_id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteImagesQuery)) {
                deleteStmt.setInt(1, rating.getId());
                deleteStmt.executeUpdate();
            }

            // 3. Insert new images if they exist
            if (rating.getImageBase64() != null && !rating.getImageBase64().isEmpty()) {
                String insertImageQuery = "INSERT INTO rating_images (rating_id, image_base64) VALUES (?, ?)";
                try (PreparedStatement imageStmt = connection.prepareStatement(insertImageQuery)) {
                    for (String base64 : rating.getImageBase64()) {
                        imageStmt.setInt(1, rating.getId());
                        imageStmt.setString(2, base64);
                        imageStmt.addBatch(); // Add to the batch for inserting images
                    }
                    // Execute the batch for image insertions
                    int[] insertedRows = imageStmt.executeBatch();
                    // Optionally handle inserted rows (to check for any failures)
                    if (insertedRows.length != rating.getImageBase64().size()) {
                        return false; // If the number of inserted rows does not match the size of the image list, return false
                    }
                }
            }
        }
        return true; // Return true if everything was successful
    }
}