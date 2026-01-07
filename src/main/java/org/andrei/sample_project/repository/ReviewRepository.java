package org.andrei.sample_project.repository;

import org.andrei.sample_project.connection.ConnectionFactory;

import org.andrei.sample_project.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewRepository
 * Handles all database operations for reviews and ratings.
 */
public class ReviewRepository {

    /**
     * Gets all reviews for a book
     */
    public List<Review> getBookReviews(int bookId) {
        String sql = "SELECT r.*, u.full_name as user_full_name, b.title as book_title " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN books b ON r.book_id = b.book_id " +
                "WHERE r.book_id = ? " +
                "ORDER BY r.created_at DESC";

        System.out.println(">>> getBookReviews: " + bookId);

        List<Review> reviews = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return reviews;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(extractReviewFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getBookReviews: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return reviews;
    }

    /**
     * Gets reviews for a book filtered by rating
     */
    public List<Review> getBookReviewsByRating(int bookId, int rating) {
        String sql = "SELECT r.*, u.full_name as user_full_name, b.title as book_title " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN books b ON r.book_id = b.book_id " +
                "WHERE r.book_id = ? AND r.rating = ? " +
                "ORDER BY r.created_at DESC";

        System.out.println(">>> getBookReviewsByRating: bookId=" + bookId + ", rating=" + rating);

        List<Review> reviews = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return reviews;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.setInt(2, rating);
            rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(extractReviewFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getBookReviewsByRating: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return reviews;
    }

    /**
     * Gets review counts by rating for a book
     * Returns an array where index 0 = 1-star count, index 4 = 5-star count
     */
    public int[] getReviewCountsByRating(int bookId) {
        String sql = "SELECT rating, COUNT(*) as count FROM reviews " +
                "WHERE book_id = ? GROUP BY rating ORDER BY rating";

        int[] counts = new int[5];
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return counts;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int rating = rs.getInt("rating");
                int count = rs.getInt("count");
                if (rating >= 1 && rating <= 5) {
                    counts[rating - 1] = count;
                }
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getReviewCountsByRating: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return counts;
    }

    /**
     * Gets all reviews by a user
     */
    public List<Review> getUserReviews(int userId) {
        String sql = "SELECT r.*, u.full_name as user_full_name, b.title as book_title " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN books b ON r.book_id = b.book_id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.created_at DESC";

        System.out.println(">>> getUserReviews: " + userId);

        List<Review> reviews = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return reviews;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(extractReviewFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserReviews: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return reviews;
    }

    /**
     * Gets a specific review by user and book
     */
    public Review getUserReviewForBook(int userId, int bookId) {
        String sql = "SELECT r.*, u.full_name as user_full_name, b.title as book_title " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN books b ON r.book_id = b.book_id " +
                "WHERE r.user_id = ? AND r.book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return null;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractReviewFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserReviewForBook: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Gets recent reviews (for activity feed)
     */
    public List<Review> getRecentReviews(int limit) {
        String sql = "SELECT r.*, u.full_name as user_full_name, b.title as book_title " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN books b ON r.book_id = b.book_id " +
                "ORDER BY r.created_at DESC LIMIT ?";

        List<Review> reviews = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return reviews;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(extractReviewFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getRecentReviews: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return reviews;
    }

    /**
     * Adds or updates a review
     */
    public boolean addOrUpdateReview(int userId, int bookId, int rating, String reviewText) {
        // Check if review exists
        Review existing = getUserReviewForBook(userId, bookId);

        String sql;
        if (existing != null) {
            sql = "UPDATE reviews SET rating = ?, review_text = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE user_id = ? AND book_id = ?";
        } else {
            sql = "INSERT INTO reviews (rating, review_text, user_id, book_id) VALUES (?, ?, ?, ?)";
        }

        System.out.println(">>> addOrUpdateReview: userId=" + userId + ", bookId=" + bookId + ", rating=" + rating);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rating);
            stmt.setString(2, reviewText);
            stmt.setInt(3, userId);
            stmt.setInt(4, bookId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                // Update book's average rating
                updateBookAverageRating(bookId);
                return true;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addOrUpdateReview: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Updates the book's average rating and review count
     */
    private void updateBookAverageRating(int bookId) {
        String sql = "UPDATE books SET " +
                "average_rating = (SELECT AVG(rating)::numeric(3,2) FROM reviews WHERE book_id = ?), " +
                "rating_count = (SELECT COUNT(*) FROM reviews WHERE book_id = ?) " +
                "WHERE book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.setInt(2, bookId);
            stmt.setInt(3, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateBookAverageRating: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    /**
     * Deletes a review
     */
    public boolean deleteReview(int userId, int bookId) {
        String sql = "DELETE FROM reviews WHERE user_id = ? AND book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                updateBookAverageRating(bookId);
                return true;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deleteReview: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    private Review extractReviewFromResultSet(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setBookId(rs.getInt("book_id"));
        review.setRating(rs.getInt("rating"));
        review.setReviewText(rs.getString("review_text"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            review.setCreatedAt(createdAt.toLocalDateTime());
        }

        try {
            review.setUserFullName(rs.getString("user_full_name"));
        } catch (SQLException e) {}

        try {
            review.setBookTitle(rs.getString("book_title"));
        } catch (SQLException e) {}

        return review;
    }
}