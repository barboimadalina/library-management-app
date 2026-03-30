package org.andrei.sample_project.repository;

import org.andrei.sample_project.Book;
import org.andrei.sample_project.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookRepository {

    public List<Book> getAllBooks() {
        String sql = "SELECT b.*, a.name as author_name " +
                "FROM books b " +
                "JOIN authors a ON b.author_id = a.author_id " +
                "ORDER BY b.title";

        List<Book> books = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in getAllBooks");
                return books;
            }

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
                count++;
            }

            System.out.println(">>> getAllBooks: Retrieved " + count + " books");

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAllBooks: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return books;
    }

    public List<Book> searchBooks(String searchTerm) {
        String sql = "SELECT b.*, a.name as author_name " +
                "FROM books b " +
                "JOIN authors a ON b.author_id = a.author_id " +
                "WHERE b.title ILIKE ? OR a.name ILIKE ? " +
                "ORDER BY b.average_rating DESC " +
                "LIMIT 20";

        List<Book> books = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in searchBooks");
                return books;
            }

            stmt = conn.prepareStatement(sql);

            String pattern = "%" + searchTerm + "%";
            System.out.println(">>> searchBooks: Searching for pattern: " + pattern);
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
                count++;
            }

            System.out.println(">>> searchBooks: Found " + count + " books for term: " + searchTerm);

        } catch (SQLException e) {
            System.out.println(">>> ERROR in searchBooks: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return books;
    }

    public List<Book> getUserBooksByStatus(int userId, String status) {
        String sql = "SELECT b.*, a.name as author_name, ub.current_page, ub.start_date, ub.finish_date " +
                "FROM user_books ub " +
                "JOIN books b ON ub.book_id = b.book_id " +
                "JOIN authors a ON b.author_id = a.author_id " +
                "WHERE ub.user_id = ? AND ub.status = ? " +
                "ORDER BY ub.added_at DESC";

        System.out.println(">>> getUserBooksByStatus: user " + userId + ", status: " + status);

        List<Book> books = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database");
                return books;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, status);

            rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                book.setReadingStatus(status);

                // Get current page from result set
                int currentPage = 0;
                try {
                    currentPage = rs.getInt("current_page");
                    if (rs.wasNull()) {
                        currentPage = 0;
                    }
                } catch (SQLException e) {
                    currentPage = 0;
                }
                book.setCurrentPage(currentPage);

                try {
                    Date startDate = rs.getDate("start_date");
                    if (startDate != null && !rs.wasNull()) {
                        book.setStartDate(startDate.toString());
                    }

                    Date finishDate = rs.getDate("finish_date");
                    if (finishDate != null && !rs.wasNull()) {
                        book.setFinishDate(finishDate.toString());
                    }
                } catch (SQLException e) {
                    // Dates not available, skip
                }

                books.add(book);
                count++;
                System.out.println(">>>   Found book: " + book.getTitle() +
                        " (page: " + book.getCurrentPage() + "/" + book.getPageCount() +
                        ", status: " + status + ")");
            }

            System.out.println(">>> getUserBooksByStatus: Found " + count + " books with status: " + status);

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserBooksByStatus: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return books;
    }

    public List<Book> getTopRatedBooks(int limit) {
        String sql = "SELECT b.*, a.name as author_name, " +
                "COALESCE((SELECT COUNT(*) FROM reviews r WHERE r.book_id = b.book_id), 0) as review_count " +
                "FROM books b " +
                "JOIN authors a ON b.author_id = a.author_id " +
                "WHERE b.average_rating > 0 " +
                "ORDER BY b.average_rating DESC, review_count DESC " +
                "LIMIT ?";

        List<Book> books = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in getTopRatedBooks");
                return books;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                try {
                    book.setRatingCount(rs.getInt("review_count"));
                } catch (SQLException e) {
                    book.setRatingCount(0);
                }
                books.add(book);
                count++;
            }

            System.out.println(">>> getTopRatedBooks: Retrieved " + count + " books");

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getTopRatedBooks: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return books;
    }

    public int getUserCompletedBooksCount(int userId) {
        String sql = "SELECT COUNT(*) FROM user_books WHERE user_id = ? AND status = 'COMPLETED'";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in getUserCompletedBooksCount");
                return 0;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println(">>> getUserCompletedBooksCount: User " + userId + " has " + count + " completed books");
                return count;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserCompletedBooksCount: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }

    public boolean updateBookStatus(int userId, int bookId, String status) {
        String sql;

        if ("COMPLETED".equals(status)) {
            sql = "UPDATE user_books SET status = ?, finish_date = CURRENT_DATE, current_page = (SELECT page_count FROM books WHERE book_id = ?) " +
                    "WHERE user_id = ? AND book_id = ?";
        } else if ("READING".equals(status)) {
            sql = "UPDATE user_books SET status = ?, start_date = CURRENT_DATE " +
                    "WHERE user_id = ? AND book_id = ?";
        } else {
            sql = "UPDATE user_books SET status = ? " +
                    "WHERE user_id = ? AND book_id = ?";
        }

        System.out.println(">>> updateBookStatus: User " + userId + ", Book " + bookId + " -> " + status);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database");
                return false;
            }

            // First check if the book is already in user's list
            if (!isBookInUserList(userId, bookId)) {
                // If not, add it
                return addBookToUserList(userId, bookId, status);
            }

            stmt = conn.prepareStatement(sql);
            if ("COMPLETED".equals(status)) {
                stmt.setString(1, status);
                stmt.setInt(2, bookId);
                stmt.setInt(3, userId);
                stmt.setInt(4, bookId);
            } else {
                stmt.setString(1, status);
                stmt.setInt(2, userId);
                stmt.setInt(3, bookId);
            }

            int rows = stmt.executeUpdate();
            System.out.println(">>>   Rows affected: " + rows);

            // Update challenge progress when marking as completed
            if ("COMPLETED".equals(status)) {
                updateChallengesForCompletedBook(userId, bookId);
            }

            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>>   SQL ERROR: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    public boolean updateReadingProgress(int userId, int bookId, int currentPage) {
        // First check if current_page column exists
        String checkSql = "SELECT column_name FROM information_schema.columns " +
                "WHERE table_name = 'user_books' AND column_name = 'current_page'";

        Connection conn = null;
        PreparedStatement checkStmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database");
                return false;
            }

            checkStmt = conn.prepareStatement(checkSql);
            rs = checkStmt.executeQuery();

            if (!rs.next()) {

                System.out.println(">>> updateReadingProgress: current_page column doesn't exist. Attempting to create it...");
                try {
                    Statement alterStmt = conn.createStatement();
                    alterStmt.execute("ALTER TABLE user_books ADD COLUMN IF NOT EXISTS current_page INTEGER DEFAULT 0");
                    System.out.println(">>>   Successfully added current_page column");
                } catch (SQLException e) {
                    System.out.println(">>>   Failed to add column: " + e.getMessage());
                    return false;
                }
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR checking for current_page column: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(checkStmt);
        }

        String updateSql = "UPDATE user_books SET current_page = ?, added_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ? AND book_id = ?";

        PreparedStatement updateStmt = null;

        try {

            int totalPages = getBookPageCount(bookId);
            if (totalPages <= 0) {
                System.out.println(">>> ERROR: Could not get page count for book " + bookId);
                return false;
            }

            currentPage = Math.min(currentPage, totalPages);

            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, currentPage);
            updateStmt.setInt(2, userId);
            updateStmt.setInt(3, bookId);

            int rows = updateStmt.executeUpdate();
            System.out.println(">>> updateReadingProgress: Updated " + rows + " rows for user " +
                    userId + ", book " + bookId + ", page " + currentPage + "/" + totalPages);


            if (currentPage >= totalPages) {
                System.out.println(">>> Book is 100% complete, marking as COMPLETED");
                markBookAsCompleted(userId, bookId);
            } else if (currentPage >= totalPages * 0.95) {
                // Optional: You can still show a message for near completion
                System.out.println(">>> Book is " + (int)((currentPage * 100.0) / totalPages) + "% complete - Almost finished!");
            }
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateReadingProgress: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(updateStmt);
            ConnectionFactory.close(conn);
        }
    }

    private int getBookPageCount(int bookId) {
        String sql = "SELECT page_count FROM books WHERE book_id = ?";

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
                return rs.getInt("page_count");
            }
        } catch (SQLException e) {
            System.out.println(">>> ERROR getting page count: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }

    public boolean markBookAsCompleted(int userId, int bookId) {
        // Get total pages
        int totalPages = getBookPageCount(bookId);

        String sql = "UPDATE user_books SET status = 'COMPLETED', finish_date = CURRENT_DATE, " +
                "current_page = ? " +  // Set current page to total pages
                "WHERE user_id = ? AND book_id = ? AND status != 'COMPLETED'";  // Only update if not already completed

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, totalPages);      // Set current_page to total pages
            stmt.setInt(2, userId);
            stmt.setInt(3, bookId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(">>> markBookAsCompleted: Book " + bookId + " marked as COMPLETED for user " + userId);
                // Update challenges when book is completed
                updateChallengesForCompletedBook(userId, bookId);
            } else {
                System.out.println(">>> markBookAsCompleted: Book " + bookId + " was already COMPLETED or not found");
            }
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in markBookAsCompleted: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    public void updateChallengesForCompletedBook(int userId, int bookId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return;

            String bookGenre = null;
            String bookSql = "SELECT genre FROM books WHERE book_id = ?";
            stmt = conn.prepareStatement(bookSql);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                bookGenre = rs.getString("genre");
            }
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);

            String appChallengeSql =
                    "SELECT ac.challenge_id, ac.required_genre, ac.target " +
                            "FROM user_app_challenges uac " +
                            "JOIN app_challenges ac ON uac.challenge_id = ac.challenge_id " +
                            "WHERE ac.is_active = TRUE " +
                            "AND uac.user_id = ? " +
                            "AND uac.completed = FALSE " +
                            "AND ac.start_date <= CURRENT_DATE " +
                            "AND ac.end_date >= CURRENT_DATE";

            stmt = conn.prepareStatement(appChallengeSql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int challengeId = rs.getInt("challenge_id");
                String requiredGenre = rs.getString("required_genre");
                int target = rs.getInt("target");

                boolean genreMatches = false;
                if (requiredGenre == null || requiredGenre.trim().isEmpty()) {
                    genreMatches = true;
                } else if (bookGenre != null) {
                    String normalizedRequired = requiredGenre.trim().toLowerCase();
                    String normalizedBook = bookGenre.trim().toLowerCase();
                    if (normalizedBook.contains(normalizedRequired) || normalizedRequired.contains(normalizedBook)) {
                        genreMatches = true;
                    }
                }

                if (genreMatches) {
                    String updateSql =
                            "UPDATE user_app_challenges " +
                                    "SET current_progress = current_progress + 1 " +
                                    "WHERE user_id = ? AND challenge_id = ? " +
                                    "RETURNING current_progress";

                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, userId);
                    updateStmt.setInt(2, challengeId);
                    ResultSet updateRs = updateStmt.executeQuery();

                    if (updateRs.next()) {
                        int newProgress = updateRs.getInt("current_progress");
                        if (newProgress >= target) {
                            String completeSql = "UPDATE user_app_challenges SET completed = TRUE, completed_at = CURRENT_TIMESTAMP WHERE user_id = ? AND challenge_id = ?";
                            PreparedStatement completeStmt = conn.prepareStatement(completeSql);
                            completeStmt.setInt(1, userId);
                            completeStmt.setInt(2, challengeId);
                            completeStmt.executeUpdate();
                            ConnectionFactory.close(completeStmt);
                        }
                    }
                    ConnectionFactory.close(updateRs);
                    ConnectionFactory.close(updateStmt);
                }
            }
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);

            String personalSql =
                    "SELECT challenge_id, target_books FROM personal_challenges " +
                            "WHERE user_id = ? " +
                            "AND is_completed = FALSE " +
                            "AND start_date <= CURRENT_DATE " +
                            "AND (end_date IS NULL OR end_date >= CURRENT_DATE)";

            stmt = conn.prepareStatement(personalSql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            List<Integer> validChallengeIds = new ArrayList<>();
            while(rs.next()) {
                validChallengeIds.add(rs.getInt("challenge_id"));
            }
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);

            String insertLinkSql = "INSERT INTO personal_challenge_books (challenge_id, book_id) VALUES (?, ?) ON CONFLICT (challenge_id, book_id) DO NOTHING";
            String incrementSql = "UPDATE personal_challenges SET books_completed = books_completed + 1 WHERE challenge_id = ?";
            String checkCompleteSql = "UPDATE personal_challenges SET is_completed = TRUE WHERE challenge_id = ? AND books_completed >= target_books";

            for (Integer cId : validChallengeIds) {
                PreparedStatement insertStmt = conn.prepareStatement(insertLinkSql);
                insertStmt.setInt(1, cId);
                insertStmt.setInt(2, bookId);
                int rows = insertStmt.executeUpdate();
                ConnectionFactory.close(insertStmt);

                if (rows > 0) {
                    PreparedStatement incStmt = conn.prepareStatement(incrementSql);
                    incStmt.setInt(1, cId);
                    incStmt.executeUpdate();
                    ConnectionFactory.close(incStmt);

                    PreparedStatement compStmt = conn.prepareStatement(checkCompleteSql);
                    compStmt.setInt(1, cId);
                    compStmt.executeUpdate();
                    ConnectionFactory.close(compStmt);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }


    public boolean addBook(String title, int authorId, String genre, int year, int pages, String description) {
        String sql = "INSERT INTO books (title, author_id, genre, publication_year, page_count, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in addBook");
                return false;
            }

            stmt = conn.prepareStatement(sql);

            stmt.setString(1, title);
            stmt.setInt(2, authorId);

            if (genre != null && !genre.isEmpty()) {
                stmt.setString(3, genre);
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            if (year > 0) {
                stmt.setInt(4, year);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (pages > 0) {
                stmt.setInt(5, pages);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            if (description != null && !description.isEmpty()) {
                stmt.setString(6, description);
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            int rows = stmt.executeUpdate();
            System.out.println(">>> addBook: Added '" + title + "' (rows affected: " + rows + ")");
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addBook: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author_id = ?, genre = ?, publication_year = ?, " +
                "page_count = ?, description = ?, cover_image_url = ? WHERE book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in updateBook");
                return false;
            }

            stmt = conn.prepareStatement(sql);

            stmt.setString(1, book.getTitle());
            stmt.setInt(2, book.getAuthorId());

            if (book.getGenre() != null && !book.getGenre().isEmpty()) {
                stmt.setString(3, book.getGenre());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            if (book.getPublicationYear() > 0) {
                stmt.setInt(4, book.getPublicationYear());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (book.getPageCount() > 0) {
                stmt.setInt(5, book.getPageCount());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            if (book.getDescription() != null && !book.getDescription().isEmpty()) {
                stmt.setString(6, book.getDescription());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
                stmt.setString(7, book.getCoverImage());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            stmt.setInt(8, book.getBookId());

            int rows = stmt.executeUpdate();
            System.out.println(">>> updateBook: Updated '" + book.getTitle() + "' (ID: " + book.getBookId() +
                    ", rows affected: " + rows + ")");
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateBook: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in deleteBook");
                return false;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);

            int rows = stmt.executeUpdate();
            System.out.println(">>> deleteBook: Deleted book ID " + bookId + " (rows affected: " + rows + ")");
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deleteBook: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    public boolean addBookToUserList(int userId, int bookId, String status) {
        // First check if already exists
        if (isBookInUserList(userId, bookId)) {
            // Update existing
            return updateBookStatus(userId, bookId, status);
        }

        String sql;
        if ("READING".equals(status)) {
            sql = "INSERT INTO user_books (user_id, book_id, status, start_date) VALUES (?, ?, ?, CURRENT_DATE)";
        } else if ("COMPLETED".equals(status)) {
            sql = "INSERT INTO user_books (user_id, book_id, status, finish_date, current_page) " +
                    "VALUES (?, ?, ?, CURRENT_DATE, (SELECT page_count FROM books WHERE book_id = ?))";
        } else {
            sql = "INSERT INTO user_books (user_id, book_id, status) VALUES (?, ?, ?)";
        }

        System.out.println(">>> addBookToUserList: User " + userId + ", Book " + bookId + ", Status " + status);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database");
                return false;
            }

            if ("COMPLETED".equals(status)) {
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setInt(2, bookId);
                stmt.setString(3, status);
                stmt.setInt(4, bookId);
            } else {
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setInt(2, bookId);
                stmt.setString(3, status);
            }

            int rows = stmt.executeUpdate();
            System.out.println(">>>   Rows affected: " + rows);

            // Update challenges if completed
            if ("COMPLETED".equals(status)) {
                updateChallengesForCompletedBook(userId, bookId);
            }

            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>>   SQL ERROR: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    public boolean removeBookFromUserList(int userId, int bookId) {
        String sql = "DELETE FROM user_books WHERE user_id = ? AND book_id = ?";

        System.out.println(">>> removeBookFromUserList: User " + userId + ", Book " + bookId);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);

            int rows = stmt.executeUpdate();
            System.out.println(">>>   Rows affected: " + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR: " + e.getMessage());
            return false;
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    public boolean isBookInUserList(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM user_books WHERE user_id = ? AND book_id = ?";

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

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }


    public Book getBookById(int bookId) {
        String sql = "SELECT b.*, a.name as author_name, " +
                "COALESCE((SELECT COUNT(*) FROM reviews r WHERE r.book_id = b.book_id), 0) as review_count " +
                "FROM books b " +
                "JOIN authors a ON b.author_id = a.author_id " +
                "WHERE b.book_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: Could not connect to database in getBookById");
                return null;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                try {
                    book.setRatingCount(rs.getInt("review_count"));
                } catch (SQLException e) {
                    book.setRatingCount(0);
                }
                return book;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getBookById: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }


    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();

        book.setBookId(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthorId(rs.getInt("author_id"));
        book.setGenre(rs.getString("genre"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setPageCount(rs.getInt("page_count"));
        book.setDescription(rs.getString("description"));
        book.setAverageRating(rs.getDouble("average_rating"));

        try {
            book.setAuthorName(rs.getString("author_name"));
        } catch (SQLException e) {
            book.setAuthorName("Unknown");
        }


        try {
            book.setCoverImage(rs.getString("cover_image_url"));
        } catch (SQLException e) {
            //column doest exist
        }


        try {
            book.setIsbn(rs.getString("isbn"));
        } catch (SQLException e) {
            //column doesnt exist
        }

        book.setCurrentPage(0);
        book.setRatingCount(0);

        try {
            book.setReadingStatus(rs.getString("status"));
        } catch (SQLException e) {
            // status column not present
        }

        return book;

    }
}