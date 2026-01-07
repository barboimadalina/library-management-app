package org.andrei.sample_project;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Review Model
 * Represents a book review by a user.
 */
public class Review {

    private int reviewId;
    private int userId;
    private int bookId;
    private int rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private String userFullName;
    private String bookTitle;
    public Review() {}

    public Review(int userId, int bookId, int rating) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
    }

    public Review(int userId, int bookId, int rating, String reviewText) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    /**
     * Gets rating as star string
     */
    public String getRatingStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            sb.append("⭐");
        }
        return sb.toString();
    }

    /**
     * Gets formatted date string
     */
    public String getFormattedDate() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }
    /**
     * Gets review preview
     */
    public String getReviewPreview() {
        if (reviewText == null || reviewText.isEmpty()) {
            return "";
        }
        if (reviewText.length() <= 80) {
            return reviewText;
        }
        return reviewText.substring(0, 77) + "...";
    }

    /**
     * Checks if review has text
     */
    public boolean hasReviewText() {
        return reviewText != null && !reviewText.trim().isEmpty();
    }

    /**
     * Checks if review was edited
     */
    public boolean wasEdited() {
        return updatedAt != null && createdAt != null && !updatedAt.equals(createdAt);
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", rating=" + rating +
                '}';
    }


}