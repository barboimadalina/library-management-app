package org.andrei.sample_project;

import java.time.LocalDateTime;

/**
 * User Model
 * Represents a user in the library system with profile, privacy, and activity settings.
 */
public class User {

    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String bio;
    private String profilePicture;
    private String role;
    private String joinDate;
    private int booksRead;
    private boolean isPrivate;
    private boolean isAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;


    public User() {
        this.isPrivate = false;
        this.isAdmin = false;
        this.role = "USER";
        this.booksRead = 0;
    }

    public User(int userId, String username, String fullName) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.isPrivate = false;
        this.isAdmin = false;
        this.role = "USER";
        this.booksRead = 0;
    }


    public User(int userId, String username, String email,
                String fullName, String role, boolean isPrivate) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.isPrivate = isPrivate;
        this.isAdmin = role.equalsIgnoreCase("ADMIN");
        this.booksRead = 0;
    }

    public User(String username, String email, String passwordHash, String fullName) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.isPrivate = false;
        this.isAdmin = false;
        this.role = "USER";
        this.booksRead = 0;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getBooksRead() {
        return booksRead;
    }
    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getJoinDate() {
        if (joinDate != null) {
            return joinDate;
        }
        return getMemberSince();
    }



    public void setBooksRead(int booksRead) {
        this.booksRead = booksRead;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean isAdmin() {
        return isAdmin || (role != null && role.equalsIgnoreCase("ADMIN"));
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        if (isAdmin && (role == null || !role.equalsIgnoreCase("ADMIN"))) {
            this.role = "ADMIN";
        }
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    /**
     * Gets initials for avatar display
     */
    public String getInitials() {
        if (fullName == null || fullName.isEmpty()) {
            return username != null && !username.isEmpty()
                    ? username.substring(0, Math.min(2, username.length())).toUpperCase()
                    : "U";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    /**
     * Gets privacy display string
     */
    public String getPrivacyDisplay() {
        return isPrivate ? "🔒 Private account" : "🌍 Public account";
    }

    /**
     * Checks if user has a profile picture
     */
    public boolean hasProfilePicture() {
        return profilePicture != null && !profilePicture.isEmpty();
    }

    /**
     * Gets display name (full name or username)
     */
    public String getDisplayName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        return username != null ? username : "Unknown User";
    }

    /**
     * Gets member since date formatted
     */
    public String getMemberSince() {
        if (createdAt != null) {
            return createdAt.toLocalDate().toString();
        }
        if (joinDate != null) {
            return joinDate;
        }
        return "Unknown";
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isPrivate=" + isPrivate +
                ", booksRead=" + booksRead +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }
}