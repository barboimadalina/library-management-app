package org.andrei.sample_project;

import java.time.LocalDate;


/**
 * Author Model
 * Represents an author with profile information and book count.
 */
public class Author {

    private int authorId;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String nationality;
    private String website;
    private String profileImageUrl;
    private int bookCount;
    public Author() {}
    public Author(int authorId, String name) {
        this.authorId = authorId;
        this.name = name;
    }

    public Author(int authorId, String name, String biography) {
        this.authorId = authorId;
        this.name = name;
        this.biography = biography;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }


    public String getLifeSpan() {
        if (birthDate == null) return "";

        String birth = String.valueOf(birthDate.getYear());
        String death = (deathDate != null) ? String.valueOf(deathDate.getYear()) : "present";

        return birth + " - " + death;
    }

    /**
     * Gets initials for avatar display
     */
    public String getInitials() {
        if (name == null || name.isEmpty()) return "A";

        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    /**
     * Gets book count display string
     */
    public String getBookCountDisplay() {
        if (bookCount == 0) return "No books";
        if (bookCount == 1) return "1 book";
        return bookCount + " books";
    }

    @Override
    public String toString() {
        return "Author{" +
                "authorId=" + authorId +
                ", name='" + name + '\'' +
                ", bookCount=" + bookCount +
                '}';
    }
}