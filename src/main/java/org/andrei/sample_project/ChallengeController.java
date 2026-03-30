package org.andrei.sample_project;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.andrei.sample_project.repository.ChallengeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ChallengesController
 * Handles the challenges view with:
 * - Community challenges
 * - Personal reading goals
 * - Badges display
 */
public class ChallengeController {


    @FXML private Label subtitleLabel;
    @FXML private Button backButton;
    @FXML private TabPane challengeTabPane;
    @FXML private Button createChallengeButton;
    @FXML private VBox appChallengesContainer;
    @FXML private VBox personalChallengesContainer;
    @FXML private FlowPane badgesContainer;
    @FXML private Label totalBadgesLabel;
    @FXML private Label completedChallengesLabel;
    private User currentUser;
    private ChallengeRepository challengeRepository;

    @FXML
    public void initialize() {
        System.out.println(">>> ChallengesController.initialize() called");
        challengeRepository = new ChallengeRepository();
        System.out.println(">>> ChallengesController initialization complete");
    }

    /**
     * Sets the current user and loads their challenge data.
     */
    public void setCurrentUser(User user) {
        System.out.println(">>> ChallengesController.setCurrentUser: " + user.getFullName());
        this.currentUser = user;

        subtitleLabel.setText("Welcome, " + user.getFullName() + "! Push yourself to read more!");

        loadAppChallenges();
        loadPersonalChallenges();
        loadBadges();
    }


    private void loadAppChallenges() {
        System.out.println(">>> Loading app challenges...");

        appChallengesContainer.getChildren().clear();

        List<AppChallenge> challenges = challengeRepository.getActiveAppChallenges(currentUser.getUserId());

        if (challenges.isEmpty()) {
            Label emptyLabel = new Label("No active community challenges at the moment. Check back soon!");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
            appChallengesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (AppChallenge challenge : challenges) {
            VBox card = createAppChallengeCard(challenge);
            appChallengesContainer.getChildren().add(card);
        }

        System.out.println(">>> Loaded " + challenges.size() + " app challenges");
    }

    private VBox createAppChallengeCard(AppChallenge challenge) {
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 15;");

        // Header row with icon, title, and status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = challenge.getBadgeIcon() != null ? challenge.getBadgeIcon() : "🏆";
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32;");

        VBox titleBox = new VBox(3);
        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label typeLabel = new Label(challenge.getChallengeTypeDisplay());
        typeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");

        titleBox.getChildren().addAll(titleLabel, typeLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Status badge
        Label statusLabel = new Label(challenge.getStatusString());
        statusLabel.setStyle("-fx-padding: 5 10; -fx-background-radius: 10; " +
                (challenge.isUserCompleted() ? "-fx-background-color: #d4edda; -fx-text-fill: #155724;" :
                        challenge.isUserJoined() ? "-fx-background-color: #fff3cd; -fx-text-fill: #856404;" :
                                "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;"));

        header.getChildren().addAll(iconLabel, titleBox, statusLabel);

        Label descLabel = new Label(challenge.getDescription());
        descLabel.setStyle("-fx-text-fill: #666; -fx-wrap-text: true;");
        descLabel.setWrapText(true);

        HBox details = new HBox(20);
        details.setStyle("-fx-padding: 10 0 0 0;");

        Label goalLabel = new Label("📚 Goal: " + challenge.getTargetBooks() + " book(s)");
        goalLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label genreLabel = new Label("📖 " + challenge.getGenreRequirementDisplay());
        genreLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label dateLabel = new Label("📅 " + challenge.getDaysRemainingString());
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label participantsLabel = new Label("👥 " + challenge.getParticipantsString());
        participantsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        details.getChildren().addAll(goalLabel, genreLabel, dateLabel, participantsLabel);

        VBox progressBox = new VBox(5);
        if (challenge.isUserJoined()) {
            ProgressBar progressBar = new ProgressBar(challenge.getUserProgressPercentage() / 100.0);
            progressBar.setPrefWidth(Double.MAX_VALUE);
            progressBar.setStyle("-fx-accent: #ff9664;");

            Label progressLabel = new Label(challenge.getUserProgressString() + " completed");
            progressLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

            progressBox.getChildren().addAll(progressBar, progressLabel);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 10 0 0 0;");

        if (!challenge.isUserJoined() && challenge.isCurrentlyActive()) {
            Button joinButton = new Button("Join Challenge");
            joinButton.setStyle("-fx-background-color: linear-gradient(to right, #ff9664, #ff6496); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; " +
                    "-fx-padding: 8 20; -fx-cursor: hand;");
            joinButton.setOnAction(e -> joinAppChallenge(challenge));
            actions.getChildren().add(joinButton);
        } else if (challenge.isUserJoined() && !challenge.isUserCompleted()) {
            Label inProgressLabel = new Label("🔥 Keep reading to complete!");
            inProgressLabel.setStyle("-fx-text-fill: #ff9664; -fx-font-weight: bold;");
            actions.getChildren().add(inProgressLabel);
        } else if (challenge.isUserCompleted()) {
            Label completedLabel = new Label(  challenge.getBadgeDisplay() + " earned!");
            completedLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            actions.getChildren().add(completedLabel);
        }

        card.getChildren().addAll(header, descLabel, details);
        if (!progressBox.getChildren().isEmpty()) {
            card.getChildren().add(progressBox);
        }
        card.getChildren().add(actions);

        return card;
    }

    private void joinAppChallenge(AppChallenge challenge) {
        System.out.println(">>> Joining app challenge: " + challenge.getTitle());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Join Challenge");
        confirm.setHeaderText("Join \"" + challenge.getTitle() + "\"?");

        String content = "Goal: Read " + challenge.getTargetBooks() + " book(s)\n";
        if (challenge.getRequiredGenre() != null) {
            content += "Genre: " + challenge.getRequiredGenre() + " books only\n";
        }
        content += "Deadline: " + challenge.getDaysRemainingString() + "\n\n";
        content += "Reward: " + challenge.getBadgeDisplay();

        confirm.setContentText(content);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = challengeRepository.joinAppChallenge(currentUser.getUserId(), challenge.getChallengeId());

            if (success) {
                showAlert("Joined!", "You've joined \"" + challenge.getTitle() + "\"!\n\nStart reading to make progress!");
                loadAppChallenges(); // Refresh
            } else {
                showAlert("Error", "Failed to join challenge. You may have already joined.");
            }
        }
    }

    private void loadPersonalChallenges() {
        System.out.println(">>> Loading personal challenges...");

        personalChallengesContainer.getChildren().clear();

        List<PersonalChallenge> challenges = challengeRepository.getUserPersonalChallenges(currentUser.getUserId());

        if (challenges.isEmpty()) {
            Label emptyLabel = new Label("No personal challenges yet. Create one to track your reading goals!");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
            personalChallengesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (PersonalChallenge challenge : challenges) {
            VBox card = createPersonalChallengeCard(challenge);
            personalChallengesContainer.getChildren().add(card);
        }

        System.out.println(">>> Loaded " + challenges.size() + " personal challenges");
    }

    private VBox createPersonalChallengeCard(PersonalChallenge challenge) {
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 15;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(challenge.getStatusEmoji());
        iconLabel.setStyle("-fx-font-size: 24;");

        VBox titleBox = new VBox(3);
        Label titleLabel = new Label(challenge.getTitle());
        titleLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label dateLabel = new Label(challenge.getDateRangeString());
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");

        titleBox.getChildren().addAll(titleLabel, dateLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label statusLabel = new Label(challenge.getStatusString());
        statusLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

        header.getChildren().addAll(iconLabel, titleBox, statusLabel);

        if (challenge.getDescription() != null && !challenge.getDescription().isEmpty()) {
            Label descLabel = new Label(challenge.getDescription());
            descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
            descLabel.setWrapText(true);
            card.getChildren().add(descLabel);
        }

        HBox progressRow = new HBox(15);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar(challenge.getProgressPercentage() / 100.0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: #6482ff;");

        Label progressLabel = new Label(challenge.getProgressString());
        progressLabel.setStyle("-fx-text-fill: #666;");

        Label daysLabel = new Label(challenge.getDaysRemainingString());
        daysLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");

        progressRow.getChildren().addAll(progressBar, progressLabel, daysLabel);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (!challenge.isCompleted()) {
            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> deletePersonalChallenge(challenge));
            actions.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(header, progressRow, actions);

        return card;
    }

    @FXML
    protected void onCreatePersonalChallenge() {
        System.out.println(">>> Create personal challenge clicked");

        Dialog<PersonalChallenge> dialog = new Dialog<>();
        dialog.setTitle("Create Personal Challenge");
        dialog.setHeaderText("Set your own reading goal!");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("e.g., Summer Reading Goal");

        TextArea descField = new TextArea();
        descField.setPromptText("Optional description...");
        descField.setPrefRowCount(2);

        Spinner<Integer> booksSpinner = new Spinner<>(1, 100, 5);
        booksSpinner.setEditable(true);

        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusMonths(1));

        grid.add(new Label("Challenge Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Books to Read:"), 0, 2);
        grid.add(booksSpinner, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startPicker, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endPicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            createButton.setDisable(newVal.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                PersonalChallenge challenge = new PersonalChallenge();
                challenge.setTitle(titleField.getText().trim());
                challenge.setDescription(descField.getText().trim());
                challenge.setTargetBooks(booksSpinner.getValue());
                challenge.setStartDate(startPicker.getValue());
                challenge.setEndDate(endPicker.getValue());
                return challenge;
            }
            return null;
        });

        Optional<PersonalChallenge> result = dialog.showAndWait();
        result.ifPresent(challenge -> {
            int id = challengeRepository.createPersonalChallenge(
                    currentUser.getUserId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getTargetBooks(),
                    challenge.getStartDate(),
                    challenge.getEndDate()
            );

            if (id > 0) {
                showAlert("Success", "Personal challenge created!\n\n\"" + challenge.getTitle() + "\"\nGoal: " + challenge.getTargetBooks() + " books");
                loadPersonalChallenges();
            } else {
                showAlert("Error", "Failed to create challenge. Please try again.");
            }
        });
    }

    private void deletePersonalChallenge(PersonalChallenge challenge) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Challenge");
        confirm.setHeaderText("Delete \"" + challenge.getTitle() + "\"?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = challengeRepository.deletePersonalChallenge(challenge.getChallengeId());
                if (success) {
                    showAlert("Deleted", "Challenge deleted successfully.");
                    loadPersonalChallenges();
                } else {
                    showAlert("Error", "Failed to delete challenge.");
                }
            }
        });
    }



    private void loadBadges() {
        badgesContainer.getChildren().clear();
        List<UserAppChallenge> completed = challengeRepository.getUserCompletedAppChallenges(currentUser.getUserId());

        totalBadgesLabel.setText(String.valueOf(completed.size()));
        completedChallengesLabel.setText(String.valueOf(completed.size()));

        if (completed.isEmpty()) {
            Label emptyLabel = new Label("Complete challenges to earn badges! 🏆");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
            badgesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (UserAppChallenge challenge : completed) {
            VBox badge = createBadge(challenge);
            badgesContainer.getChildren().add(badge);
        }

        System.out.println(">>> Loaded " + completed.size() + " badges");
    }

    private VBox createBadge(UserAppChallenge challenge) {
        VBox badge = new VBox(8);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-padding: 20; -fx-background-color: linear-gradient(to bottom, #fff9e6, #fff0cc); " +
                "-fx-background-radius: 15; -fx-min-width: 120; -fx-min-height: 120;");

        String icon = challenge.getBadgeIcon() != null ? challenge.getBadgeIcon() : "🏆";
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 36;");

        String name = challenge.getBadgeName() != null ? challenge.getBadgeName() : "Badge";
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #666; -fx-text-alignment: center;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(100);

        Label dateLabel = new Label("Earned: " + challenge.getCompletedDateString());
        dateLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #999;");

        badge.getChildren().addAll(iconLabel, nameLabel, dateLabel);

        badge.setOnMouseEntered(e -> badge.setStyle(badge.getStyle() + "-fx-opacity: 0.8;"));
        badge.setOnMouseExited(e -> badge.setStyle("-fx-padding: 20; -fx-background-color: linear-gradient(to bottom, #fff9e6, #fff0cc); " +
                "-fx-background-radius: 15; -fx-min-width: 120; -fx-min-height: 120;"));

        return badge;
    }


    @FXML
    protected void onBackClick() {
        System.out.println(">>> Menu button clicked");
        Stage stage = (Stage) backButton.getScene().getWindow();
        MenuHelper.showMenu(backButton, currentUser, stage, "challenges");
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}