package org.andrei.sample_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.andrei.sample_project.BookDialog;
import org.andrei.sample_project.repository.*;

import java.util.List;

/**
 * UserProfileController
 * displays anothers user s profile
 */
public class UserProfileController {

    @FXML private Button menuButton;
    @FXML private Label profileTitleLabel;
    @FXML private Label privacyLabel;
    @FXML private Button followButton;

    @FXML private Label profileInitialsLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label bioLabel;
    @FXML private Label followersCountLabel;
    @FXML private Label followingCountLabel;

    @FXML private VBox publicContentBox;
    @FXML private Label booksCompletedLabel;
    @FXML private Label reviewsWrittenLabel;
    @FXML private HBox favoriteBooksContainer;
    @FXML private VBox recentReviewsContainer;

    @FXML private VBox privateContentBox;

    private User currentUser;
    private User profileUser;
    private String followStatus;

    private BookRepository bookRepository;
    private UserRepository userRepository;
    private ReviewRepository reviewRepository;
    private FavoriteRepository favoriteRepository;
    private FollowRepository followRepository;

    @FXML
    public void initialize() {
        System.out.println(">>> UserProfileController.initialize()");

        bookRepository = new BookRepository();
        userRepository = new UserRepository();
        reviewRepository = new ReviewRepository();
        favoriteRepository = new FavoriteRepository();
        followRepository = new FollowRepository();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setProfileUser(User profileUser) {
        this.profileUser = profileUser;
        loadProfile();
    }

    public void setUsers(User currentUser, User profileUser) {
        this.currentUser = currentUser;
        this.profileUser = profileUser;
        System.out.println(">>> UserProfileController.setUsers: viewing " + profileUser.getUsername());
        loadProfile();
    }

    private void loadProfile() {
        if (profileUser == null) return;

        //check follow status
        followStatus = followRepository.getFollowStatus(currentUser.getUserId(), profileUser.getUserId());
        boolean isFollowing = "ACCEPTED".equals(followStatus);

        // check if profile is private
        boolean isPrivate = followRepository.isUserPrivate(profileUser.getUserId());
        boolean canViewContent = !isPrivate || isFollowing || currentUser.getUserId() == profileUser.getUserId();

        profileTitleLabel.setText(profileUser.getFullName());
        privacyLabel.setText(isPrivate ? "🔒 Private Account" : "🌍 Public Account");
        fullNameLabel.setText(profileUser.getFullName());
        usernameLabel.setText("@" + profileUser.getUsername());
        String bio = profileUser.getBio();
        bioLabel.setText(bio != null && !bio.isEmpty() ? bio : "No bio yet");

        //profile initials
        profileInitialsLabel.setText(getInitials(profileUser.getFullName()));

        //follower count
        int followers = userRepository.getFollowersCount(profileUser.getUserId());
        int following = userRepository.getFollowingCount(profileUser.getUserId());
        followersCountLabel.setText(String.valueOf(followers));
        followingCountLabel.setText(String.valueOf(following));

        updateFollowButton();

        if (canViewContent) {
            publicContentBox.setVisible(true);
            publicContentBox.setManaged(true);
            privateContentBox.setVisible(false);
            privateContentBox.setManaged(false);

            loadStats();
            loadFavorites();
            loadReviews();
        } else {
            publicContentBox.setVisible(false);
            publicContentBox.setManaged(false);
            privateContentBox.setVisible(true);
            privateContentBox.setManaged(true);
        }
    }

    private void updateFollowButton() {
        if (currentUser.getUserId() == profileUser.getUserId()) {
            followButton.setVisible(false);
            followButton.setManaged(false);
            return;
        }

        followButton.setVisible(true);
        followButton.setManaged(true);

        if ("ACCEPTED".equals(followStatus)) {
            followButton.setText("✓ Following");
            followButton.setStyle("-fx-background-color: #e8f4fd; -fx-text-fill: #3498db; " +
                    "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25;");
        } else if ("PENDING".equals(followStatus)) {
            followButton.setText("⏳ Requested");
            followButton.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f39c12; " +
                    "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25;");
        } else {
            followButton.setText("➕ Follow");
            followButton.setStyle("-fx-background-color: white; -fx-text-fill: #667eea; " +
                    "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 25;");
        }
    }

    private void loadStats() {
        int booksCompleted = bookRepository.getUserCompletedBooksCount(profileUser.getUserId());
        List<Review> reviews = reviewRepository.getUserReviews(profileUser.getUserId());

        booksCompletedLabel.setText(String.valueOf(booksCompleted));
        reviewsWrittenLabel.setText(String.valueOf(reviews.size()));
    }

    private void loadFavorites() {
        favoriteBooksContainer.getChildren().clear();

        List<Book> favorites = favoriteRepository.getUserFavorites(profileUser.getUserId());
        if (favorites.isEmpty()) {
            Label empty = new Label("No favorite books yet");
            empty.setStyle("-fx-text-fill: #12050e; -fx-padding: 20;");
            favoriteBooksContainer.getChildren().add(empty);
            return;
        }

        for (Book book : favorites) {
            VBox card = createFavoriteBookCard(book);
            favoriteBooksContainer.getChildren().add(card);
        }
    }

    private VBox createFavoriteBookCard(Book book) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(120);
        card.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");

        Label emoji = new Label("📚");
        emoji.setStyle("-fx-font-size: 28;");

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #333;");
        title.setWrapText(true);
        title.setMaxWidth(100);
        title.setAlignment(Pos.CENTER);

        Label author = new Label(book.getAuthorName());
        author.setStyle("-fx-font-size: 10; -fx-text-fill: #12050e;");

        card.getChildren().addAll(emoji, title, author);
        card.setOnMouseClicked(e -> showBookDetails(book));
        return card;
    }

    private void loadReviews() {
        recentReviewsContainer.getChildren().clear();

        List<Review> reviews = reviewRepository.getUserReviews(profileUser.getUserId());

        if (reviews.isEmpty()) {
            Label empty = new Label("No reviews yet");
            empty.setStyle("-fx-text-fill: #12050e;");
            recentReviewsContainer.getChildren().add(empty);
            return;
        }

        int count = 0;
        for (Review review : reviews) {
            if (count >= 5) break;

            HBox card = createReviewCard(review);
            recentReviewsContainer.getChildren().add(card);
            count++;
        }
    }

    private HBox createReviewCard(Review review) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-padding: 12; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        Label stars = new Label(review.getRatingStars());
        stars.setStyle("-fx-font-size: 14;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label bookTitle = new Label(review.getBookTitle());
        bookTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: black;");

        if (review.hasReviewText()) {
            Label text = new Label("\"" + review.getReviewPreview() + "\"");
            text.setStyle("-fx-text-fill: #666; -fx-font-size: 11; -fx-font-style: italic;");
            text.setWrapText(true);
            info.getChildren().addAll(bookTitle, text);
        } else {
            info.getChildren().add(bookTitle);
        }

        Label date = new Label(review.getFormattedDate());
        date.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10;");

        card.getChildren().addAll(stars, info, date);
        return card;
    }

    @FXML
    protected void onMenuClick() {
        showMenu();
    }

    @FXML
    protected void onFollowClick() {
        if ("ACCEPTED".equals(followStatus)) {
            // Unfollow confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unfollow");
            confirm.setHeaderText("Unfollow " + profileUser.getFullName() + "?");
            confirm.setContentText("You will no longer see their updates.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = followRepository.unfollowUser(currentUser.getUserId(), profileUser.getUserId());
                    if (success) {
                        followStatus = null;
                        loadProfile();
                    }
                }
            });
        } else if ("PENDING".equals(followStatus)) {
            // Cancel request
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Request");
            confirm.setHeaderText("Cancel follow request?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    followRepository.unfollowUser(currentUser.getUserId(), profileUser.getUserId());
                    followStatus = null;
                    updateFollowButton();
                }
            });
        } else {
            //send follow request
            boolean success = followRepository.followUser(currentUser.getUserId(), profileUser.getUserId());
            if (success) {
                boolean isPrivate = followRepository.isUserPrivate(profileUser.getUserId());
                followStatus = isPrivate ? "PENDING" : "ACCEPTED";

                if ("PENDING".equals(followStatus)) {
                    showAlert("Request Sent", "Your follow request has been sent to " + profileUser.getFullName());
                }

                loadProfile();
            }
        }
    }

    @FXML
    protected void onViewFollowers() {
        List<User> followers = followRepository.getFollowers(profileUser.getUserId());
        showUserListDialog("Followers", followers);
    }

    @FXML
    protected void onViewFollowing() {
        List<User> following = followRepository.getFollowing(profileUser.getUserId());
        showUserListDialog("Following", following);
    }

    private void showMenu() {
        try {
            Stage stage = (Stage) menuButton.getScene().getWindow();
            MenuHelper.showMenu(menuButton, currentUser, stage, "user-profile");
        } catch (Exception e) {
            System.out.println(">>> Error showing menu: " + e.getMessage());
            navigateToMain();
        }
    }

    private void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) menuButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1250, 750));
            stage.setTitle("Library System - " + currentUser.getFullName());

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to main: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showUserListDialog(String title, List<User> users) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(profileUser.getFullName() + "'s " + title);

        VBox content = new VBox(10);
        content.setPrefWidth(350);
        content.setStyle("-fx-padding: 10;");

        if (users.isEmpty()) {
            content.getChildren().add(new Label("No " + title.toLowerCase() + " yet"));
        } else {
            for (User user : users) {
                HBox userCard = createUserCard(user);
                content.getChildren().add(userCard);
            }
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Set the owner of the dialog to ensure proper stage hierarchy
        Stage mainStage = (Stage) menuButton.getScene().getWindow();
        dialog.initOwner(mainStage);

        dialog.showAndWait();
    }

    private HBox createUserCard(User user) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-cursor: hand;");

        Label initials = new Label(getInitials(user.getFullName()));
        initials.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 20;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(user.getFullName());
        name.setStyle("-fx-font-weight: bold;");

        Label username = new Label("@" + user.getUsername());
        username.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        info.getChildren().addAll(name, username);
        card.getChildren().addAll(initials, info);
        card.setOnMouseClicked(e -> {
            if (user.getUserId() != currentUser.getUserId()) {
                navigateToUserProfile(user);
            }
        });

        return card;
    }

    private void navigateToUserProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-view.fxml"));
            Parent root = loader.load();

            UserProfileController controller = loader.getController();
            controller.setUsers(currentUser, user);

            // Create a new stage for the user profile
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root, 900, 700));
            newStage.setTitle(user.getFullName() + "'s Profile");

            // Set owner to maintain window hierarchy
            Stage currentStage = (Stage) menuButton.getScene().getWindow();
            newStage.initOwner(currentStage);

            newStage.show();

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showBookDetails(Book book) {
        new BookDialog(currentUser).showBookDetails(book);
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