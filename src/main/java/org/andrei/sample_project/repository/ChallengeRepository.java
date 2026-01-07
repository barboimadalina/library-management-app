package org.andrei.sample_project.repository;

import org.andrei.sample_project.connection.ConnectionFactory;
import org.andrei.sample_project.AppChallenge;
import org.andrei.sample_project.PersonalChallenge;
import org.andrei.sample_project.UserAppChallenge;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ChallengeRepository
 */
public class ChallengeRepository {
    public List<AppChallenge> getActiveAppChallenges(int userId) {
        String sql =
                "SELECT ac.challenge_id, " +
                        "       ac.name, " +
                        "       ac.description, " +
                        "       ac.challenge_type, " +
                        "       ac.target, " +
                        "       ac.required_genre, " +
                        "       ac.start_date, " +
                        "       ac.end_date, " +
                        "       ac.badge_name, " +
                        "       ac.badge_icon, " +
                        "       ac.is_active, " +
                        "       COALESCE(uc.user_id, 0) AS has_joined, " +
                        "       COALESCE(uc.completed, false) AS has_completed, " +
                        "       COALESCE(uc.current_progress, 0) AS user_progress, " +
                        "       (SELECT COUNT(*) " +
                        "          FROM user_app_challenges " +
                        "          WHERE challenge_id = ac.challenge_id) AS participant_count " +
                        "FROM app_challenges ac " +
                        "LEFT JOIN user_app_challenges uc " +
                        "       ON ac.challenge_id = uc.challenge_id " +
                        "      AND uc.user_id = ? " +
                        "WHERE ac.is_active = TRUE " +
                        "ORDER BY ac.start_date DESC";

        System.out.println(">>> getActiveAppChallenges: userId=" + userId);

        List<AppChallenge> challenges = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return challenges;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                challenges.add(extractAppChallengeFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getActiveAppChallenges: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return challenges;
    }

    public boolean joinAppChallenge(int userId, int challengeId) {
        String sql = "INSERT INTO user_app_challenges (user_id, challenge_id, current_progress, completed, started_at) " +
                "VALUES (?, ?, 0, FALSE, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (user_id, challenge_id) DO NOTHING";

        System.out.println(">>> joinAppChallenge: userId=" + userId + ", challengeId=" + challengeId);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in joinAppChallenge: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }
    public List<PersonalChallenge> getUserPersonalChallenges(int userId) {
        String sql = "SELECT pc.*, " +
                "COALESCE((SELECT COUNT(*) FROM personal_challenge_books WHERE challenge_id = pc.challenge_id), 0) as books_completed " +
                "FROM personal_challenges pc " +
                "WHERE pc.user_id = ? " +
                "ORDER BY pc.created_at DESC";

        System.out.println(">>> getUserPersonalChallenges: userId=" + userId);

        List<PersonalChallenge> challenges = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return challenges;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                challenges.add(extractPersonalChallengeFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserPersonalChallenges: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return challenges;
    }

    public int createPersonalChallenge(int userId, String title, String description,
                                       int targetBooks, LocalDate startDate, LocalDate endDate) {
        String sql = "INSERT INTO personal_challenges " +
                "(user_id, title, description, target_books, start_date, end_date, is_completed, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP) " +
                "RETURNING challenge_id";

        System.out.println(">>> createPersonalChallenge: " + title);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return -1;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, targetBooks);
            stmt.setDate(5, Date.valueOf(startDate));
            stmt.setDate(6, Date.valueOf(endDate));

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("challenge_id");
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in createPersonalChallenge: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return -1;
    }

    public boolean deletePersonalChallenge(int challengeId) {
        String deleteBooksSql = "DELETE FROM personal_challenge_books WHERE challenge_id = ?";
        String deleteChallengeSql = "DELETE FROM personal_challenges WHERE challenge_id = ?";

        System.out.println(">>> deletePersonalChallenge: challengeId=" + challengeId);

        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt1 = conn.prepareStatement(deleteBooksSql);
            stmt1.setInt(1, challengeId);
            stmt1.executeUpdate();

            stmt2 = conn.prepareStatement(deleteChallengeSql);
            stmt2.setInt(1, challengeId);
            int rows = stmt2.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deletePersonalChallenge: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(stmt1);
            ConnectionFactory.close(stmt2);
            ConnectionFactory.close(conn);
        }

        return false;
    }
    public List<UserAppChallenge> getUserCompletedAppChallenges(int userId) {
        String sql = "SELECT uc.*, ac.name as challenge_name, ac.description as challenge_description, " +
                "ac.challenge_type, ac.badge_name, ac.badge_icon, ac.target as target_progress " +  // REMOVED xp_reward
                "FROM user_app_challenges uc " +
                "JOIN app_challenges ac ON uc.challenge_id = ac.challenge_id " +
                "WHERE uc.user_id = ? AND uc.completed = true " +
                "ORDER BY uc.completed_at DESC";

        System.out.println(">>> getUserCompletedAppChallenges: userId=" + userId);

        List<UserAppChallenge> challenges = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return challenges;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                challenges.add(extractUserAppChallengeFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserCompletedAppChallenges: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(rs);
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return challenges;
    }
    public boolean updateChallengeProgress(int userId, int challengeId, int newProgress) {
        String sql = "UPDATE user_app_challenges SET current_progress = ? WHERE user_id = ? AND challenge_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newProgress);
            stmt.setInt(2, userId);
            stmt.setInt(3, challengeId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                checkAndCompleteChallenge(userId, challengeId);
            }

            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateChallengeProgress: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    public boolean incrementChallengeProgress(int userId, int challengeId) {
        String sql = "UPDATE user_app_challenges SET current_progress = current_progress + 1 " +
                "WHERE user_id = ? AND challenge_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return false;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                checkAndCompleteChallenge(userId, challengeId);
            }

            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in incrementChallengeProgress: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }

        return false;
    }

    private void checkAndCompleteChallenge(int userId, int challengeId) {
        String sql = "UPDATE user_app_challenges uc SET completed = true, completed_at = CURRENT_TIMESTAMP " +
                "FROM app_challenges ac " +
                "WHERE uc.challenge_id = ac.challenge_id " +
                "AND uc.user_id = ? AND uc.challenge_id = ? " +
                "AND uc.current_progress >= ac.target AND uc.completed = false";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = ConnectionFactory.getConnection();
            if (conn == null) return;

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, challengeId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(">>> ERROR in checkAndCompleteChallenge: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(conn);
        }
    }
    private AppChallenge extractAppChallengeFromResultSet(ResultSet rs) throws SQLException {
        AppChallenge challenge = new AppChallenge();

        challenge.setChallengeId(rs.getInt("challenge_id"));
        challenge.setTitle(rs.getString("name"));  // Database column is 'name' not 'title'
        challenge.setDescription(rs.getString("description"));
        challenge.setChallengeType(rs.getString("challenge_type"));
        challenge.setTargetBooks(rs.getInt("target"));
        challenge.setRequiredGenre(rs.getString("required_genre"));

        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");
        if (startDate != null) challenge.setStartDate(startDate.toLocalDate());
        if (endDate != null) challenge.setEndDate(endDate.toLocalDate());

        challenge.setBadgeName(rs.getString("badge_name"));
        challenge.setBadgeIcon(rs.getString("badge_icon"));
        challenge.setActive(rs.getBoolean("is_active"));

        challenge.setUserJoined(rs.getInt("has_joined") > 0);
        challenge.setUserCompleted(rs.getBoolean("has_completed"));
        challenge.setUserBooksCompleted(rs.getInt("user_progress"));
        challenge.setTotalParticipants(rs.getInt("participant_count"));

        return challenge;
    }

    private PersonalChallenge extractPersonalChallengeFromResultSet(ResultSet rs) throws SQLException {
        PersonalChallenge challenge = new PersonalChallenge();

        challenge.setChallengeId(rs.getInt("challenge_id"));
        challenge.setUserId(rs.getInt("user_id"));
        challenge.setTitle(rs.getString("title"));
        challenge.setDescription(rs.getString("description"));
        challenge.setTargetBooks(rs.getInt("target_books"));
        challenge.setBooksCompleted(rs.getInt("books_completed"));

        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");
        if (startDate != null) challenge.setStartDate(startDate.toLocalDate());
        if (endDate != null) challenge.setEndDate(endDate.toLocalDate());

        challenge.setCompleted(rs.getBoolean("is_completed"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            challenge.setCreatedAt(createdAt.toLocalDateTime());
        }

        return challenge;
    }

    private UserAppChallenge extractUserAppChallengeFromResultSet(ResultSet rs) throws SQLException {
        UserAppChallenge challenge = new UserAppChallenge();

        challenge.setId(rs.getInt("id"));
        challenge.setUserId(rs.getInt("user_id"));
        challenge.setChallengeId(rs.getInt("challenge_id"));
        challenge.setCurrentProgress(rs.getInt("current_progress"));
        challenge.setTargetProgress(rs.getInt("target_progress"));
        challenge.setCompleted(rs.getBoolean("completed"));

        Timestamp startedAt = rs.getTimestamp("started_at");
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (startedAt != null) challenge.setStartedAt(startedAt.toLocalDateTime());
        if (completedAt != null) challenge.setCompletedAt(completedAt.toLocalDateTime());

        challenge.setChallengeName(rs.getString("challenge_name"));
        challenge.setChallengeDescription(rs.getString("challenge_description"));
        challenge.setChallengeType(rs.getString("challenge_type"));
        challenge.setBadgeName(rs.getString("badge_name"));
        challenge.setBadgeIcon(rs.getString("badge_icon"));


        return challenge;
    }
}