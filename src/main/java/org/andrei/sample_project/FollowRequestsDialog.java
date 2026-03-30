package org.andrei.sample_project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.andrei.sample_project.repository.FollowRepository;

import java.util.List;

/**
 * FollowRequestsDialog
 * Shows follow requests or recent follows based on account privacy
 */
public class FollowRequestsDialog {

    private final User currentUser;
    private final FollowRepository followRepository;
    private DialogPane dialogPane;
    private Stage ownerStage;  // Add this to store the owner stage

    public FollowRequestsDialog(User currentUser, Stage ownerStage) {
        this.currentUser = currentUser;
        this.followRepository = new FollowRepository();
        this.ownerStage = ownerStage;
    }

    public void show() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📨 Follow Activity");

        // Initialize with owner stage to maintain window hierarchy
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.WINDOW_MODAL);

        dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setMinWidth(500);
        mainContainer.setMinHeight(400);

        Label headerLabel = new Label("📨 Follow Activity");
        headerLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #667eea;");

        boolean isPrivate = currentUser.isPrivate();
        Label privacyLabel = new Label(isPrivate ? "🔒 Private Account" : "🌍 Public Account");
        privacyLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + (isPrivate ? "#e74c3c" : "#27ae60") + ";");

        String description = isPrivate
                ? "People who want to follow you. Accept or decline requests."
                : "Recent users who followed you.";
        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #666;");
        descriptionLabel.setWrapText(true);

        List<User> users = followRepository.getRecentFollowActivity(currentUser.getUserId());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox usersContainer = new VBox(10);
        usersContainer.setPadding(new Insets(10, 5, 10, 5));

        if (users.isEmpty()) {
            Label emptyLabel = new Label(isPrivate ? "No pending follow requests" : "No recent followers");
            emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14; -fx-padding: 40;");
            emptyLabel.setAlignment(Pos.CENTER);
            usersContainer.getChildren().add(emptyLabel);
        } else {
            for (User user : users) {
                HBox userCard = createUserCard(user, isPrivate);
                usersContainer.getChildren().add(userCard);
            }
        }

        scrollPane.setContent(usersContainer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        int followersCount = followRepository.getFollowers(currentUser.getUserId()).size();
        int followingCount = followRepository.getFollowing(currentUser.getUserId()).size();

        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox followersBox = createStatBox("👥 Followers", String.valueOf(followersCount), "#667eea");
        VBox followingBox = createStatBox("➡Following", String.valueOf(followingCount), "#764ba2");

        if (isPrivate) {
            int pendingCount = users.size();
            VBox requestsBox = createStatBox("⏳ Pending", String.valueOf(pendingCount), "#f39c12");
            statsBox.getChildren().addAll(followersBox, followingBox, requestsBox);
        } else {
            statsBox.getChildren().addAll(followersBox, followingBox);
        }

        mainContainer.getChildren().addAll(
                headerLabel,
                privacyLabel,
                descriptionLabel,
                scrollPane,
                new Separator(),
                statsBox
        );

        dialogPane.setContent(mainContainer);
        dialogPane.setStyle("-fx-background-color: #f5f7fa;");
        dialog.showAndWait();
    }

    private HBox createUserCard(User user, boolean isPrivateAccount) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-border-width: 1;");

        // Avatar
        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(40, 40);
        avatarPane.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                "-fx-background-radius: 20;");

        Label initialsLabel = new Label(getInitials(user.getFullName()));
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");

        avatarPane.getChildren().add(initialsLabel);

        // User info
        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(user.getFullName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #333;");

        Label usernameLabel = new Label("@" + user.getUsername());
        usernameLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

        infoBox.getChildren().addAll(nameLabel, usernameLabel);

        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        if (isPrivateAccount) {
            // For private accounts: Accept/Decline buttons
            Button acceptBtn = new Button("Accept");
            acceptBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                    "-fx-font-size: 11; -fx-padding: 5 12; -fx-background-radius: 15;");
            acceptBtn.setOnAction(e -> handleAcceptRequest(user));

            Button declineBtn = new Button("Decline");
            declineBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                    "-fx-font-size: 11; -fx-padding: 5 12; -fx-background-radius: 15;");
            declineBtn.setOnAction(e -> handleDeclineRequest(user));

            actionBox.getChildren().addAll(declineBtn, acceptBtn);

        } else {
            // For public accounts: Show follow status
            Label statusLabel = new Label("Follows you");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11; -fx-font-weight: bold;");
            actionBox.getChildren().add(statusLabel);
        }

        card.getChildren().addAll(avatarPane, infoBox, actionBox);

        // Make card clickable to view profile
        card.setOnMouseClicked(e -> showUserProfile(user));
        card.setStyle(card.getStyle() + " -fx-cursor: hand;");

        return card;
    }

    private VBox createStatBox(String title, String value, String color) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        box.getChildren().addAll(valueLabel, titleLabel);
        return box;
    }

    private void handleAcceptRequest(User requester) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accept Follow Request");
        confirm.setHeaderText("Accept " + requester.getFullName() + "?");
        confirm.setContentText("This user will be able to see your content.");

        // Set owner to maintain window hierarchy
        confirm.initOwner(ownerStage);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = followRepository.acceptFollowRequest(
                        requester.getUserId(),
                        currentUser.getUserId()
                );

                if (success) {
                    showAlert("Request Accepted",
                            requester.getFullName() + " can now see your content.");
                    // Refresh the dialog
                    show();
                } else {
                    showAlert("Error", "Could not accept the request. Please try again.");
                }
            }
        });
    }

    private void handleDeclineRequest(User requester) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Decline Follow Request");
        confirm.setHeaderText("Decline " + requester.getFullName() + "?");
        confirm.setContentText("This user will not be able to follow you.");

        // Set owner to maintain window hierarchy
        confirm.initOwner(ownerStage);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = followRepository.rejectFollowRequest(
                        requester.getUserId(),
                        currentUser.getUserId()
                );

                if (success) {
                    showAlert("Request Declined",
                            requester.getFullName() + " will not be following you.");
                    show();
                } else {
                    showAlert("Error", "Could not decline the request. Please try again.");
                }
            }
        });
    }

    private void showUserProfile(User user) {
        // Navigate to user profile
        try {
            // Create a new stage for the user profile
            Stage profileStage = new Stage();
            profileStage.setTitle(user.getFullName() + " - Profile");

            // Set owner to maintain window hierarchy
            profileStage.initOwner(ownerStage);
            profileStage.initModality(Modality.WINDOW_MODAL);

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("user-profile-view.fxml")
            );
            javafx.scene.Parent root = loader.load();

            UserProfileController controller = loader.getController();
            controller.setUsers(currentUser, user);

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
            profileStage.setScene(scene);

            profileStage.show();

        } catch (Exception e) {
            System.out.println(">>> ERROR showing user profile: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Could not load profile: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Set owner to maintain window hierarchy
        alert.initOwner(ownerStage);

        alert.showAndWait();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "U";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }
}