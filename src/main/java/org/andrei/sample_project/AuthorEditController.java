package org.andrei.sample_project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;

// Repository imports
import org.andrei.sample_project.repository.AuthorRepository;
import org.andrei.sample_project.repository.BookRepository;
import org.andrei.sample_project.connection.ConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AuthorEditController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ImageView authorImageView;
    @FXML private StackPane avatarStack;
    @FXML private Label avatarInitialsLabel;
    @FXML private Label imagePathLabel;
    @FXML private Label statsBookCountLabel;
    @FXML private Label statsRatingLabel;
    @FXML private TextField nameField;
    @FXML private TextArea biographyArea;
    @FXML private DatePicker birthDatePicker;
    @FXML private DatePicker deathDatePicker;
    @FXML private TextField nationalityField;
    @FXML private TextField websiteField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Integer> bookIdColumn;
    @FXML private TableColumn<Book, String> bookTitleColumn;
    @FXML private TableColumn<Book, Integer> bookYearColumn;
    @FXML private TableColumn<Book, String> bookGenreColumn;
    @FXML private TableColumn<Book, Double> bookRatingColumn;
    @FXML private TableColumn<Book, Void> bookActionsColumn;

    private Author author;
    private AdminController adminController;
    private AuthorRepository authorRepository;
    private BookRepository bookRepository;
    private File selectedImageFile;
    private String originalImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authorRepository = new AuthorRepository();
        bookRepository = new BookRepository();

        setupDatePickers();
        statusComboBox.getItems().addAll("ACTIVE", "INACTIVE", "DECEASED");
        statusComboBox.setValue("ACTIVE");
        setupBooksTable();
        authorImageView.setFitWidth(180);
        authorImageView.setFitHeight(220);
        authorImageView.setPreserveRatio(true);
    }

    private void setupDatePickers() {

        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        birthDatePicker.setConverter(converter);
        deathDatePicker.setConverter(converter);
    }

    private void setupBooksTable() {
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        bookYearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        bookGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        bookRatingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        // Setup actions column
        bookActionsColumn.setCellFactory(param -> new TableCell<Book, Void>() {
            private final Button editButton = new Button("✏️ Edit");
            private final Button deleteButton = new Button("🗑️ Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");

                editButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    onEditBookClick(book);
                });

                deleteButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    onDeleteBookClick(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    public void setAuthor(Author author) {
        this.author = author;
        loadAuthorData();
        loadAuthorBooks();
        updateStats();
    }

    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }
    private void loadAuthorData() {
        if (author == null) return;

        titleLabel.setText("✍Edit Author: " + author.getName());

        nameField.setText(author.getName());
        biographyArea.setText(author.getBiography());
        birthDatePicker.setValue(author.getBirthDate());
        deathDatePicker.setValue(author.getDeathDate());
        nationalityField.setText(author.getNationality());
        websiteField.setText(author.getWebsite());

        if (author.getDeathDate() != null) {
            statusComboBox.setValue("DECEASED");
        } else {
            statusComboBox.setValue("ACTIVE");
        }

        loadAuthorImage();

        if (author.getName() != null && !author.getName().isEmpty()) {
            avatarInitialsLabel.setText(getInitials(author.getName()));
        }
    }

    private void loadAuthorImage() {
        if (author.getProfileImageUrl() != null && !author.getProfileImageUrl().isEmpty()) {
            try {
                originalImagePath = author.getProfileImageUrl();
                if (author.getProfileImageUrl().startsWith("http")) {
                    authorImageView.setImage(new Image(author.getProfileImageUrl()));
                } else {
                    authorImageView.setImage(new Image("file:" + author.getProfileImageUrl()));
                }
                avatarStack.setVisible(false);
                authorImageView.setVisible(true);
                imagePathLabel.setText("Current: " + Paths.get(author.getProfileImageUrl()).getFileName());
            } catch (Exception e) {
                showImageError();
            }
        } else {
            showAvatarFallback();
        }
    }

    private void loadAuthorBooks() {
        if (author == null) return;

        List<Book> books = authorRepository.getBooksByAuthor(author.getAuthorId());
        booksTable.setItems(FXCollections.observableArrayList(books));
    }

    private void updateStats() {
        if (author == null) return;

        int bookCount = booksTable.getItems().size();
        statsBookCountLabel.setText("Books: " + bookCount);

        double avgRating = booksTable.getItems().stream()
                .mapToDouble(Book::getAverageRating)
                .average()
                .orElse(0.0);
        statsRatingLabel.setText(String.format("Avg Rating: %.1f/5", avgRating));
    }

    @FXML
    private void onBackClick() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onSaveClick() {
        if (!validateForm()) {
            return;
        }

        author.setName(nameField.getText().trim());
        author.setBiography(biographyArea.getText().trim());
        author.setBirthDate(birthDatePicker.getValue());
        author.setDeathDate(deathDatePicker.getValue());
        author.setNationality(nationalityField.getText().trim());
        author.setWebsite(websiteField.getText().trim());

        String newImagePath = saveUploadedImage();
        if (newImagePath != null) {
            author.setProfileImageUrl(newImagePath);
        }

        boolean success = authorRepository.updateAuthor(author);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Author '" + author.getName() + "' updated successfully!");

            if (adminController != null) {
                adminController.refreshAuthors();
            }

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update author.");
        }
    }

    @FXML
    private void onUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Author Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        selectedImageFile = fileChooser.showOpenDialog(null);

        if (selectedImageFile != null) {
            try {

                if (selectedImageFile.length() > 5 * 1024 * 1024) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Image file is too large. Maximum size is 5MB.");
                    return;
                }


                Image image = new Image(selectedImageFile.toURI().toString());
                authorImageView.setImage(image);
                authorImageView.setVisible(true);
                avatarStack.setVisible(false);
                imagePathLabel.setText("Selected: " + selectedImageFile.getName());

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image: " + e.getMessage());
                onClearImage();
            }
        }
    }

    @FXML
    private void onClearImage() {
        selectedImageFile = null;
        authorImageView.setImage(null);
        authorImageView.setVisible(false);
        avatarStack.setVisible(true);
        imagePathLabel.setText("No image selected");
    }

    @FXML
    private void onAddBookClick() {
        showAlert(Alert.AlertType.INFORMATION, "Add Book",
                "This would open a dialog to add a new book by this author.\n" +
                        "For now, add books through the main Books tab and assign this author's ID.");
    }

    @FXML
    private void onDeleteClick() {
        if (author == null) return;
        if (!booksTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Cannot Delete",
                    "This author has " + booksTable.getItems().size() + " book(s).\n" +
                            "You must delete or reassign all books before deleting the author.");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Delete Author",
                "Are you sure you want to permanently delete '" + author.getName() + "'?\n" +
                        "This action cannot be undone."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = deleteAuthorFromDatabase(author.getAuthorId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Author deleted!");

                if (adminController != null) {
                    adminController.refreshAuthors();
                }

                Stage stage = (Stage) titleLabel.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete author.");
            }
        }
    }

    private void onEditBookClick(Book book) {
        showAlert(Alert.AlertType.INFORMATION, "Edit Book",
                "Would open edit dialog for: " + book.getTitle() + "\n" +
                        "Implement book editing as needed.");
    }

    private void onDeleteBookClick(Book book) {
        Optional<ButtonType> result = showConfirmation(
                "Delete Book",
                "Are you sure you want to delete '" + book.getTitle() + "'?\n" +
                        "This will also remove the book from all users' reading lists."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = bookRepository.deleteBook(book.getBookId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book deleted!");
                loadAuthorBooks();
                updateStats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete book.");
            }
        }
    }
    private boolean validateForm() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Author name is required!");
            nameField.requestFocus();
            return false;
        }

        LocalDate birthDate = birthDatePicker.getValue();
        LocalDate deathDate = deathDatePicker.getValue();

        if (deathDate != null && birthDate != null) {
            if (deathDate.isBefore(birthDate)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error",
                        "Death date cannot be before birth date!");
                deathDatePicker.requestFocus();
                return false;
            }
        }

        return true;
    }

    private String saveUploadedImage() {
        if (selectedImageFile == null) {
            return originalImagePath; // Return original if no new image
        }

        try {
            Path imagesDir = Paths.get("uploads/authors");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }

            String originalFilename = selectedImageFile.getName();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = "author_" + author.getAuthorId() + "_" + System.currentTimeMillis() + fileExtension;

            Path destination = imagesDir.resolve(newFilename);
            Files.copy(selectedImageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            return "uploads/authors/" + newFilename;

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save image: " + e.getMessage());
            return originalImagePath;
        }
    }

    private boolean deleteAuthorFromDatabase(int authorId) {
        String sql = "DELETE FROM authors WHERE author_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, authorId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deleteAuthorFromDatabase: " + e.getMessage());
            return false;
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "A";

        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    private void showImageError() {
        authorImageView.setVisible(false);
        avatarStack.setVisible(true);
        imagePathLabel.setText("Error loading image");
    }

    private void showAvatarFallback() {
        authorImageView.setVisible(false);
        avatarStack.setVisible(true);
        imagePathLabel.setText("No image - using avatar");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
}