package org.andrei.sample_project.repository;

import org.andrei.sample_project.User;
import org.andrei.sample_project.connection.ConnectionFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserRepository
 * Handles all database operations for users with BCrypt password hashing and privacy settings.
 */
public class UserRepository {

    // ==========================================
    // AUTHENTICATION (WITH BCRYPT)
    // ==========================================

    /**
     * Login method - called by LoginController
     */
    public User login(String usernameOrEmail, String password) {
        return authenticate(usernameOrEmail, password);
    }

    /**
     * Authenticates a user with username/email and password using BCrypt
     */
    public User authenticate(String usernameOrEmail, String password) {
        // Step 1: Get the user by username or email
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";

        System.out.println(">>> authenticate: " + usernameOrEmail);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) {
                System.out.println(">>> ERROR: No database connection");
                return null;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // Step 2: Get the stored BCrypt hash from database
                String storedHash = rs.getString("password_hash");
                System.out.println(">>> Found user, checking password...");

                // Step 3: Verify the password using BCrypt
                if (storedHash != null && BCrypt.checkpw(password, storedHash)) {
                    // Step 4: Password is correct - create User object
                    User user = extractUserFromResultSet(rs);
                    System.out.println(">>> ✓ Authentication successful for: " + user.getUsername());
                    System.out.println(">>> Privacy setting: " + (user.isPrivate() ? "🔒 Private" : "🌍 Public"));

                    // Step 5: Update last login time
                    updateLastLogin(user.getUserId());

                    return user;
                } else {
                    // Step 6: Password is wrong
                    System.out.println(">>> ✗ Password incorrect for: " + usernameOrEmail);

                    // DEBUG: Try to see what's happening
                    if (storedHash == null) {
                        System.out.println(">>>   WARNING: storedHash is NULL!");
                    } else if (!storedHash.startsWith("$2a$")) {
                        System.out.println(">>>   WARNING: storedHash doesn't look like BCrypt!");
                        System.out.println(">>>   Hash starts with: " + storedHash.substring(0, Math.min(10, storedHash.length())));
                    }
                }
            } else {
                System.out.println(">>> ✗ No user found with username/email: " + usernameOrEmail);
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in authenticate: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Registers a new user with BCrypt password hashing and privacy setting
     */
    public boolean register(String username, String email, String password, String fullName, boolean isPrivate) {
        // Check if username or email already exists
        if (usernameExists(username)) {
            System.out.println(">>> Username already exists: " + username);
            return false;
        }

        if (emailExists(email)) {
            System.out.println(">>> Email already exists: " + email);
            return false;
        }

        // Hash the password with BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println(">>> Registering: " + username);
        System.out.println(">>> Privacy setting: " + (isPrivate ? "🔒 Private" : "🌍 Public"));

        // Try with role and is_private columns
        String sql = "INSERT INTO users (username, email, password_hash, full_name, role, is_private) VALUES (?, ?, ?, ?, 'USER', ?) RETURNING user_id";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, fullName);
            stmt.setBoolean(5, isPrivate);

            rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println(">>> User registered successfully: " + username);
                return true;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in register (with privacy): " + e.getMessage());
            e.printStackTrace();

            // Try without is_private column (backward compatibility)
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();

                String sqlNoPrivacy = "INSERT INTO users (username, email, password_hash, full_name, role) VALUES (?, ?, ?, ?, 'USER')";
                stmt = conn.prepareStatement(sqlNoPrivacy);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);
                stmt.setString(4, fullName);

                int rows = stmt.executeUpdate();
                System.out.println(">>> Registered without privacy column (backward compatible)");
                return rows > 0;

            } catch (SQLException e2) {
                System.out.println(">>> ERROR in register (backward compatible): " + e2.getMessage());
                e2.printStackTrace();
                return false;
            }

        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Register without privacy setting (backward compatibility - defaults to public)
     */
    public boolean register(String username, String email, String password, String fullName) {
        return register(username, email, password, fullName, false);
    }

    // ==========================================
    // GET OPERATIONS
    // ==========================================

    /**
     * Gets a user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return null;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserById: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Gets a user by username
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return null;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserByUsername: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Checks if username exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.out.println(">>> ERROR in usernameExists: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Checks if email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.out.println(">>> ERROR in emailExists: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Gets all users (for admin)
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY full_name";

        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return users;

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAllUsers: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return users;
    }

    // ==========================================
    // FOLLOWER COUNTS
    // ==========================================

    /**
     * Gets follower count for a user
     */
    public int getFollowersCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM follows WHERE following_id = ? AND status = 'ACCEPTED'";

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
            // Try old 'followers' table
            return getFollowersCountLegacy(userId);
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }

    private int getFollowersCountLegacy(int userId) {
        String sql = "SELECT COUNT(*) as count FROM followers WHERE following_id = ?";

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
            System.out.println(">>> ERROR in getFollowersCountLegacy: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }

    /**
     * Gets following count for a user
     */
    public int getFollowingCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM follows WHERE follower_id = ? AND status = 'ACCEPTED'";

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
            // Try old 'followers' table
            return getFollowingCountLegacy(userId);
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }

    private int getFollowingCountLegacy(int userId) {
        String sql = "SELECT COUNT(*) as count FROM followers WHERE follower_id = ?";

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
            System.out.println(">>> ERROR in getFollowingCountLegacy: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return 0;
    }

    // ==========================================
    // UPDATE OPERATIONS
    // ==========================================

    /**
     * Updates user's last login timestamp
     */
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            // Column might not exist in old schema - ignore
            System.out.println(">>> Note: last_login update skipped (column may not exist)");
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    /**
     * Updates user's bio
     */
    public boolean updateUserBio(int userId, String bio) {
        String sql = "UPDATE users SET bio = ? WHERE user_id = ?";

        System.out.println(">>> updateUserBio: " + userId);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, bio);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateUserBio: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Updates user's profile picture
     */
    public boolean updateProfilePicture(int userId, String picturePath) {
        String sql = "UPDATE users SET profile_picture = ? WHERE user_id = ?";

        System.out.println(">>> updateProfilePicture: " + userId);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, picturePath);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateProfilePicture: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Updates user's full name
     */
    public boolean updateFullName(int userId, String fullName) {
        String sql = "UPDATE users SET full_name = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateFullName: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Updates user's email
     */
    public boolean updateEmail(int userId, String email) {
        String sql = "UPDATE users SET email = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateEmail: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Updates user's password with BCrypt hashing
     */
    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        // First verify old password
        String verifySql = "SELECT password_hash FROM users WHERE user_id = ?";
        String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement verifyStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            // Get stored hash
            verifyStmt = conn.prepareStatement(verifySql);
            verifyStmt.setInt(1, userId);
            rs = verifyStmt.executeQuery();

            if (!rs.next()) {
                System.out.println(">>> User not found");
                return false;
            }

            String storedHash = rs.getString("password_hash");

            // Verify old password with BCrypt
            if (!BCrypt.checkpw(oldPassword, storedHash)) {
                System.out.println(">>> Old password incorrect");
                return false;
            }

            // Hash new password with BCrypt
            String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            // Update to new password
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, newHashedPassword);
            updateStmt.setInt(2, userId);

            int rows = updateStmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updatePassword: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(verifyStmt);
            ConnectionFactory.close(updateStmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Updates user's privacy setting
     */
    public boolean updatePrivacy(int userId, boolean isPrivate) {
        String sql = "UPDATE users SET is_private = ? WHERE user_id = ?";

        System.out.println(">>> updatePrivacy: " + userId + " to " + (isPrivate ? "🔒 Private" : "🌍 Public"));

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, isPrivate);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(">>> Privacy updated successfully");
                return true;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updatePrivacy: " + e.getMessage());
            System.out.println(">>> Column 'is_private' may not exist in database");
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * DEBUG method: Check user password hash
     */
    public void debugUserPassword(String username) {
        String sql = "SELECT username, password_hash FROM users WHERE username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("=== DEBUG: " + username + " ===");
                String hash = rs.getString("password_hash");
                System.out.println("Hash: " + hash);

                if (hash != null) {
                    System.out.println("Length: " + hash.length());
                    System.out.println("Is BCrypt: " + hash.startsWith("$2a$"));

                    // Test common passwords
                    String[] testPasswords = {"password123", "password", "admin", "123456"};
                    for (String testPwd : testPasswords) {
                        try {
                            boolean matches = BCrypt.checkpw(testPwd, hash);
                            System.out.println("Test '" + testPwd + "': " + (matches ? "✓ MATCH" : "✗ NO MATCH"));
                        } catch (Exception e) {
                            System.out.println("Test '" + testPwd + "': Error - " + e.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Debug error: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }

    // ==========================================
    // DELETE OPERATIONS
    // ==========================================

    /**
     * Deletes a user (admin only)
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deleteUser: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));

        // Get bio (may be null)
        try {
            user.setBio(rs.getString("bio"));
        } catch (SQLException e) {
            user.setBio(null);
        }

        // Check for admin status - try multiple column names
        boolean isAdmin = false;

        // Try 'is_admin' column first (boolean)
        try {
            isAdmin = rs.getBoolean("is_admin");
        } catch (SQLException e1) {
            // Try 'role' column (string)
            try {
                String role = rs.getString("role");
                isAdmin = "ADMIN".equalsIgnoreCase(role);
            } catch (SQLException e2) {
                // No admin column found - default to false
                isAdmin = false;
            }
        }

        user.setAdmin(isAdmin);

        // Get privacy setting - may not exist in old schema
        try {
            boolean isPrivate = rs.getBoolean("is_private");
            user.setPrivate(isPrivate);
        } catch (SQLException e) {
            // Column doesn't exist - default to public
            user.setPrivate(false);
        }

        // New columns - may not exist in old schema
        try {
            user.setProfilePicture(rs.getString("profile_picture"));
        } catch (SQLException e) {
            // Column doesn't exist
        }

        // Timestamps
        try {
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                user.setCreatedAt(createdAt.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Column might not exist
        }

        try {
            Timestamp lastLogin = rs.getTimestamp("last_login");
            if (lastLogin != null) {
                user.setLastLogin(lastLogin.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Column might not exist
        }

        return user;
    }
}