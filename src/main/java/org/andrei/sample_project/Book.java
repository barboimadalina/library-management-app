package org.andrei.sample_project;

import java.net.URL;

/**
 * Book Model
 * Represents a book in the library system.
 */
public class Book {

    private int bookId;
    private String title;
    private int authorId;
    private String authorName;
    private String genre;
    private String description;
    private int publicationYear;
    private int pageCount;
    private String isbn;
    private double averageRating;
    private int ratingCount;
    private String coverImage;

    private String readingStatus; // TO_READ, READING, COMPLETED
    private int currentPage;
    private String startDate;
    private String finishDate;
    private static String cachedDefaultCoverImage;

    public Book() {}

    public Book(int bookId, String title, String authorName) {
        this.bookId = bookId;
        this.title = title;
        this.authorName = authorName;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getGenre() {
        return genre;
    }
    public String getProgressDisplay() {
        if (pageCount <= 0) {
            return "0%";
        }
        int percentage = (int) ((currentPage * 100.0) / pageCount);
        return percentage + "% (" + currentPage + "/" + pageCount + ")";}

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public void setReadingStatus(String readingStatus) {
        this.readingStatus = readingStatus;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }

    public String getSafeCoverImage() {
        return getDefaultCoverImage();
    }

    /**
     * Gets default cover image from resources
     */
    public String getDefaultCoverImage() {
        // Use cached value if available
        if (cachedDefaultCoverImage != null) {
            return cachedDefaultCoverImage;
        }

        try {

            URL defaultImage = getClass().getResource("/images/default-book-cover.png");
            if (defaultImage != null) {
                cachedDefaultCoverImage = defaultImage.toExternalForm();
                return cachedDefaultCoverImage;
            }

            defaultImage = getClass().getClassLoader().getResource("default-book-cover.png");
            if (defaultImage != null) {
                cachedDefaultCoverImage = defaultImage.toExternalForm();
                return cachedDefaultCoverImage;
            }

            String[] possiblePaths = {
                    "/images/default-book-cover.png",
                    "images/default-book-cover.png",
                    "default-book-cover.png"
            };

            for (String path : possiblePaths) {
                URL url = getClass().getResource(path);
                if (url != null) {
                    cachedDefaultCoverImage = url.toExternalForm();
                    return cachedDefaultCoverImage;
                }

                url = getClass().getClassLoader().getResource(path);
                if (url != null) {
                    cachedDefaultCoverImage = url.toExternalForm();
                    return cachedDefaultCoverImage;
                }
            }


            System.out.println(">>> WARNING: Default book cover image not found in resources");
            cachedDefaultCoverImage = null;
            return null;

        } catch (Exception e) {
            System.out.println(">>> ERROR getting default cover image: " + e.getMessage());
            cachedDefaultCoverImage = null;
            return null;
        }
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", authorName='" + authorName + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return bookId == book.bookId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(bookId);
    }
}