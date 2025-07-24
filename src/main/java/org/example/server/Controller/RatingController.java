package org.example.server.Controller;

import org.example.server.dao.RatingDAO;
import org.example.server.modules.Rating;

import java.sql.SQLException;
import java.util.List;

public class RatingController {
    private final RatingDAO ratingDAO;

    public RatingController() throws SQLException {
        this.ratingDAO = new RatingDAO();
    }


    public void createRating(Rating rating) throws SQLException {
        ratingDAO.saveRatingWithImages(rating);
    }


    public List<Rating> getRatingsByItemId(int itemId) throws SQLException {
        return ratingDAO.getRatingsByItemId(itemId);
    }


    public Rating getRatingById(int ratingId) throws SQLException {
        return ratingDAO.getRatingById(ratingId);
    }


    public boolean deleteRating(int ratingId) throws SQLException {
        return ratingDAO.deleteRating(ratingId);
    }


    public boolean updateRating(Rating rating) throws SQLException {
        return ratingDAO.updateRating(rating);
    }
}