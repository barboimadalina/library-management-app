package org.andrei.sample_project.repository;

import org.andrei.sample_project.User;

import org.andrei.sample_project.connection.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FollowRepository
 * Handles all database operations for user follows and privacy.

 */
public class FollowRepository {

    private static final String FOLLOWS_TABLE = "follows";
    private static final boolean HAS_STATUS_COLUMN = true;


    /**
     * Follows a user (sends follow request for private accounts)
     */
    public boolean followUser(int followerId, int followingId) {
        String sql;

        if (HAS_STATUS_COLUMN) {
            // Check if target user is private
            boolean isPrivate = isUserPrivate(followingId);
            String status = isPrivate ? "PENDING" : "ACCEPTED";

            sql = "INSERT INTO " + FOLLOWS_TABLE + " (follower_id, following_id, status) VALUES (?, ?, ?) " +
                    "ON CONFLICT (follower_id, following_id) DO UPDATE SET status = ?";

            System.out.println(">>> followUser: " + followerId + " -> " + followingId + " (status: " + status + ")");

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = ConnectionFactory.getConnection();
                if (conn == null) return false;

                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, followerId);
                stmt.setInt(2, followingId);
                stmt.setString(3, status);
                stmt.setString(4, status);

                int rows = stmt.executeUpdate();
                return rows > 0;

            } catch (SQLException e) {
                System.out.println(">>> ERROR in followUser: " + e.getMessage());
            } finally {
                ConnectionFactory.close(stmt);
                ConnectionFactory.close(conn);
            }
        } else {
            // Old schema without status
            sql = "INSERT INTO " + FOLLOWS_TABLE + " (follower_id, following_id) VALUES (?, ?) " +
                    "ON CONFLICT DO NOTHING";

            System.out.println(">>> followUser (legacy): " + followerId + " -> " + followingId);

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = ConnectionFactory.getConnection();
                if (conn == null) return false;

                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, followerId);
                stmt.setInt(2, followingId);

                stmt.executeUpdate();
                return true;

            } catch (SQLException e) {
                System.out.println(">>> ERROR in followUser: " + e.getMessage());
            } finally {
                ConnectionFactory.close(stmt);
                ConnectionFactory.close(conn);
            }
        }

        return false;
    }

    /**
     * Unfollows a user
     */
    public boolean unfollowUser(int followerId, int followingId) {
        String sql = "DELETE FROM " + FOLLOWS_TABLE + " WHERE follower_id = ? AND following_id = ?";

        System.out.println(">>> unfollowUser: " + followerId + " -> " + followingId);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in unfollowUser: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Checks if user is following another user
     */
    public boolean isFollowing(int followerId, int followingId) {
        String sql;

        if (HAS_STATUS_COLUMN) {
            sql = "SELECT 1 FROM " + FOLLOWS_TABLE + " WHERE follower_id = ? AND following_id = ? AND status = 'ACCEPTED'";
        } else {
            sql = "SELECT 1 FROM " + FOLLOWS_TABLE + " WHERE follower_id = ? AND following_id = ?";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);
            rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.out.println(">>> ERROR in isFollowing: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Gets follow status (PENDING, ACCEPTED, or null if not following)
     */
    public String getFollowStatus(int followerId, int followingId) {
        if (!HAS_STATUS_COLUMN) {
            // Old schema - if record exists, they're following
            return isFollowing(followerId, followingId) ? "ACCEPTED" : null;
        }

        String sql = "SELECT status FROM " + FOLLOWS_TABLE + " WHERE follower_id = ? AND following_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return null;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getFollowStatus: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return null;
    }

    /**
     * Accepts a follow request
     */
    public boolean acceptFollowRequest(int followerId, int followingId) {
        if (!HAS_STATUS_COLUMN) {
            return true; // No status column, already "accepted"
        }

        String sql = "UPDATE " + FOLLOWS_TABLE + " SET status = 'ACCEPTED' WHERE follower_id = ? AND following_id = ? AND status = 'PENDING'";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in acceptFollowRequest: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Rejects a follow request
     */
    public boolean rejectFollowRequest(int followerId, int followingId) {
        if (!HAS_STATUS_COLUMN) {
            return unfollowUser(followerId, followingId);
        }

        String sql = "DELETE FROM " + FOLLOWS_TABLE + " WHERE follower_id = ? AND following_id = ? AND status = 'PENDING'";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in rejectFollowRequest: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Gets pending follow requests for a user
     */
    public List<User> getPendingFollowRequests(int userId) {
        if (!HAS_STATUS_COLUMN) {
            return new ArrayList<>(); // No pending requests without status column
        }

        String sql = "SELECT u.* FROM users u " +
                "JOIN " + FOLLOWS_TABLE + " f ON u.user_id = f.follower_id " +
                "WHERE f.following_id = ? AND f.status = 'PENDING' " +
                "ORDER BY f.created_at DESC";

        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return users;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getPendingFollowRequests: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return users;
    }
    /**
     * Gets followers of a user
     */
    public List<User> getFollowers(int userId) {
        String sql;

        if (HAS_STATUS_COLUMN) {
            sql = "SELECT u.* FROM users u " +
                    "JOIN " + FOLLOWS_TABLE + " f ON u.user_id = f.follower_id " +
                    "WHERE f.following_id = ? AND f.status = 'ACCEPTED' " +
                    "ORDER BY u.full_name";
        } else {
            sql = "SELECT u.* FROM users u " +
                    "JOIN " + FOLLOWS_TABLE + " f ON u.user_id = f.follower_id " +
                    "WHERE f.following_id = ? " +
                    "ORDER BY u.full_name";
        }

        System.out.println(">>> getFollowers: " + userId);

        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return users;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getFollowers: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return users;
    }
    /**
     * Gets recent follow activity for a user
     * If user is private: shows pending requests
     * If user is public: shows recent accepted follows
     */
    public List<User> getRecentFollowActivity(int userId) {
        if (isUserPrivate(userId)) {
            // For private accounts: show pending requests
            return getPendingFollowRequests(userId);
        } else {
            // For public accounts: show recent followers
            String sql;
            if (HAS_STATUS_COLUMN) {
                sql = "SELECT u.* FROM users u " +
                        "JOIN " + FOLLOWS_TABLE + " f ON u.user_id = f.follower_id " +
                        "WHERE f.following_id = ? AND f.status = 'ACCEPTED' " +
                        "ORDER BY f.created_at DESC LIMIT 10";
            } else {
                sql = "SELECT u.* FROM users u " +
                        "JOIN " + FOLLOWS_TABLE + " f ON u.user_id = f.follower_id " +
                        "WHERE f.following_id = ? " +
                        "ORDER BY f.followed_at DESC LIMIT 10";
            }

            List<User> users = new ArrayList<>();
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = ConnectionFactory.getConnection();
                if (conn == null) return users;

                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }

            } catch (SQLException e) {
                System.out.println(">>> ERROR in getRecentFollowActivity: " + e.getMessage());
            } finally {
                ConnectionFactory.close(rs);
                ConnectionFactory.close(stmt);
                ConnectionFactory.close(conn);
            }

            return users;
        }
    }
    /**
     * Gets users that a user is following
     */
    public List<User> getFollowing(int userId) {
        String sql;

        if (HAS_STATUS_COLUMN) {
            sql = "SELECT u.* FROM users u " +
                    "JOIN " + FOLLOWS_TABLE + " f ON u.user_id = f.following_id " +
                    "WHERE f.follower_id = ? AND f.status = 'ACCEPTED' " +
                    "ORDER BY u.full_name";
        } else {
            sql = "SELECT u.* FROM users u " +
                    "JOIN " + FOLLOWS_TABLE + " f ON u.user_id = f.following_id " +
                    "WHERE f.follower_id = ? " +
                    "ORDER BY u.full_name";
        }

        System.out.println(">>> getFollowing: " + userId);

        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return users;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getFollowing: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return users;
    }

    /**
     * Checks if a user's account is private
     */
    public boolean isUserPrivate(int userId) {
        String sql = "SELECT is_private FROM users WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_private");
            }

        } catch (SQLException e) {
            // Column might not exist in old schema
            System.out.println(">>> Note: is_private column may not exist: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Sets user privacy status
     */
    public boolean setUserPrivacy(int userId, boolean isPrivate) {
        String sql = "UPDATE users SET is_private = ? WHERE user_id = ?";

        System.out.println(">>> setUserPrivacy: userId=" + userId + ", isPrivate=" + isPrivate);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, isPrivate);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in setUserPrivacy: " + e.getMessage());
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    /**
     * Searches users by name or username (excludes current user)
     */
    public List<User> searchUsers(String searchTerm, int excludeUserId) {
        String sql = "SELECT * FROM users " +
                "WHERE user_id != ? " +
                "AND (LOWER(full_name) LIKE LOWER(?) OR LOWER(username) LIKE LOWER(?)) " +
                "ORDER BY full_name LIMIT 20";

        System.out.println(">>> searchUsers: " + searchTerm);

        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return users;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, excludeUserId);
            String pattern = "%" + searchTerm + "%";
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in searchUsers: " + e.getMessage());
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return users;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setBio(rs.getString("bio"));

        try {
            user.setProfilePicture(rs.getString("profile_picture"));
        } catch (SQLException e) { /* Column doesn't exist */ }

        try {
            user.setPrivate(rs.getBoolean("is_private"));
        } catch (SQLException e) { /* Column doesn't exist */ }

        try {
            user.setAdmin(rs.getBoolean("is_admin"));
        } catch (SQLException e) {
            try {
                String role = rs.getString("role");
                user.setAdmin("ADMIN".equals(role));
            } catch (SQLException e2) { /* Neither column exists */ }
        }

        return user;
    }
}