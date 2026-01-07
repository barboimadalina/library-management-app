package org.andrei.sample_project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * AppChallenge Model Class
 * Represents a system-wide challenge created by admins
 */
public class AppChallenge {

    private int challengeId;
    private String title;
    private String description;
    private String challengeType;
    private int targetBooks;
    private String requiredGenre;
    private LocalDate startDate;
    private LocalDate endDate;
    private String badgeName;
    private String badgeIcon;
    private boolean isActive;
    private LocalDateTime createdAt;

    private int userBooksCompleted;
    private boolean userJoined;
    private boolean userCompleted;

    private int totalParticipants;
    public AppChallenge() {
        this.userBooksCompleted = 0;
        this.userJoined = false;
        this.userCompleted = false;
    }

    public AppChallenge(String title, String challengeType, int targetBooks, LocalDate startDate, LocalDate endDate) {
        this();
        this.title = title;
        this.challengeType = challengeType;
        this.targetBooks = targetBooks;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(String challengeType) {
        this.challengeType = challengeType;
    }

    public int getTargetBooks() {
        return targetBooks;
    }

    public void setTargetBooks(int targetBooks) {
        this.targetBooks = targetBooks;
    }

    public String getRequiredGenre() {
        return requiredGenre;
    }

    public void setRequiredGenre(String requiredGenre) {
        this.requiredGenre = requiredGenre;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getBadgeIcon() {
        return badgeIcon;
    }

    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getUserBooksCompleted() {
        return userBooksCompleted;
    }

    public void setUserBooksCompleted(int userBooksCompleted) {
        this.userBooksCompleted = userBooksCompleted;
    }

    public boolean isUserJoined() {
        return userJoined;
    }

    public void setUserJoined(boolean userJoined) {
        this.userJoined = userJoined;
    }

    public boolean isUserCompleted() {
        return userCompleted;
    }

    public void setUserCompleted(boolean userCompleted) {
        this.userCompleted = userCompleted;
    }

    public int getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(int totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    /**
     * Returns progress as percentage (0-100) for current user
     */
    public double getUserProgressPercentage() {
        if (targetBooks <= 0) return 0;
        return Math.min(100.0, (userBooksCompleted * 100.0) / targetBooks);
    }

    /**
     * Returns user progress string (e.g., "2/5 books")
     */
    public String getUserProgressString() {
        return String.format("%d/%d books", userBooksCompleted, targetBooks);
    }

    /**
     * Returns days remaining until deadline
     */
    public long getDaysRemaining() {
        if (endDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) return 0;
        return ChronoUnit.DAYS.between(today, endDate);
    }

    /**
     * Returns formatted days remaining string
     */
    public String getDaysRemainingString() {
        long days = getDaysRemaining();
        if (days == 0) {
            if (LocalDate.now().isAfter(endDate)) {
                return "Ended";
            }
            return "Last day!";
        } else if (days == 1) {
            return "1 day left";
        }
        return days + " days left";
    }

    /**
     * Returns date range string
     */
    public String getDateRangeString() {
        if (startDate == null || endDate == null) return "No dates set";
        return startDate.toString() + " to " + endDate.toString();
    }

    /**
     * Checks if the challenge is currently running
     */
    public boolean isCurrentlyActive() {
        if (!isActive) return false;
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Checks if challenge has ended
     */
    public boolean hasEnded() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Checks if challenge hasn't started yet
     */
    public boolean isUpcoming() {
        return LocalDate.now().isBefore(startDate);
    }

    /**
     * Returns challenge type as formatted string
     */
    public String getChallengeTypeDisplay() {
        if (challengeType == null) return "Challenge";
        switch (challengeType) {
            case "SEASONAL": return "🌸 Seasonal";
            case "GENRE": return "📚 Genre Challenge";
            case "READING_GOAL": return "🎯 Reading Goal";
            case "EVENT": return "🎉 Special Event";
            default: return challengeType;
        }
    }

    /**
     * Returns genre requirement display
     */
    public String getGenreRequirementDisplay() {
        if (requiredGenre == null || requiredGenre.isEmpty()) {
            return "Any genre";
        }
        return requiredGenre + " books only";
    }

    /**
     * Returns status string for display
     */
    public String getStatusString() {
        if (userCompleted) return "✅ Completed!";
        if (hasEnded()) return "⏰ Ended";
        if (isUpcoming()) return "⏳ Coming Soon";
        if (userJoined) return "🔥 In Progress";
        return "📋 Not Joined";
    }

    /**
     * Returns badge display string
     */
    public String getBadgeDisplay() {
        if (badgeIcon != null && badgeName != null) {
            return badgeIcon + " " + badgeName;
        } else if (badgeName != null) {
            return "🏆 " + badgeName;
        }
        return "🏆 Badge";
    }

    /**
     * Returns participant count string
     */
    public String getParticipantsString() {
        if (totalParticipants == 1) {
            return "1 participant";
        }
        return totalParticipants + " participants";
    }

    @Override
    public String toString() {
        return (badgeIcon != null ? badgeIcon + " " : "") + title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppChallenge that = (AppChallenge) o;
        return challengeId == that.challengeId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(challengeId);
    }
}