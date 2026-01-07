package org.andrei.sample_project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * PersonalChallenge Model Class
 * Represents a user-created reading challenge
 */
public class PersonalChallenge {

    private int challengeId;
    private int userId;
    private String title;
    private String description;
    private int targetBooks;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private boolean isCompleted;
    private int booksCompleted;
    private List<Book> completedBooks;

    public PersonalChallenge() {
        this.completedBooks = new ArrayList<>();
        this.booksCompleted = 0;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public int getTargetBooks() {
        return targetBooks;
    }

    public void setTargetBooks(int targetBooks) {
        this.targetBooks = targetBooks;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getBooksCompleted() {
        return booksCompleted;
    }

    public void setBooksCompleted(int booksCompleted) {
        this.booksCompleted = booksCompleted;
    }

    /**
     * Returns progress as percentage
     */
    public double getProgressPercentage() {
        if (targetBooks <= 0) return 0;
        return Math.min(100.0, (booksCompleted * 100.0) / targetBooks);
    }

    /**
     * Returns progress string
     */
    public String getProgressString() {
        return String.format("%d/%d books (%.0f%%)", booksCompleted, targetBooks, getProgressPercentage());
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
     * Returns date range string (e.g., "Jan 1 - Dec 31, 2025")
     */
    public String getDateRangeString() {
        if (startDate == null || endDate == null) return "No dates set";
        return startDate.toString() + " to " + endDate.toString();
    }

    /**
     * Checks if the challenge has expired (past end date and not completed)
     */
    public boolean isExpired() {
        if (isCompleted) return false;
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Checks if the challenge hasn't started yet
     */
    public boolean isUpcoming() {
        return LocalDate.now().isBefore(startDate);
    }

    /**
     * Returns status string for display
     */
    public String getStatusString() {
        if (isCompleted) return "✅ Completed";
        if (isExpired()) return "❌ Expired";
        if (isUpcoming()) return "⏳ Upcoming";
        return "🔥 Active";
    }

    /**
     * Returns status emoji
     */
    public String getStatusEmoji() {
        if (isCompleted) return "✅";
        if (isExpired()) return "❌";
        if (isUpcoming()) return "⏳";
        return "🔥";
    }

    @Override
    public String toString() {
        return title + " - " + getProgressString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalChallenge that = (PersonalChallenge) o;
        return challengeId == that.challengeId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(challengeId);
    }
}