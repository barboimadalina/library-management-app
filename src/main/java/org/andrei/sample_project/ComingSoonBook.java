package org.andrei.sample_project;

import java.time.LocalDate;

public class ComingSoonBook {
    private int id;
    private String title;
    private String author;
    private String genre;
    private String description;
    private LocalDate releaseDate;
    private String status; // UPCOMING, RELEASED, CANCELLED
    private String coverImage;

    public ComingSoonBook() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getStatusDisplay() {
        if (status == null) return "UPCOMING";
        switch (status) {
            case "UPCOMING": return "🔜 Upcoming";
            case "RELEASED": return "✅ Released";
            case "CANCELLED": return "❌ Cancelled";
            default: return status;
        }
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + getStatusDisplay() + ")";
    }
}