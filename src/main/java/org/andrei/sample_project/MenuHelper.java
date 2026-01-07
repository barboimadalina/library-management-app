package org.andrei.sample_project;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.andrei.sample_project.FavoritesDialog;
import org.andrei.sample_project.FollowRequestsDialog;
import org.andrei.sample_project.repository.FavoriteRepository;

import java.util.List;

/**
 * MenuHelper
 * Provides navigation menu functionality across views.
 */
public class MenuHelper {

    /**
     * Shows a popup menu for navigation
     */
    public static void showMenu(Button menuButton, User currentUser, Stage stage, String currentView) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox menu = new VBox(5);
        menu.setStyle("-fx-background-color: white; -fx-padding: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        menu.setPrefWidth(200);

        // User info header
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 10; -fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-background-radius: 8;");

        Label avatar = new Label(getInitials(currentUser.getFullName()));
        avatar.setStyle("-fx-background-color: white; -fx-text-fill: #667eea; " +
                "-fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 15;");

        VBox userInfo = new VBox(2);
        Label nameLabel = new Label(currentUser.getFullName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label usernameLabel = new Label("@" + currentUser.getUsername());
        usernameLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 10;");
        userInfo.getChildren().addAll(nameLabel, usernameLabel);

        header.getChildren().addAll(avatar, userInfo);
        menu.getChildren().add(header);
        menu.getChildren().add(new Separator());

        if (!currentView.equals("main")) {
            Button homeBtn = createMenuItem("🏠 Home", "#667eea");
            homeBtn.setOnAction(e -> {
                popup.hide();
                navigateToMain(currentUser, stage);
            });
            menu.getChildren().add(homeBtn);
        }


        if (!currentView.equals("profile")) {
            Button profileBtn = createMenuItem("👤 My Profile", "#27ae60");
            profileBtn.setOnAction(e -> {
                popup.hide();
                navigateToProfile(currentUser, stage);
            });
            menu.getChildren().add(profileBtn);
        }


        if (!currentView.equals("requests")) {
            Button requestsBtn = createMenuItem("📨 Requests", "#9b59b6");
            requestsBtn.setOnAction(e -> {
                popup.hide();
                showRequests(currentUser, stage);
            });
            menu.getChildren().add(requestsBtn);
        }

        if (!currentView.equals("favorites")) {
            Button favoritesBtn = createMenuItem("❤ My Favorites", "#e74c3c");
            favoritesBtn.setOnAction(e -> {
                popup.hide();
                showFavorites(currentUser, stage);
            });
            menu.getChildren().add(favoritesBtn);
        }


        if (!currentView.equals("challenges")) {
            Button challengesBtn = createMenuItem("🏆 Achievements", "#f39c12");
            challengesBtn.setOnAction(e -> {
                popup.hide();
                navigateToChallenges(currentUser, stage);
            });
            menu.getChildren().add(challengesBtn);
        }

        menu.getChildren().add(new Separator());


        Button logoutBtn = createMenuItem("🚪 Logout", "#e74c3c");
        logoutBtn.setOnAction(e -> {
            popup.hide();
            logout(stage);
        });
        menu.getChildren().add(logoutBtn);

        popup.getContent().add(menu);

        double x = menuButton.localToScreen(menuButton.getBoundsInLocal()).getMinX();
        double y = menuButton.localToScreen(menuButton.getBoundsInLocal()).getMaxY() + 5;
        popup.show(menuButton, x, y);
    }
    private static Button createMenuItem(String text, String hoverColor) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hoverColor + "15; -fx-cursor: hand; -fx-padding: 8; " +
                        "-fx-text-fill: " + hoverColor + "; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8;"));

        return btn;
    }
    private static void navigateToMain(User user, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(MenuHelper.class.getResource("main-view.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(user);

            stage.setScene(new Scene(root, 1250, 750));
            stage.setTitle("Library System - " + user.getFullName());

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to main: " + e.getMessage());
            showAlert("Navigation Error", "Could not load main view.");
        }
    }

    private static void navigateToProfile(User user, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(MenuHelper.class.getResource("profile-view.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setCurrentUser(user);

            stage.setScene(new Scene(root, 1000, 750));
            stage.setTitle("My Profile - " + user.getFullName());

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to profile: " + e.getMessage());
            showAlert("Navigation Error", "Could not load profile view.");
        }
    }

    private static void navigateToChallenges(User user, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(MenuHelper.class.getResource("challenges-view.fxml"));
            Parent root = loader.load();

            ChallengeController controller = loader.getController();
            controller.setCurrentUser(user);

            stage.setScene(new Scene(root, 1100, 750));
            stage.setTitle("Challenges - " + user.getFullName());

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to challenges: " + e.getMessage());
            showAlert("Navigation Error", "Could not load challenges view.");
        }
    }


    private static void showRequests(User user, Stage stage) {
        try {
            FollowRequestsDialog dialog = new FollowRequestsDialog(user);
            dialog.show();

        } catch (Exception e) {
            System.out.println(">>> ERROR showing requests dialog: " + e.getMessage());
            showAlert("Error", "Could not load follow activity. Please try again.");
        }
    }

    private static void showFavorites(User user, Stage stage) {
        FavoriteRepository repo = new FavoriteRepository();
        List<Book> favorites = repo.getUserFavorites(user.getUserId());

        FavoritesDialog.show(favorites, book -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        MenuHelper.class.getResource("main-view.fxml")
                );
                Parent root = loader.load();

                MainController controller = loader.getController();
                controller.setCurrentUser(user);
                controller.showBookDetailsDialog(book);

                stage.setScene(new Scene(root, 1000, 700));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void logout(Stage stage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(MenuHelper.class.getResource("login-view.fxml"));
                    Parent root = loader.load();

                    stage.setScene(new Scene(root, 800, 600));
                    stage.setTitle("Library System - Login");

                } catch (Exception e) {
                    System.out.println(">>> ERROR logging out: " + e.getMessage());
                    showAlert("Error", "Could not log out. Please try again.");
                }
            }
        });
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "U";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }
}