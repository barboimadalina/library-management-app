package org.andrei.sample_project.repository;

import org.andrei.sample_project.ComingSoonBook;
import org.andrei.sample_project.connection.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ComingSoonRepository {

    public List<ComingSoonBook> getAllComingSoonBooks() {
        String sql = "SELECT * FROM coming_soon_books ORDER BY release_date ASC";
        List<ComingSoonBook> books = new ArrayList<>();

        System.out.println(">>> getAllComingSoonBooks");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(extractComingSoonBookFromResultSet(rs));
            }

            System.out.println(">>> Found " + books.size() + " coming soon books");

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAllComingSoonBooks: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    public ComingSoonBook getComingSoonBookById(int id) {
        String sql = "SELECT * FROM coming_soon_books WHERE id = ?";

        System.out.println(">>> getComingSoonBookById: " + id);

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractComingSoonBookFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getComingSoonBookById: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<ComingSoonBook> getUpcomingBooks() {
        String sql = "SELECT * FROM coming_soon_books ORDER BY id DESC LIMIT 10";
        List<ComingSoonBook> books = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(extractComingSoonBookFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUpcomingBooks: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    public boolean addComingSoonBook(ComingSoonBook book) {
        String sql = "INSERT INTO coming_soon_books (title, author, genre, description, release_date, status, cover_image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        System.out.println(">>> addComingSoonBook: " + book.getTitle());

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setString(4, book.getDescription());
            stmt.setDate(5, book.getReleaseDate() != null ? Date.valueOf(book.getReleaseDate()) : null);
            stmt.setString(6, book.getStatus() != null ? book.getStatus() : "UPCOMING");
            stmt.setString(7, book.getCoverImage());

            int rows = stmt.executeUpdate();
            System.out.println(">>> Inserted " + rows + " row(s)");
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addComingSoonBook: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateComingSoonBook(ComingSoonBook book) {
        String sql = "UPDATE coming_soon_books SET title = ?, author = ?, genre = ?, description = ?, " +
                "release_date = ?, status = ?, cover_image = ? WHERE id = ?";

        System.out.println(">>> updateComingSoonBook: " + book.getId());

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setString(4, book.getDescription());
            stmt.setDate(5, book.getReleaseDate() != null ? Date.valueOf(book.getReleaseDate()) : null);
            stmt.setString(6, book.getStatus());
            stmt.setString(7, book.getCoverImage());
            stmt.setInt(8, book.getId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateComingSoonBook: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE coming_soon_books SET status = ? WHERE id = ?";

        System.out.println(">>> updateStatus: id=" + id + ", status=" + status);

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, id);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteComingSoonBook(int id) {
        String sql = "DELETE FROM coming_soon_books WHERE id = ?";

        System.out.println(">>> deleteComingSoonBook: " + id);

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            System.out.println(">>> Deleted " + rows + " row(s)");
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deleteComingSoonBook: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private ComingSoonBook extractComingSoonBookFromResultSet(ResultSet rs) throws SQLException {
        ComingSoonBook book = new ComingSoonBook();

        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));  // Changed from "author_name" to "author"
        book.setGenre(rs.getString("genre"));
        book.setDescription(rs.getString("description"));

        Date releaseDate = rs.getDate("release_date");  // Changed from "expected_release_date" to "release_date"
        if (releaseDate != null) {
            book.setReleaseDate(releaseDate.toLocalDate());
        }

        book.setStatus(rs.getString("status"));
        book.setCoverImage(rs.getString("cover_image"));

        return book;
    }

}