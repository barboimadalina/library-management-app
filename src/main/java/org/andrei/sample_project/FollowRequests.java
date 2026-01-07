package org.andrei.sample_project;

import java.time.LocalDateTime;

/**
 * FollowRequest Model Class
 * Represents a pending follow request between users.
 * Used for private accounts that require approval to follow.
 */
public class FollowRequests {

    private int requestId;
    private int requesterId;
    private int targetId;
    private String status;  // PENDING, ACCEPTED, DECLINED
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private String requesterFullName;
    private String targetFullName;

    public FollowRequests() {
        this.status = "PENDING";
    }

    public FollowRequests(int requesterId, int targetId) {
        this();
        this.requesterId = requesterId;
        this.targetId = targetId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return requesterFullName + " → " + targetFullName + " (" + status + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowRequests that = (FollowRequests) o;
        return requestId == that.requestId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(requestId);
    }
}