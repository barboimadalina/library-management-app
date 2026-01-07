package org.andrei.sample_project.repository;

import org.andrei.sample_project.Book;

import org.andrei.sample_project.connection.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FavoriteRepository
 */
public class FavoriteRepository {

    /**
     * Checks if a book is in user's favorites
     */
    public boolean isFavorite(int userId, int bookId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.out.println(">>> ERROR in isFavorite: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Gets all favorite books for a user
     */
    public List<Book> getUserFavorites(int userId) {
        String sql = "SELECT b.*, a.name as author_name FROM books b " +
                "JOIN authors a ON b.author_id = a.author_id " +
                "JOIN favorites f ON b.book_id = f.book_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY f.created_at DESC";

        System.out.println(">>> getUserFavorites: " + userId);

        List<Book> books = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return books;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserFavorites: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return books;
    }

    /**
     * Gets count of user's favorites
     */
    public int getUserFavoriteCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM favorites WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return 0;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserFavoriteCount: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }


    public int getBookFavoriteCount(int bookId) {
        String sql = "SELECT COUNT(*) as count FROM favorites WHERE book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return 0;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getBookFavoriteCount: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }

    /**
     * Adds a book to user's favorites
     */
    public boolean addFavorite(int userId, int bookId) {
        String sql = "INSERT INTO favorites (user_id, book_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

        System.out.println(">>> addFavorite: userId=" + userId + ", bookId=" + bookId);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addFavorite: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Removes a book from user's favorites
     */
    public boolean removeFavorite(int userId, int bookId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND book_id = ?";

        System.out.println(">>> removeFavorite: userId=" + userId + ", bookId=" + bookId);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in removeFavorite: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthorId(rs.getInt("author_id"));
        book.setAuthorName(rs.getString("author_name"));
        book.setGenre(rs.getString("genre"));
        book.setDescription(rs.getString("description"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setPageCount(rs.getInt("page_count"));

        // Handle nullable columns
        try {
            book.setIsbn(rs.getString("isbn"));
        } catch (SQLException e) {
            book.setIsbn(null);
        }

        try {
            book.setAverageRating(rs.getDouble("average_rating"));
        } catch (SQLException e) {
            book.setAverageRating(0.0);
        }

        try {
            book.setRatingCount(rs.getInt("rating_count"));
        } catch (SQLException e) {
            book.setRatingCount(0);
        }

        // book.setCoverImage(rs.getString("cover_image"));

        return book;
    }

}