package org.andrei.sample_project;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import org.andrei.sample_project.repository.UserRepository;
import java.io.IOException;

/**
 * LoginController manages:
 * - user login with username/password
 * - user registration with privacy choice
 * - navigation to appropriate view based on user role
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;
    private UserRepository userRepository;
    @FXML
    public void initialize() {
        userRepository = new UserRepository();
        errorLabel.setVisible(false);
        javafx.application.Platform.runLater(() -> usernameField.requestFocus());
    }

    /**
     * Called when user clicks the Login button.
     */
    @FXML
    protected void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password!");
            return;
        }
        User user = userRepository.login(username, password);
        if (user != null) {
            //login successful
            try {
                String fxmlFile;
                String windowTitle;
                //which view to load based on role
                if (user.isAdmin()) {
                    fxmlFile = "admin-view.fxml";
                    windowTitle = "Library Admin Panel - " + user.getFullName();
                    System.out.println(">>> Admin login: " + user.getUsername() +
                            " | Privacy: " + (user.isPrivate() ? "🔒 Private" : "🌍 Public"));

                } else {
                    fxmlFile = "main-view.fxml";
                    windowTitle = "Library - Welcome " + user.getFullName();
                    System.out.println(">>> User login: " + user.getUsername() +
                            " | Privacy: " + (user.isPrivate() ? "🔒 Private" : "🌍 Public"));
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();

                if (user.isAdmin()) {
                    AdminController adminController = loader.getController();
                    adminController.setCurrentUser(user);
                } else {
                    MainController mainController = loader.getController();
                    mainController.setCurrentUser(user);
                }

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root, 1100, 700));
                stage.setTitle(windowTitle);

            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading application screen!");
            }

        } else {
            showError("Invalid username or password!");
        }
    }


    /**
     * Called when user clicks "Create Account" button.
     * Shows a dialog for registration with privacy option.
     */
    @FXML
    protected void onRegister() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Account");
        dialog.setHeaderText("Register a new account");

        TextField newUsername = new TextField();
        newUsername.setPromptText("Username (3-20 characters)");
        newUsername.setPrefWidth(300);

        TextField newEmail = new TextField();
        newEmail.setPromptText("Email address");
        newEmail.setPrefWidth(300);

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("Password (min 6 characters)");
        newPassword.setPrefWidth(300);

        TextField newFullName = new TextField();
        newFullName.setPromptText("Full Name");
        newFullName.setPrefWidth(300);

        // Privacy selection section
        Label privacyLabel = new Label("Account Privacy:");
        privacyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        RadioButton publicAccount = new RadioButton("🌍 Public Account");
        publicAccount.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 14;");

        RadioButton privateAccount = new RadioButton("🔒 Private Account");
        privateAccount.setStyle("-fx-text-fill: #c62828; -fx-font-size: 14;");

        ToggleGroup privacyGroup = new ToggleGroup();
        publicAccount.setToggleGroup(privacyGroup);
        privateAccount.setToggleGroup(privacyGroup);

        Tooltip publicTooltip = new Tooltip("Anyone can see your profile, reading lists, and activity");
        publicTooltip.setStyle("-fx-font-size: 12;");
        publicAccount.setTooltip(publicTooltip);

        Tooltip privateTooltip = new Tooltip("Only approved followers can see your profile and activity");
        privateTooltip.setStyle("-fx-font-size: 12;");
        privateAccount.setTooltip(privateTooltip);

        publicAccount.setSelected(true);
        Label publicDesc = new Label("✓ Anyone can follow you\n✓ Your reading activity is visible to all");
        publicDesc.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 11; -fx-padding: 0 0 0 25;");

        Label privateDesc = new Label("✓ Follow requests must be approved\n✓ Your activity is private by default");
        privateDesc.setStyle("-fx-text-fill: #f44336; -fx-font-size: 11; -fx-padding: 0 0 0 25;");

        privateDesc.setVisible(false);
        privateDesc.setManaged(false);

        publicAccount.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                publicDesc.setVisible(true);
                publicDesc.setManaged(true);
                privateDesc.setVisible(false);
                privateDesc.setManaged(false);
            }
        });

        privateAccount.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                publicDesc.setVisible(false);
                publicDesc.setManaged(false);
                privateDesc.setVisible(true);
                privateDesc.setManaged(true);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 30, 20, 30));

        grid.add(new Label("Username:*"), 0, 0);
        grid.add(newUsername, 1, 0);
        grid.add(new Label("Email:*"), 0, 1);
        grid.add(newEmail, 1, 1);
        grid.add(new Label("Password:*"), 0, 2);
        grid.add(newPassword, 1, 2);
        grid.add(new Label("Full Name:*"), 0, 3);
        grid.add(newFullName, 1, 3);

        grid.add(new Separator(), 0, 4, 2, 1);
        grid.add(privacyLabel, 0, 5);

        VBox privacyOptions = new VBox(8);
        privacyOptions.getChildren().addAll(publicAccount, publicDesc, privateAccount, privateDesc);
        grid.add(privacyOptions, 1, 5);

        Label infoLabel = new Label("* Required fields\nYou can change privacy settings anytime in your profile");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11; -fx-padding: 10 0 0 0;");
        grid.add(infoLabel, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Create Account");
        okButton.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; " +
                "-fx-padding: 8 20; -fx-background-radius: 5;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #666; " +
                "-fx-font-size: 14; -fx-padding: 8 20; -fx-background-radius: 5;");

        okButton.setDisable(true);

        newUsername.textProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton, newUsername, newEmail, newPassword, newFullName));
        newEmail.textProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton, newUsername, newEmail, newPassword, newFullName));
        newPassword.textProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton, newUsername, newEmail, newPassword, newFullName));
        newFullName.textProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton, newUsername, newEmail, newPassword, newFullName));

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String username = newUsername.getText().trim();
                String email = newEmail.getText().trim().toLowerCase();
                String password = newPassword.getText();
                String fullName = newFullName.getText().trim();
                boolean isPrivate = privateAccount.isSelected();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                    showError("All required fields must be filled!");
                    return;
                }

                // validate username length
                if (username.length() < 3 || username.length() > 20) {
                    showError("Username must be 3-20 characters!");
                    return;
                }

                // validate email format
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showError("Please enter a valid email address!");
                    return;
                }

                // validate password strength
                if (password.length() < 6) {
                    showError("Password must be at least 6 characters long!");
                    return;
                }


                boolean success = userRepository.register(username, email, password, fullName, isPrivate);

                if (success) {

                    errorLabel.setVisible(false);


                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success!");
                    successAlert.setHeaderText("🎉 Account Created Successfully!");

                    String privacyIcon = isPrivate ? "🔒" : "🌍";
                    String privacyStatus = isPrivate ? "Private" : "Public";
                    String privacyDetails = isPrivate ?
                            "• Follow requests must be approved\n• Your activity is private" :
                            "• Anyone can follow you\n• Your reading activity is visible";

                    successAlert.setContentText(
                            "Welcome to the Library, " + fullName + "!\n\n" +
                                    "Account Details:\n" +
                                    "• Username: " + username + "\n" +
                                    "• Email: " + email + "\n" +
                                    "• Privacy: " + privacyIcon + " " + privacyStatus + "\n\n" +
                                    privacyDetails + "\n\n" +
                                    "You can now login with your credentials."
                    );

                    successAlert.getDialogPane().setPrefSize(400, 300);
                    successAlert.showAndWait();


                    usernameField.setText(username);
                    passwordField.clear();
                    usernameField.requestFocus();

                } else {
                    showError("Registration failed. Username or email may already exist.");
                }
            }
        });
    }

    /**
     * Shows an error message to the user.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #db2c2c; -fx-font-weight: bold; " +
                "-fx-background-color: #fff0f0; -fx-background-radius: 5; " +
                "-fx-padding: 10; -fx-border-color: #ffd6e0; " +
                "-fx-border-width: 1; -fx-border-radius: 5;");

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Validates registration form
     */
    private void validateForm(Button okButton, TextField username, TextField email,
                              PasswordField password, TextField fullName) {
        boolean isValid = !username.getText().trim().isEmpty() &&
                !email.getText().trim().isEmpty() &&
                !password.getText().isEmpty() &&
                !fullName.getText().trim().isEmpty() &&
                username.getText().trim().length() >= 3 &&
                password.getText().length() >= 6;

        okButton.setDisable(!isValid);

        if (isValid) {
            okButton.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; " +
                    "-fx-padding: 8 20; -fx-background-radius: 5;");
        } else {
            okButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #888; " +
                    "-fx-font-size: 14; -fx-padding: 8 20; -fx-background-radius: 5;");
        }
    }


    }