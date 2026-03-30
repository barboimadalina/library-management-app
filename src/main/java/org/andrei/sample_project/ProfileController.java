package org.andrei.sample_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.stage.Modality;
import org.andrei.sample_project.BookDialog;
import org.andrei.sample_project.FavoritesDialog;
import org.andrei.sample_project.repository.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ProfileController {

    @FXML private Label profileTitleLabel;
    @FXML private Label memberSinceLabel;
    @FXML private Button backButton;
    @FXML private Label profileInitialsLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label bioLabel;
    @FXML private Label followersCountLabel;
    @FXML private Label followingCountLabel;
    @FXML private Label booksCompletedLabel;
    @FXML private Label pagesReadLabel;
    @FXML private Label reviewsWrittenLabel;
    @FXML private Label badgesEarnedLabel;
    @FXML private HBox favoriteBooksContainer;
    @FXML private VBox recentReviewsContainer;
    @FXML private FlowPane badgesShowcaseContainer;
    @FXML private Label privacyStatusLabel;
    @FXML private Button togglePrivacyButton;

    private User currentUser;
    private BookRepository bookRepository;
    private FollowRepository followRepository;
    private ReviewRepository reviewRepository;
    private FavoriteRepository favoriteRepository;
    private ChallengeRepository challengeRepository;
    private UserRepository userRepository;

    @FXML
    public void initialize() {
        bookRepository = new BookRepository();
        followRepository = new FollowRepository();
        reviewRepository = new ReviewRepository();
        favoriteRepository = new FavoriteRepository();
        challengeRepository = new ChallengeRepository();
        userRepository = new UserRepository();
        setupClickableLabels();

        if (currentUser != null) {
            FavoritesDialog.setCurrentUser(currentUser);
        }
    }

    private void setupClickableLabels() {
        if (followersCountLabel != null) {
            followersCountLabel.setOnMouseEntered(e -> followersCountLabel.setStyle("-fx-cursor: hand; -fx-underline: true;"));
            followersCountLabel.setOnMouseExited(e -> followersCountLabel.setStyle("-fx-cursor: default;"));
            followersCountLabel.setOnMouseClicked(e -> showFollowersList());
        }

        if (followingCountLabel != null) {
            followingCountLabel.setOnMouseEntered(e -> followingCountLabel.setStyle("-fx-cursor: hand; -fx-underline: true;"));
            followingCountLabel.setOnMouseExited(e -> followingCountLabel.setStyle("-fx-cursor: default;"));
            followingCountLabel.setOnMouseClicked(e -> showFollowingList());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        FavoritesDialog.setCurrentUser(user);
        loadProfileData();
    }

    private void loadProfileData() {
        if (currentUser == null) return;

        profileTitleLabel.setText("My Profile");
        memberSinceLabel.setText("Member since 2024");

        String initials = getInitials(currentUser.getFullName());
        profileInitialsLabel.setText(initials);

        fullNameLabel.setText(currentUser.getFullName());
        usernameLabel.setText("@" + currentUser.getUsername());
        emailLabel.setText(currentUser.getEmail());

        String bio = currentUser.getBio();
        if (bio != null && !bio.trim().isEmpty()) {
            bioLabel.setText(bio);
            bioLabel.setStyle(bioLabel.getStyle() + "-fx-font-style: normal;");
        } else {
            bioLabel.setText("No bio yet. Click 'Edit Profile' to add one!");
            bioLabel.setStyle(bioLabel.getStyle() + "-fx-font-style: italic; -fx-text-fill: #999;");
        }

        // Privacy status
        boolean isPrivate = followRepository.isUserPrivate(currentUser.getUserId());
        if (privacyStatusLabel != null) {
            privacyStatusLabel.setText(isPrivate ? "🔒 Private Account" : "🌍 Public Account");
        }
        if (togglePrivacyButton != null) {
            togglePrivacyButton.setText(isPrivate ? "Make Public" : "Make Private");
        }

        loadFollowCounts();
        loadReadingStats();
        loadFavoriteBooks();
        loadRecentReviews();
        loadBadgesShowcase();
    }

    private void loadFollowCounts() {
        List<User> followers = followRepository.getFollowers(currentUser.getUserId());
        List<User> following = followRepository.getFollowing(currentUser.getUserId());

        followersCountLabel.setText(String.valueOf(followers.size()));
        followingCountLabel.setText(String.valueOf(following.size()));
    }

    private void loadReadingStats() {
        int booksCompleted = bookRepository.getUserCompletedBooksCount(currentUser.getUserId());
        booksCompletedLabel.setText(String.valueOf(booksCompleted));

        int pagesRead = booksCompleted * 300;
        pagesReadLabel.setText(String.valueOf(pagesRead));

        List<Review> reviews = reviewRepository.getUserReviews(currentUser.getUserId());
        reviewsWrittenLabel.setText(String.valueOf(reviews.size()));

        List<UserAppChallenge> badges = challengeRepository.getUserCompletedAppChallenges(currentUser.getUserId());
        badgesEarnedLabel.setText(String.valueOf(badges.size()));
    }

    private void loadFavoriteBooks() {
        favoriteBooksContainer.getChildren().clear();
        List<Book> favorites = favoriteRepository.getUserFavorites(currentUser.getUserId());

        if (favorites.isEmpty()) {
            Label empty = new Label("No favorites yet");
            empty.setStyle("-fx-text-fill: #999;");
            favoriteBooksContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < Math.min(5, favorites.size()); i++) {
            favoriteBooksContainer.getChildren().add(createFavoriteBookCard(favorites.get(i)));
        }
    }

    private VBox createFavoriteBookCard(Book book) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(120);
        card.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10; -fx-cursor: hand;");

        Label emoji = new Label("📚");
        emoji.setStyle("-fx-font-size: 28;");

        Label title = new Label(book.getTitle().length() > 20 ? book.getTitle().substring(0, 17) + "..." : book.getTitle());
        title.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: black;");

        title.setWrapText(true);

        card.getChildren().addAll(emoji, title);
        card.setOnMouseClicked(e -> showBookDetails(book));

        return card;
    }

    private void loadRecentReviews() {
        recentReviewsContainer.getChildren().clear();
        List<Review> reviews = reviewRepository.getUserReviews(currentUser.getUserId());

        if (reviews.isEmpty()) {
            Label empty = new Label("No reviews yet");
            empty.setStyle("-fx-text-fill: #999;");
            recentReviewsContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < Math.min(3, reviews.size()); i++) {
            recentReviewsContainer.getChildren().add(createReviewCard(reviews.get(i)));
        }
    }

    private HBox createReviewCard(Review review) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f0e6ff; -fx-background-radius: 8; -fx-cursor: hand;");

        Book book = bookRepository.getBookById(review.getBookId());

        Label stars = new Label(getStars(review.getRating()));
        stars.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14;");

        Label bookTitle = new Label(book != null ? book.getTitle() : "Unknown");
        bookTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        Label ratingLabel = new Label("(" + review.getRating() + "/5)");
        ratingLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

        card.getChildren().addAll(stars, bookTitle, ratingLabel);
        card.setOnMouseClicked(e -> showReviewDetails(review));

        return card;
    }

    private String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    private void loadBadgesShowcase() {
        badgesShowcaseContainer.getChildren().clear();
        List<UserAppChallenge> badges = challengeRepository.getUserCompletedAppChallenges(currentUser.getUserId());

        if (badges.isEmpty()) {
            Label empty = new Label("Complete challenges to earn badges!");
            empty.setStyle("-fx-text-fill: #999;");
            badgesShowcaseContainer.getChildren().add(empty);
            return;
        }

        for (UserAppChallenge badge : badges) {
            badgesShowcaseContainer.getChildren().add(createBadgeCard(badge));
        }
    }

    private VBox createBadgeCard(UserAppChallenge badge) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(80, 90);
        card.setStyle("-fx-background-color: #FFF9C4; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");

        Label icon = new Label(badge.getBadgeIcon() != null ? badge.getBadgeIcon() : "🏆");
        icon.setStyle("-fx-font-size: 24;");

        Label name = new Label(badge.getBadgeName() != null ? badge.getBadgeName() : "Badge");
        name.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");
        name.setWrapText(true);

        card.getChildren().addAll(icon, name);
        card.setOnMouseClicked(e -> showBadgeDetails(badge));

        return card;
    }

    @FXML
    protected void onEditProfile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your profile");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField fullNameField = new TextField(currentUser.getFullName());
        TextArea bioArea = new TextArea(currentUser.getBio() != null ? currentUser.getBio() : "");
        bioArea.setPrefRowCount(3);
        bioArea.setWrapText(true);

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Bio:"), 0, 1);
        grid.add(bioArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newFullName = fullNameField.getText().trim();
            String newBio = bioArea.getText().trim();

            if (!newFullName.isEmpty()) {
                userRepository.updateFullName(currentUser.getUserId(), newFullName);
                currentUser.setFullName(newFullName);
            }

            userRepository.updateUserBio(currentUser.getUserId(), newBio);
            currentUser.setBio(newBio);

            loadProfileData();
            showAlert("Success", "Profile updated successfully!");
        }
    }

    @FXML
    protected void onViewAllFavorites() {
        List<Book> favorites = favoriteRepository.getUserFavorites(currentUser.getUserId());
        FavoritesDialog.show(favorites, book -> {
            showBookDetails(book);
        });
    }

    @FXML
    protected void onViewAllReviews() {
        List<Review> reviews = reviewRepository.getUserReviews(currentUser.getUserId());
        showReviewsDialog(reviews);
    }

    private void showReviewsDialog(List<Review> reviews) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Reviews");
        dialog.setHeaderText("You have written " + reviews.size() + " reviews");

        TableView<Review> table = new TableView<>();
        table.setPrefHeight(400);
        table.setPrefWidth(700);

        TableColumn<Review, String> bookCol = new TableColumn<>("Book");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setPrefWidth(200);

        TableColumn<Review, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setPrefWidth(80);

        TableColumn<Review, String> textCol = new TableColumn<>("Review");
        textCol.setCellValueFactory(new PropertyValueFactory<>("reviewText"));
        textCol.setPrefWidth(300);

        TableColumn<Review, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setPrefWidth(120);

        table.getColumns().addAll(bookCol, ratingCol, textCol, dateCol);
        table.setItems(FXCollections.observableArrayList(reviews));

        table.setRowFactory(tv -> {
            TableRow<Review> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Review selectedReview = row.getItem();
                    showReviewDetails(selectedReview);
                }
            });
            return row;
        });

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        if (reviews.isEmpty()) {
            Label emptyLabel = new Label("No reviews yet. Rate and review books you've read!");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
            content.getChildren().add(emptyLabel);
        } else {
            content.getChildren().addAll(new Label("Double-click on a review to view details:"), table);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    protected void onViewAllBadges() {
        List<UserAppChallenge> badges = challengeRepository.getUserCompletedAppChallenges(currentUser.getUserId());
        showBadgesDialog(badges);
    }

    private void showBadgesDialog(List<UserAppChallenge> badges) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Badges");
        dialog.setHeaderText("You have earned " + badges.size() + " badges");

        FlowPane badgesPane = new FlowPane();
        badgesPane.setHgap(15);
        badgesPane.setVgap(15);
        badgesPane.setPrefWidth(500);
        badgesPane.setPadding(new Insets(20));

        if (badges.isEmpty()) {
            Label emptyLabel = new Label("Complete challenges to earn badges!");
            emptyLabel.setStyle("-fx-text-fill: #999;");
            badgesPane.getChildren().add(emptyLabel);
        } else {
            for (UserAppChallenge badge : badges) {
                VBox badgeCard = createDetailedBadgeCard(badge);
                badgesPane.getChildren().add(badgeCard);
            }
        }

        ScrollPane scrollPane = new ScrollPane(badgesPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private VBox createDetailedBadgeCard(UserAppChallenge badge) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(120, 140);
        card.setStyle("-fx-background-color: linear-gradient(to bottom, #FFF9C4, #FFECB3); " +
                "-fx-background-radius: 15; -fx-padding: 15; -fx-cursor: hand;");

        Label icon = new Label(badge.getBadgeIcon() != null ? badge.getBadgeIcon() : "🏆");
        icon.setStyle("-fx-font-size: 36;");

        Label name = new Label(badge.getBadgeName() != null ? badge.getBadgeName() : "Badge");
        name.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");
        name.setWrapText(true);

        Label date = new Label(badge.getCompletedDateString());
        date.setStyle("-fx-font-size: 9; -fx-text-fill: #666;");

        Label challengeName = new Label(badge.getChallengeName());
        challengeName.setStyle("-fx-font-size: 9; -fx-text-fill: #888; -fx-font-style: italic;");
        challengeName.setWrapText(true);

        card.getChildren().addAll(icon, name, date, challengeName);
        card.setOnMouseClicked(e -> showBadgeDetails(badge));

        return card;
    }

    @FXML
    protected void onViewChallenges() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("challenges-view.fxml"));
            Parent root = loader.load();

            ChallengeController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1250, 750));
        } catch (IOException e) {
            System.out.println(">>> Error loading challenges: " + e.getMessage());
            showAlert("Error", "Could not load challenges view");
        }
    }

    @FXML
    protected void onViewDetailedStats() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detailed Reading Statistics");
        dialog.setHeaderText("Your Reading Journey");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));

        int booksCompleted = bookRepository.getUserCompletedBooksCount(currentUser.getUserId());
        int booksReading = bookRepository.getUserBooksByStatus(currentUser.getUserId(), "READING").size();
        int booksToRead = bookRepository.getUserBooksByStatus(currentUser.getUserId(), "TO_READ").size();
        int reviewsCount = reviewRepository.getUserReviews(currentUser.getUserId()).size();
        double avgRating = calculateAverageRating();
        int badgesCount = challengeRepository.getUserCompletedAppChallenges(currentUser.getUserId()).size();

        grid.add(new Label("Books Completed:"), 0, 0);
        grid.add(new Label(String.valueOf(booksCompleted)), 1, 0);
        grid.add(new Label("Currently Reading:"), 0, 1);
        grid.add(new Label(String.valueOf(booksReading)), 1, 1);
        grid.add(new Label("To Read:"), 0, 2);
        grid.add(new Label(String.valueOf(booksToRead)), 1, 2);
        grid.add(new Label("Total Books in Library:"), 0, 3);
        grid.add(new Label(String.valueOf(booksCompleted + booksReading + booksToRead)), 1, 3);
        grid.add(new Label("Reviews Written:"), 0, 4);
        grid.add(new Label(String.valueOf(reviewsCount)), 1, 4);
        grid.add(new Label("Average Rating Given:"), 0, 5);
        grid.add(new Label(String.format("%.1f/5", avgRating)), 1, 5);
        grid.add(new Label("Badges Earned:"), 0, 6);
        grid.add(new Label(String.valueOf(badgesCount)), 1, 6);
        grid.add(new Label("Estimated Pages Read:"), 0, 7);
        grid.add(new Label(String.valueOf(booksCompleted * 300)), 1, 7);

        for (int i = 0; i < 8; i++) {
            GridPane.setHalignment(grid.getChildren().get(i * 2), javafx.geometry.HPos.RIGHT);
            ((Label) grid.getChildren().get(i * 2)).setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
            ((Label) grid.getChildren().get(i * 2 + 1)).setStyle("-fx-text-fill: #6482ff; -fx-font-weight: bold;");
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private double calculateAverageRating() {
        List<Review> reviews = reviewRepository.getUserReviews(currentUser.getUserId());
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    private void showFollowersList() {
        List<User> followers = followRepository.getFollowers(currentUser.getUserId());
        showUserListDialog("Followers", followers);
    }

    private void showFollowingList() {
        List<User> following = followRepository.getFollowing(currentUser.getUserId());
        showUserListDialog("Following", following);
    }

    private void showUserListDialog(String title, List<User> users) {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText("You have " + users.size() + " " + title.toLowerCase());

            VBox content = new VBox(10);
            content.setPadding(new Insets(10));

            for (User user : users) {
                HBox userCard = createUserCard(user);
                content.getChildren().add(userCard);
            }

            ScrollPane scroll = new ScrollPane(content);
            scroll.setPrefHeight(400);
            scroll.setPrefWidth(350);
            scroll.setFitToWidth(true);

            dialog.getDialogPane().setContent(scroll);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Set owner to maintain proper window hierarchy
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            dialog.initOwner(currentStage);
            dialog.initModality(Modality.WINDOW_MODAL);

            dialog.showAndWait();

        } catch (Exception e) {
            System.out.println(">>> ERROR showing " + title + " dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private HBox createUserCard(User user) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-cursor: hand;");

        Label initials = new Label(getInitials(user.getFullName()));
        initials.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 20; -fx-font-weight: bold;");

        VBox info = new VBox(2);
        Label name = new Label(user.getFullName());
        name.setStyle("-fx-font-weight: bold;");
        Label username = new Label("@" + user.getUsername());
        username.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        info.getChildren().addAll(name, username);

        card.getChildren().addAll(initials, info);
        card.setOnMouseClicked(e -> navigateToUserProfile(user));

        return card;
    }

    private void navigateToUserProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-view.fxml"));
            Parent root = loader.load();

            UserProfileController controller = loader.getController();
            controller.setUsers(currentUser, user);

            // Get the current stage from any node that's in the main scene
            Stage currentStage = (Stage) backButton.getScene().getWindow();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root, 900, 700));
            newStage.setTitle(user.getFullName() + "'s Profile");
            newStage.initOwner(currentStage); // Set owner to maintain window hierarchy
            newStage.show();

        } catch (IOException e) {
            System.out.println(">>> ERROR navigating to user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1250, 750));
        } catch (IOException e) {
            System.out.println(">>> Error: " + e.getMessage());
        }
    }

    @FXML
    protected void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            System.out.println(">>> Error: " + e.getMessage());
        }
    }

    private void showBookDetails(Book book) {
        new BookDialog(currentUser).showBookDetails(book);
    }

    private void showReviewDetails(Review review) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Review Details");

        Book book = bookRepository.getBookById(review.getBookId());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label bookLabel = new Label("Book: " + (book != null ? book.getTitle() : "Unknown"));
        bookLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label ratingLabel = new Label("Rating: " + getStars(review.getRating()) + " (" + review.getRating() + "/5)");
        ratingLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #FFD700;");

        Label reviewTextLabel = new Label("Review:");
        reviewTextLabel.setStyle("-fx-font-weight: bold;");

        TextArea reviewArea = new TextArea(review.getReviewText() != null ? review.getReviewText() : "No review text");
        reviewArea.setEditable(false);
        reviewArea.setWrapText(true);
        reviewArea.setPrefRowCount(5);

        Label dateLabel = new Label("Date: " + review.getCreatedAt());
        dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        content.getChildren().addAll(bookLabel, ratingLabel, reviewTextLabel, reviewArea, dateLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showBadgeDetails(UserAppChallenge badge) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Badge Details");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(badge.getBadgeIcon() != null ? badge.getBadgeIcon() : "🏆");
        iconLabel.setStyle("-fx-font-size: 48;");

        Label nameLabel = new Label(badge.getBadgeName() != null ? badge.getBadgeName() : "Badge");
        nameLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label challengeLabel = new Label("Challenge: " + badge.getChallengeName());
        challengeLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        Label descriptionLabel = new Label(badge.getChallengeDescription() != null ? badge.getChallengeDescription() : "Complete a challenge to earn this badge");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefWidth(300);

        Label dateLabel = new Label("Earned: " + badge.getCompletedDateString());
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");

        content.getChildren().addAll(iconLabel, nameLabel, challengeLabel, descriptionLabel, dateLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "U";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}