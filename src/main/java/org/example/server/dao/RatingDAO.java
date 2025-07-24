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
                    item_id INTEGER NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                    comment TEXT,
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
        String sql = "INSERT INTO ratings (item_id, rating, comment, user_id) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating.getItemId());
            stmt.setInt(2, rating.getRating());
            stmt.setString(3, rating.getComment());
            stmt.setInt(4, rating.getUserId());
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

    public List<Rating> getRatingsByItemId(int itemId) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE item_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setItemId(rs.getInt("item_id"));
                rating.setRating(rs.getInt("rating"));
                rating.setComment(rs.getString("comment"));
                rating.setUserId(rs.getInt("user_id"));
                rating.setCreatedAt(rs.getTimestamp("created_at").toString());
                rating.setImageBase64(getImagesForRating(rating.getId()));
                ratings.add(rating);
            }
        }
        return ratings;
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
            rating.setItemId(rs.getInt("item_id"));
            rating.setRating(rs.getInt("rating"));
            rating.setComment(rs.getString("comment"));
            rating.setUserId(rs.getInt("user_id"));
            rating.setCreatedAt(rs.getTimestamp("created_at").toString());

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
        String updateRatingQuery = "UPDATE ratings SET rating = ?, comment = ? WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(updateRatingQuery);
        stmt.setInt(1,rating.getRating());
        stmt.setString(2,rating.getComment());
        stmt.setInt(3,rating.getId());
        int affectedRows = stmt.executeUpdate();

        String deleteImagesQuery = "DELETE FROM rating_images WHERE rating_id = ?";
        PreparedStatement deleteStmt = connection.prepareStatement(deleteImagesQuery);
        deleteStmt.setInt(1, rating.getId());
        deleteStmt.executeUpdate();
        if(rating.getImageBase64() != null) {
            String insertImageQuery = "INSERT INTO rating_images (rating_id, image_base64) VALUES (?, ?)";
            PreparedStatement imageStmt = connection.prepareStatement(insertImageQuery);
            for(String base64 : rating.getImageBase64()) {
                imageStmt.setInt(1, rating.getId());
                imageStmt.setString(2, base64);
                imageStmt.addBatch();
            }
            stmt.executeBatch();
        }
        return affectedRows > 0;
    }
}