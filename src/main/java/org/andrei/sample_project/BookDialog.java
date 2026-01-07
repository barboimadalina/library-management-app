package org.andrei.sample_project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.andrei.sample_project.Author;
import org.andrei.sample_project.Book;
import org.andrei.sample_project.Review;
import org.andrei.sample_project.User;
import org.andrei.sample_project.AuthorController;
import org.andrei.sample_project.repository.*;
import javafx.stage.Window;
import java.util.List;

/**
 * Reusable book dialog component that displays book details with actions.
 */
public class BookDialog {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final User currentUser;
    private Stage currentStage;


    public BookDialog(User currentUser) {
        this.currentUser = currentUser;
        this.bookRepository = new BookRepository();
        this.authorRepository = new AuthorRepository();
        this.favoriteRepository = new FavoriteRepository();
        this.reviewRepository = new ReviewRepository();
    }

    /**
     * Set the current stage for navigation
     */
    public void setCurrentStage(Stage stage) {
        this.currentStage = stage;
    }

    /**
     * Shows the book details dialog with all actions
     */
    public void showBookDetails(Book book) {
        showBookDetails(book, null, null);
    }

    /**
     * Shows the book details dialog with all actions
     * @param book The book to display
     * @param currentStatus Current status of the book for the user (optional)
     */
    public void showBookDetails(Book book, String currentStatus, Runnable onBookUpdated) {

            System.out.println(">>> DEBUG: BookDialog.showBookDetails for: " + book.getTitle());
            System.out.println(">>> DEBUG: coverImage from DB: " + book.getCoverImage());
            System.out.println(">>> DEBUG: safeCoverImage: " + book.getSafeCoverImage());
            System.out.println(">>> DEBUG: defaultCoverImage: " + book.getDefaultCoverImage());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("📖 " + book.getTitle());
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CLOSE);
        pane.setPrefWidth(700); // Increased width for cover image
        pane.setPrefHeight(600);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");


        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.TOP_LEFT);

        VBox coverContainer = new VBox();
        coverContainer.setPrefWidth(150);
        coverContainer.setAlignment(Pos.TOP_CENTER);

        StackPane coverPane = new StackPane();
        coverPane.setPrefSize(120, 180);
        coverPane.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                "-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-width: 1; -fx-border-color: #ddd;");

        try {
            String safeCoverImage = book.getSafeCoverImage();
            if (safeCoverImage != null) {
                ImageView coverImageView = new ImageView();
                coverImageView.setFitWidth(120);
                coverImageView.setFitHeight(180);
                coverImageView.setPreserveRatio(true);
                coverImageView.setSmooth(true);

                // Load image with error handling
                Image image = new Image(safeCoverImage, 120, 180, true, true, true);
                if (!image.isError()) {
                    coverImageView.setImage(image);
                    coverPane.getChildren().add(coverImageView);
                } else {
                    throw new Exception("Image load error");
                }
            } else {
                throw new Exception("No cover image");
            }
        } catch (Exception e) {

            VBox placeholder = new VBox(10);
            placeholder.setAlignment(Pos.CENTER);

            Label bookEmoji = new Label("📖");
            bookEmoji.setStyle("-fx-font-size: 48; -fx-text-fill: white;");

            Label coverText = new Label("Book Cover");
            coverText.setStyle("-fx-font-size: 11; -fx-text-fill: white; -fx-font-weight: bold;");

            placeholder.getChildren().addAll(bookEmoji, coverText);
            coverPane.getChildren().add(placeholder);
        }


        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            Label genreBadge = new Label(book.getGenre());
            genreBadge.setStyle("-fx-background-color: rgba(102, 126, 234, 0.2); " +
                    "-fx-text-fill: #667eea; -fx-font-size: 10; " +
                    "-fx-font-weight: bold; -fx-padding: 3 8; " +
                    "-fx-background-radius: 10;");
            genreBadge.setMaxWidth(120);
            genreBadge.setAlignment(Pos.CENTER);
            VBox.setMargin(genreBadge, new Insets(10, 0, 0, 0));
            coverContainer.getChildren().addAll(coverPane, genreBadge);
        } else {
            coverContainer.getChildren().add(coverPane);
        }


        VBox infoBox = new VBox(12);
        infoBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);


        HBox authorBox = new HBox(5);
        authorBox.setAlignment(Pos.CENTER_LEFT);

        Label byLabel = new Label("by ");
        byLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        Button authorButton = new Button(book.getAuthorName());
        authorButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; " +
                "-fx-font-weight: bold; -fx-padding: 0 0 0 5; -fx-border-width: 0; " +
                "-fx-cursor: hand; -fx-underline: true; -fx-font-size: 14;");
        authorButton.setOnAction(e -> {
            dialog.close();
            navigateToAuthor(book.getAuthorId());
        });

        authorBox.getChildren().addAll(byLabel, authorButton);

        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        if (book.getAverageRating() > 0) {
            // Star rating
            HBox starsBox = new HBox(2);
            int fullStars = (int) book.getAverageRating();
            boolean halfStar = (book.getAverageRating() - fullStars) >= 0.5;

            for (int i = 0; i < 5; i++) {
                Label star = new Label(i < fullStars ? "★" : "☆");
                star.setStyle(i < fullStars ?
                        "-fx-text-fill: #FFD700; -fx-font-size: 16;" :
                        "-fx-text-fill: #ddd; -fx-font-size: 16;");
                starsBox.getChildren().add(star);
            }

            if (halfStar && fullStars < 5) {
                Label halfStarLabel = new Label("⯨");
                halfStarLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16;");
                starsBox.getChildren().set(fullStars, halfStarLabel);
            }

            Label ratingValue = new Label(String.format("%.1f", book.getAverageRating()));
            ratingValue.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            Label reviewCount = new Label("(" + book.getRatingCount() + " reviews)");
            reviewCount.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

            ratingBox.getChildren().addAll(starsBox, ratingValue, reviewCount);
        } else {
            Label noRating = new Label("No ratings yet");
            noRating.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 14;");
            ratingBox.getChildren().add(noRating);
        }


        HBox metaBox = new HBox(15);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.setStyle("-fx-padding: 8 0;");

        if (book.getPageCount() > 0) {
            HBox pagesBox = new HBox(5);
            pagesBox.setAlignment(Pos.CENTER_LEFT);
            Label pagesIcon = new Label("📄");
            Label pagesText = new Label(book.getPageCount() + " pages");
            pagesText.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
            pagesBox.getChildren().addAll(pagesIcon, pagesText);
            metaBox.getChildren().add(pagesBox);
        }

        if (book.getPublicationYear() > 0) {
            HBox yearBox = new HBox(5);
            yearBox.setAlignment(Pos.CENTER_LEFT);
            Label yearIcon = new Label("📅");
            Label yearText = new Label(String.valueOf(book.getPublicationYear()));
            yearText.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
            yearBox.getChildren().addAll(yearIcon, yearText);
            metaBox.getChildren().add(yearBox);
        }


        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            HBox isbnBox = new HBox(5);
            isbnBox.setAlignment(Pos.CENTER_LEFT);
            Label isbnIcon = new Label("🏷️");
            Label isbnText = new Label("ISBN: " + book.getIsbn());
            isbnText.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
            isbnBox.getChildren().addAll(isbnIcon, isbnText);
            metaBox.getChildren().add(isbnBox);
        }

        infoBox.getChildren().addAll(titleLabel, authorBox, ratingBox, metaBox);
        headerBox.getChildren().addAll(coverContainer, infoBox);

        VBox descSection = new VBox(10);
        descSection.setStyle("-fx-padding: 15 0;");

        Label descHeader = new Label("Description");
        descHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        TextArea descArea = new TextArea(book.getDescription() != null ?
                book.getDescription() : "No description available.");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #eee; " +
                "-fx-text-fill: #444; -fx-font-size: 13;");

        descSection.getChildren().addAll(descHeader, descArea);

        if (currentUser != null) {
            HBox actionBox = createActionBox(book, currentStatus, dialog, onBookUpdated);
            actionBox.setStyle("-fx-padding: 10 0;");
            content.getChildren().addAll(headerBox, new Separator(), descSection,
                    new Separator(), actionBox, new Separator());
        } else {
            content.getChildren().addAll(headerBox, new Separator(), descSection, new Separator());
        }

        VBox reviewsSection = createReviewsSection(book);
        content.getChildren().add(reviewsSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        pane.setContent(scrollPane);
        dialog.showAndWait();
    }

    /**
     * Creates the action buttons box
     */
    private HBox createActionBox(Book book, String currentStatus, Dialog<ButtonType> parentDialog, Runnable onBookUpdated) {
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        boolean isFavorite = favoriteRepository.isFavorite(currentUser.getUserId(), book.getBookId());

        Button favoriteBtn = new Button(isFavorite ? "❤️ Favorited" : "🤍 Add to Favorites");
        favoriteBtn.setStyle(isFavorite ?
                "-fx-background-color: #ffe6e6; -fx-text-fill: #e74c3c; -fx-background-radius: 20;" :
                "-fx-background-color: #f0f0f0; -fx-text-fill: #666; -fx-background-radius: 20;");
        favoriteBtn.setOnAction(e -> {
            toggleFavoriteButton(book, favoriteBtn, onBookUpdated);
        });

        Button addToListBtn = new Button("📚 Add to List");
        addToListBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-background-radius: 20;");
        addToListBtn.setOnAction(e -> {
            parentDialog.close();
            showAddToListDialog(book, onBookUpdated);
        });

        Button rateBtn = new Button("⭐ Rate");
        rateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 20;");
        rateBtn.setOnAction(e -> {
            parentDialog.close();
            showRatingDialog(book, onBookUpdated);
        });

        actionBox.getChildren().addAll(favoriteBtn, addToListBtn, rateBtn);
        return actionBox;
    }

    /**
     * Creates the reviews section
     */
    private VBox createReviewsSection(Book book) {
        VBox section = new VBox(10);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label reviewsHeader = new Label("Reviews");
        reviewsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Stars", "⭐⭐⭐⭐⭐ 5", "⭐⭐⭐⭐ 4",
                "⭐⭐⭐ 3", "⭐⭐ 2", "⭐ 1");
        filterCombo.setValue("All Stars");
        filterCombo.setStyle("-fx-font-size: 11;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(reviewsHeader, spacer, filterCombo);

        VBox reviewsContainer = new VBox(8);

        loadReviewsIntoContainer(reviewsContainer, book, -1);

        filterCombo.setOnAction(e -> {
            String selected = filterCombo.getValue();
            int rating = -1;
            if (selected.contains("5")) rating = 5;
            else if (selected.contains("4")) rating = 4;
            else if (selected.contains("3")) rating = 3;
            else if (selected.contains("2")) rating = 2;
            else if (selected.contains("1")) rating = 1;

            loadReviewsIntoContainer(reviewsContainer, book, rating);
        });

        ScrollPane reviewsScroll = new ScrollPane(reviewsContainer);
        reviewsScroll.setFitToWidth(true);
        reviewsScroll.setPrefHeight(180);
        reviewsScroll.setStyle("-fx-background-color: transparent;");

        section.getChildren().addAll(header, reviewsScroll);

        return section;
    }

    /**
     * Loads reviews into container
     */
    private void loadReviewsIntoContainer(VBox container, Book book, int filterRating) {
        container.getChildren().clear();

        List<Review> reviews;
        if (filterRating > 0) {
            reviews = reviewRepository.getBookReviewsByRating(book.getBookId(), filterRating);
        } else {
            reviews = reviewRepository.getBookReviews(book.getBookId());
        }

        if (reviews.isEmpty()) {
            Label empty = new Label(filterRating > 0 ?
                    "No " + filterRating + "-star reviews yet." : "No reviews yet.");
            empty.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            container.getChildren().add(empty);
            return;
        }

        for (Review review : reviews) {
            HBox card = new HBox(12);
            card.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 8;");

            Label stars = new Label(review.getRatingStars());
            stars.setStyle("-fx-font-size: 12;");

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label userName = new Label(review.getUserFullName());
            userName.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");

            if (review.getReviewText() != null && !review.getReviewText().isEmpty()) {
                Label text = new Label(review.getReviewText());
                text.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
                text.setWrapText(true);
                info.getChildren().addAll(userName, text);
            } else {
                info.getChildren().add(userName);
            }

            Label date = new Label(review.getFormattedDate());
            date.setStyle("-fx-text-fill: #aaa; -fx-font-size: 9;");

            card.getChildren().addAll(stars, info, date);
            container.getChildren().add(card);
        }
    }

    /**
     * Shows add to list dialog
     */
    private void showAddToListDialog(Book book, Runnable onBookUpdated) {
        System.out.println(">>> BookDialog.showAddToListDialog called for: " + book.getTitle());
        System.out.println(">>>   Callback provided: " + (onBookUpdated != null));

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add to List");
        alert.setHeaderText("Add \"" + book.getTitle() + "\" to:");

        ButtonType toRead = new ButtonType("📚 To Read");
        ButtonType reading = new ButtonType("📖 Currently Reading");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(toRead, reading, cancel);

        alert.showAndWait().ifPresent(response -> {
            boolean bookAdded = false;

            if (response == toRead) {
                System.out.println(">>> Adding book to TO_READ list");
                boolean success = bookRepository.addBookToUserList(currentUser.getUserId(), book.getBookId(), "TO_READ");
                System.out.println(">>> Add to TO_READ result: " + success);

                if (success) {
                    showAlert("Added!", "\"" + book.getTitle() + "\" added to your To Read list.");
                    bookAdded = true;
                } else {
                    showAlert("Already in list", "\"" + book.getTitle() + "\" is already in your list.");
                }

            } else if (response == reading) {
                System.out.println(">>> Adding book to READING list");
                boolean success = bookRepository.addBookToUserList(currentUser.getUserId(), book.getBookId(), "READING");
                System.out.println(">>> Add to READING result: " + success);

                if (success) {
                    showAlert("Added!", "\"" + book.getTitle() + "\" added to Currently Reading.");
                    bookAdded = true;
                } else {
                    showAlert("Already in list", "\"" + book.getTitle() + "\" is already in your list.");
                }
            }

            // ⭐ KEY FIX: Call the callback to refresh the view
            if (bookAdded && onBookUpdated != null) {
                System.out.println(">>> Calling refresh callback");
                onBookUpdated.run();
            }
        });
    }

    /**
     * Shows move to list dialog
     */
    private void showMoveToListDialog(Book book, String currentStatus, Runnable onBookUpdated) {
        System.out.println(">>> BookDialog.showMoveToListDialog called");
        System.out.println(">>>   Current status: " + currentStatus);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Move Book");
        alert.setHeaderText("Move \"" + book.getTitle() + "\" to:");

        ButtonType toRead = new ButtonType("📚 To Read");
        ButtonType reading = new ButtonType("📖 Currently Reading");
        ButtonType completed = new ButtonType("✅ Completed");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(toRead, reading, completed, cancel);

        alert.showAndWait().ifPresent(response -> {
            String newStatus = null;

            if (response == toRead) newStatus = "TO_READ";
            else if (response == reading) newStatus = "READING";
            else if (response == completed) newStatus = "COMPLETED";

            if (newStatus != null && !newStatus.equals(currentStatus)) {
                System.out.println(">>> Moving book to: " + newStatus);
                boolean success = bookRepository.updateBookStatus(currentUser.getUserId(), book.getBookId(), newStatus);
                System.out.println(">>> Move result: " + success);

                if (success) {
                    showAlert("Moved!", "\"" + book.getTitle() + "\" moved successfully.");

                    // ⭐ Call callback to refresh view
                    if (onBookUpdated != null) {
                        System.out.println(">>> Calling refresh callback");
                        onBookUpdated.run();
                    }

                    if (newStatus.equals("COMPLETED")) {
                        showRatingDialog(book, onBookUpdated);
                    }
                }
            }
        });
    }

    /**
     * Shows rating dialog
     */
    private void showRatingDialog(Book book, Runnable onBookUpdated) {
        System.out.println(">>> BookDialog.showRatingDialog called");

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("⭐ Rate Book");
        dialog.setHeaderText("Rate \"" + book.getTitle() + "\"");

        ButtonType submitType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");
        content.setAlignment(Pos.CENTER);

        // Star rating
        HBox starsBox = new HBox(10);
        starsBox.setAlignment(Pos.CENTER);

        ToggleGroup starGroup = new ToggleGroup();
        int[] selectedRating = {0};

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ToggleButton star = new ToggleButton("⭐");
            star.setStyle("-fx-font-size: 24; -fx-background-color: transparent;");
            star.setToggleGroup(starGroup);

            star.setOnAction(e -> {
                selectedRating[0] = rating;
                // Update visual feedback
                for (int j = 0; j < starsBox.getChildren().size(); j++) {
                    ToggleButton s = (ToggleButton) starsBox.getChildren().get(j);
                    s.setText(j < rating ? "⭐" : "☆");
                }
            });

            starsBox.getChildren().add(star);
        }

        // Review text
        Label reviewLabel = new Label("Write a review (optional):");
        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Share your thoughts about this book...");
        reviewArea.setPrefRowCount(4);
        reviewArea.setWrapText(true);

        content.getChildren().addAll(starsBox, reviewLabel, reviewArea);
        dialog.getDialogPane().setContent(content);

        Button submitBtn = (Button) dialog.getDialogPane().lookupButton(submitType);
        submitBtn.setDisable(true);

        starGroup.selectedToggleProperty().addListener((obs, o, n) -> submitBtn.setDisable(n == null));

        dialog.setResultConverter(btn -> btn == submitType ? selectedRating[0] : null);

        dialog.showAndWait().ifPresent(rating -> {
            if (rating > 0) {
                System.out.println(">>> Submitting rating: " + rating);
                String reviewText = reviewArea.getText().trim();
                boolean success = reviewRepository.addOrUpdateReview(
                        currentUser.getUserId(),
                        book.getBookId(),
                        rating,
                        reviewText.isEmpty() ? null : reviewText
                );

                if (success) {
                    showAlert("Thank you!", "Your " + rating + "-star review has been submitted!");

                    // ⭐ Call callback to refresh view
                    if (onBookUpdated != null) {
                        System.out.println(">>> Calling refresh callback");
                        onBookUpdated.run();
                    }
                }
            }
        });
    }

    /**
     * Toggles favorite button
     */
    private void toggleFavoriteButton(Book book, Button btn, Runnable onBookUpdated) {
        System.out.println(">>> BookDialog.toggleFavoriteButton called");

        boolean isFavorite = favoriteRepository.isFavorite(currentUser.getUserId(), book.getBookId());

        if (isFavorite) {
            favoriteRepository.removeFavorite(currentUser.getUserId(), book.getBookId());
            btn.setText("Add to Favorites");
            btn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #666; -fx-background-radius: 20;");
            showAlert("Removed", "\"" + book.getTitle() + "\" removed from favorites.");
        } else {
            favoriteRepository.addFavorite(currentUser.getUserId(), book.getBookId());
            btn.setText("❤ Favorited");
            btn.setStyle("-fx-background-color: #ffe6e6; -fx-text-fill: #e74c3c; -fx-background-radius: 20;");
            showAlert("Added!", "\"" + book.getTitle() + "\" added to favorites!");
        }

        // ⭐ Call callback to refresh view
        if (onBookUpdated != null) {
            System.out.println(">>> Calling refresh callback");
            onBookUpdated.run();
        }
    }

    private void navigateToAuthor(int authorId) {
        System.out.println(">>> BookDialog.navigateToAuthor: " + authorId);

        try {

            Author author = authorRepository.getAuthorById(authorId);
            if (author == null) {
                showAlert("Error", "Author not found!");
                return;
            }

            // Load the author view FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("author-view.fxml"));
            Parent root = loader.load();

            AuthorController controller = loader.getController();
            controller.setData(currentUser, author);

            Stage stage = currentStage;
            if (stage == null) {
                // Try to get stage from any open window
                stage = (Stage) javafx.stage.Window.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .map(w -> (Stage) w)
                        .orElse(null);

                if (stage == null) {
                    showAlert("Error", "Cannot navigate to author page. No window found.");
                    return;
                }
            }

            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Author: " + author.getName());
            System.out.println(">>> Successfully navigated to author: " + author.getName());

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to author: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not navigate to author page: " + e.getMessage());
        }
    }

    /**
     * Helper method to show alerts
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showBookOptions(Book book, String currentStatus, Runnable refreshCallback) {
        // Create a custom dialog for book options
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Options: " + book.getTitle());
        dialog.setHeaderText("What would you like to do with this book?");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);
        dialogPane.setPrefWidth(400);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Option 1: View Details
        Button viewDetailsBtn = new Button("📖 View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        viewDetailsBtn.setMaxWidth(Double.MAX_VALUE);
        viewDetailsBtn.setOnAction(e -> {
            dialog.close();
            showBookDetails(book, currentStatus, refreshCallback);
        });

        // Option 2: Add to List / Move
        String listOptionText = (currentStatus == null || currentStatus.isEmpty()) ?
                "📚 Add to List" : "📚 Move to Another List";
        Button addToListBtn = new Button(listOptionText);
        addToListBtn.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        addToListBtn.setMaxWidth(Double.MAX_VALUE);
        addToListBtn.setOnAction(e -> {
            dialog.close();
            if (currentStatus == null || currentStatus.isEmpty()) {
                showAddToListDialog(book, refreshCallback);
            } else {
                showMoveToListDialog(book, currentStatus, refreshCallback);
            }
        });

        // Option 3: Rate
        Button rateBtn = new Button("⭐ Rate this Book");
        rateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        rateBtn.setMaxWidth(Double.MAX_VALUE);
        rateBtn.setOnAction(e -> {
            dialog.close();
            showRatingDialog(book, refreshCallback);
        });

        // Option 4: Toggle Favorite
        boolean isFavorite = favoriteRepository.isFavorite(currentUser.getUserId(), book.getBookId());
        String favText = isFavorite ? "❤ Remove from Favorites" : "🤍 Add to Favorites";
        Button favoriteBtn = new Button(favText);
        favoriteBtn.setStyle(isFavorite ?
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" :
                "-fx-background-color: #666; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        favoriteBtn.setMaxWidth(Double.MAX_VALUE);
        favoriteBtn.setOnAction(e -> {
            dialog.close();
            boolean newFavoriteState = !isFavorite;

            if (newFavoriteState) {
                favoriteRepository.addFavorite(currentUser.getUserId(), book.getBookId());
                showAlert("Added!", "\"" + book.getTitle() + "\" added to favorites!");
            } else {
                favoriteRepository.removeFavorite(currentUser.getUserId(), book.getBookId());
                showAlert("Removed", "\"" + book.getTitle() + "\" removed from favorites.");
            }

            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });

        // Option 5: View Author
        Button authorBtn = new Button("👨‍💻 View Author");
        authorBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");
        authorBtn.setMaxWidth(Double.MAX_VALUE);
        authorBtn.setOnAction(e -> {
            dialog.close();
            navigateToAuthor(book.getAuthorId());
        });

        content.getChildren().addAll(viewDetailsBtn, addToListBtn, rateBtn, favoriteBtn, authorBtn);

        dialogPane.setContent(content);
        dialog.showAndWait();
    }
}