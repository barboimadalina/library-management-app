package org.andrei.sample_project;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * UserAppChallenge Model
 * Represents a user's progress in an app challenge (achievement/badge).
 */
public class UserAppChallenge {

    private int id;
    private int userId;
    private int challengeId;
    private int currentProgress;
    private int targetProgress;
    private boolean completed;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String challengeName;
    private String challengeDescription;
    private String challengeType; // READING_GOAL, GENRE_EXPLORER, STREAK, etc.
    private String badgeName;
    private String badgeIcon;

    public UserAppChallenge() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }
    public void setTargetProgress(int targetProgress) {
        this.targetProgress = targetProgress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public void setChallengeName(String challengeName) {
        this.challengeName = challengeName;
    }

    public String getChallengeDescription() {
        return challengeDescription;
    }

    public void setChallengeDescription(String challengeDescription) {
        this.challengeDescription = challengeDescription;
    }

    public void setChallengeType(String challengeType) {
        this.challengeType = challengeType;
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

    /**
     * Gets progress percentage
     */
    public double getProgressPercent() {
        if (targetProgress <= 0) return 0;
        return (currentProgress * 100.0) / targetProgress;
    }

    /**
     * Gets progress display string
     */
    public String getProgressDisplay() {
        return currentProgress + "/" + targetProgress;
    }

    /**
     * Gets formatted completion date
     */
    public String getFormattedCompletionDate() {
        if (completedAt == null) return "";
        return completedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    /**
     * Gets completion date as string 
     */
    public String getCompletedDateString() {
        if (completedAt == null) return "Not completed";
        return completedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    /**
     * Gets started date as string
     */
    public String getStartedDateString() {
        if (startedAt == null) return "";
        return startedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    /**
     * Gets status display
     */
    public String getStatusDisplay() {
        if (completed) {
            return "✅ Completed";
        }
        return "🔄 In Progress (" + (int) getProgressPercent() + "%)";
    }

    @Override
    public String toString() {
        return "UserAppChallenge{" +
                "id=" + id +
                ", challengeName='" + challengeName + '\'' +
                ", progress=" + getProgressDisplay() +
                ", completed=" + completed +
                '}';
    }
}