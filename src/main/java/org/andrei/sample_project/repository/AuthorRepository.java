package org.andrei.sample_project.repository;
import org.andrei.sample_project.connection.ConnectionFactory;
import org.andrei.sample_project.Author;
import org.andrei.sample_project.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthorRepository
 * Handles all database operations for authors.
 */
public class AuthorRepository {
    /**
     * Gets an author by ID with book count
     */
    public Author getAuthorById(int authorId) {
        String sql = "SELECT a.*, " +
                "(SELECT COUNT(*) FROM books b WHERE b.author_id = a.author_id) as book_count " +
                "FROM authors a WHERE a.author_id = ?";

        System.out.println(">>> getAuthorById: " + authorId);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return null;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, authorId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractAuthorFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAuthorById: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Gets an author by name
     */
    public Author getAuthorByName(String name) {
        String sql = "SELECT a.*, " +
                "(SELECT COUNT(*) FROM books b WHERE b.author_id = a.author_id) as book_count " +
                "FROM authors a WHERE LOWER(a.name) LIKE LOWER(?)";

        System.out.println(">>> getAuthorByName: " + name);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return null;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + name + "%");
            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractAuthorFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAuthorByName: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Searches authors by name or nationality
     */
    public List<Author> searchAuthors(String searchTerm) {
        String sql = "SELECT a.*, " +
                "(SELECT COUNT(*) FROM books b WHERE b.author_id = a.author_id) as book_count " +
                "FROM authors a " +
                "WHERE LOWER(a.name) LIKE LOWER(?) " +
                "OR LOWER(a.nationality) LIKE LOWER(?) " +
                "ORDER BY a.name";

        System.out.println(">>> searchAuthors: " + searchTerm);

        List<Author> authors = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return authors;

            stmt = conn.prepareStatement(sql);
            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            rs = stmt.executeQuery();

            while (rs.next()) {
                authors.add(extractAuthorFromResultSet(rs));
            }

            System.out.println(">>> Found " + authors.size() + " authors");

        } catch (SQLException e) {
            System.out.println(">>> ERROR in searchAuthors: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return authors;
    }

    /**
     * Gets all authors with book counts
     */
    public List<Author> getAllAuthors() {
        String sql = "SELECT a.*, " +
                "(SELECT COUNT(*) FROM books b WHERE b.author_id = a.author_id) as book_count " +
                "FROM authors a ORDER BY a.name";

        System.out.println(">>> getAllAuthors");

        List<Author> authors = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return authors;

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                authors.add(extractAuthorFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAllAuthors: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return authors;
    }

    /**
     * Gets all books by an author
     */
    public List<Book> getBooksByAuthor(int authorId) {
        String sql = "SELECT b.*, a.name as author_name " +
                "FROM books b " +
                "JOIN authors a ON b.author_id = a.author_id " +
                "WHERE b.author_id = ? " +
                "ORDER BY b.publication_year DESC, b.title";

        System.out.println(">>> getBooksByAuthor: " + authorId);

        List<Book> books = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return books;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, authorId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }

            System.out.println(">>> Found " + books.size() + " books");

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getBooksByAuthor: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return books;
    }

    /**
     * Adds a new author
     */
    public Author addAuthor(Author author) {
        String sql = "INSERT INTO authors (name, biography, birth_date, death_date, nationality, website, profile_image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING author_id";

        System.out.println(">>> addAuthor: " + author.getName());

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return null;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, author.getName());
            stmt.setString(2, author.getBiography());

            if (author.getBirthDate() != null) {
                stmt.setDate(3, Date.valueOf(author.getBirthDate()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            if (author.getDeathDate() != null) {
                stmt.setDate(4, Date.valueOf(author.getDeathDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, author.getNationality());
            stmt.setString(6, author.getWebsite());
            stmt.setString(7, author.getProfileImageUrl());

            rs = stmt.executeQuery();

            if (rs.next()) {
                author.setAuthorId(rs.getInt("author_id"));
                return author;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addAuthor: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Updates an existing author
     */
    public boolean updateAuthor(Author author) {
        String sql = "UPDATE authors SET name = ?, biography = ?, birth_date = ?, death_date = ?, " +
                "nationality = ?, website = ?, profile_image_url = ? WHERE author_id = ?";

        System.out.println(">>> updateAuthor: " + author.getAuthorId());

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, author.getName());
            stmt.setString(2, author.getBiography());

            if (author.getBirthDate() != null) {
                stmt.setDate(3, Date.valueOf(author.getBirthDate()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            if (author.getDeathDate() != null) {
                stmt.setDate(4, Date.valueOf(author.getDeathDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, author.getNationality());
            stmt.setString(6, author.getWebsite());
            stmt.setString(7, author.getProfileImageUrl());
            stmt.setInt(8, author.getAuthorId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateAuthor: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    private Author extractAuthorFromResultSet(ResultSet rs) throws SQLException {
        Author author = new Author();
        author.setAuthorId(rs.getInt("author_id"));
        author.setName(rs.getString("name"));
        author.setBiography(rs.getString("biography"));

        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            author.setBirthDate(birthDate.toLocalDate());
        }

        Date deathDate = rs.getDate("death_date");
        if (deathDate != null) {
            author.setDeathDate(deathDate.toLocalDate());
        }

        author.setNationality(rs.getString("nationality"));
        author.setWebsite(rs.getString("website"));
        author.setProfileImageUrl(rs.getString("profile_image_url"));

        try {
            author.setBookCount(rs.getInt("book_count"));
        } catch (SQLException e) {
            // column not in result set
        }

        return author;
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
        book.setIsbn(rs.getString("isbn"));
        book.setAverageRating(rs.getDouble("average_rating"));
        book.setRatingCount(rs.getInt("rating_count"));
        book.setCoverImage(rs.getString("cover_image"));
        return book;
    }
}