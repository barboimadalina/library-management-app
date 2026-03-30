package org.andrei.sample_project;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import org.andrei.sample_project.repository.*;
import org.andrei.sample_project.connection.ConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * AdminController - Handles the Admin Dashboard
 */
public class AdminController {

    @FXML private Label welcomeLabel;
    @FXML private Label statsLabel;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> joinDateColumn;
    @FXML private TableColumn<User, Integer> booksReadColumn;
    @FXML private TextField userSearchField;

    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> coverColumn;
    @FXML private TableColumn<Book, Integer> bookIdColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> genreColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, Double> ratingColumn;
    @FXML private TextField bookSearchField;
    @FXML private ComboBox<String> bookSortCombo;

    @FXML private TextField titleField;
    @FXML private TextField authorIdField;
    @FXML private TextField genreField;
    @FXML private TextField yearField;
    @FXML private TextField pagesField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView coverPreview;
    @FXML private Label coverPathLabel;
    @FXML private Button uploadImageButton;

    @FXML private TableView<Author> authorsTable;
    @FXML private TableColumn<Author, Integer> authorIdColumn;
    @FXML private TableColumn<Author, String> authorNameColumn;
    @FXML private TableColumn<Author, String> authorNationalityColumn;
    @FXML private TableColumn<Author, String> authorBirthDateColumn;
    @FXML private TableColumn<Author, Integer> authorBookCountColumn;

    @FXML private TextField authorSearchField;
    @FXML private TextField authorNameField;
    @FXML private TextArea authorBioArea;
    @FXML private DatePicker authorBirthDatePicker;
    @FXML private TextField authorNationalityField;
    @FXML private TextField authorWebsiteField;

    @FXML private TableView<Review> reviewsTable;
    @FXML private TableColumn<Review, Integer> reviewIdColumn;
    @FXML private TableColumn<Review, String> reviewBookColumn;
    @FXML private TableColumn<Review, String> reviewUserColumn;
    @FXML private TableColumn<Review, Integer> reviewRatingColumn;
    @FXML private TableColumn<Review, String> reviewTextColumn;
    @FXML private TableColumn<Review, String> reviewDateColumn;
    @FXML private ComboBox<String> reviewFilterCombo;
    @FXML private ComboBox<String> reviewSortCombo;
    @FXML private Label reviewStatsLabel;

    @FXML private TableView<AppChallenge> challengesTable;
    @FXML private TableColumn<AppChallenge, Integer> challengeIdColumn;
    @FXML private TableColumn<AppChallenge, String> challengeTitleColumn;
    @FXML private TableColumn<AppChallenge, String> challengeTypeColumn;
    @FXML private TableColumn<AppChallenge, Integer> challengeTargetColumn;
    @FXML private TableColumn<AppChallenge, String> challengeDatesColumn;
    @FXML private TableColumn<AppChallenge, Integer> challengeParticipantsColumn;


    @FXML private TableView<ComingSoonBook> comingSoonTable;
    @FXML private TableColumn<ComingSoonBook, Integer> csIdColumn;
    @FXML private TableColumn<ComingSoonBook, String> csTitleColumn;
    @FXML private TableColumn<ComingSoonBook, String> csAuthorColumn;
    @FXML private TableColumn<ComingSoonBook, String> csGenreColumn;
    @FXML private TableColumn<ComingSoonBook, String> csReleaseDateColumn;
    @FXML private TableColumn<ComingSoonBook, String> csStatusColumn;

    private User currentUser;
    private UserRepository userRepository;
    private BookRepository bookRepository;
    private AuthorRepository authorRepository;
    private ReviewRepository reviewRepository;
    private ChallengeRepository challengeRepository;
    private File selectedImageFile;
    private Book selectedBookForEdit;
    private Author selectedAuthorForEdit;
    private Review selectedReviewForView;
    private AppChallenge selectedChallengeForView;
    private ComingSoonBook selectedComingSoonForEdit;

    private List<Book> allBooksCache;
    private List<Review> allReviewsCache;

    @FXML
    public void initialize() {
        System.out.println(">>> AdminController.initialize()");

        userRepository = new UserRepository();
        bookRepository = new BookRepository();
        authorRepository = new AuthorRepository();
        reviewRepository = new ReviewRepository();
        challengeRepository = new ChallengeRepository();

        setupUsersTable();
        setupBooksTable();
        setupAuthorsTable();
        setupReviewsTable();
        setupChallengesTable();
        setupComingSoonTable();

        coverPreview.setFitWidth(80);
        coverPreview.setFitHeight(100);
        coverPreview.setPreserveRatio(true);
        try {
            Image placeholder = new Image(getClass().getResourceAsStream("/images/book-placeholder.png"));
            coverPreview.setImage(placeholder);
        } catch (Exception e) {
            System.out.println(">>> Could not load placeholder image: " + e.getMessage());
        }

        reviewFilterCombo.getItems().addAll("All Reviews", "1 Star", "2 Stars", "3 Stars", "4 Stars", "5 Stars");
        reviewFilterCombo.setValue("All Reviews");

        reviewSortCombo.getItems().addAll("Newest First", "Oldest First", "Highest Rating", "Lowest Rating");
        reviewSortCombo.setValue("Newest First");

        bookSortCombo.getItems().addAll("Title A-Z", "Title Z-A", "Rating High-Low", "Rating Low-High",
                "Year New-Old", "Year Old-New");
        bookSortCombo.setValue("Title A-Z");

        setupDatePicker();

        loadInitialData();
    }

    private void setupUsersTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        joinDateColumn.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        booksReadColumn.setCellValueFactory(new PropertyValueFactory<>("booksRead"));
    }

    private void setupBooksTable() {
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        coverColumn.setCellFactory(param -> new TableCell<Book, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        if (imagePath.startsWith("http")) {
                            Image image = new Image(imagePath);
                            imageView.setImage(image);
                        } else {
                            Image image = new Image("file:" + imagePath);
                            imageView.setImage(image);
                        }
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });


        booksTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> onBookSelected());
    }

    private void setupAuthorsTable() {
        authorIdColumn.setCellValueFactory(new PropertyValueFactory<>("authorId"));
        authorNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        authorNationalityColumn.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        authorBirthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        authorBookCountColumn.setCellValueFactory(new PropertyValueFactory<>("bookCount"));

        // Handle author selection
        authorsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> onAuthorSelected());
    }

    private void setupReviewsTable() {
        reviewIdColumn.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        reviewBookColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        reviewUserColumn.setCellValueFactory(new PropertyValueFactory<>("userFullName"));
        reviewRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        reviewTextColumn.setCellValueFactory(new PropertyValueFactory<>("reviewText"));
        reviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Handle review selection
        reviewsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> onReviewSelected());
    }

    private void setupChallengesTable() {
        challengeIdColumn.setCellValueFactory(new PropertyValueFactory<>("challengeId"));
        challengeTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        challengeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("challengeType"));
        challengeTargetColumn.setCellValueFactory(new PropertyValueFactory<>("targetBooks"));
        challengeDatesColumn.setCellValueFactory(new PropertyValueFactory<>("dateRangeString"));
        challengeParticipantsColumn.setCellValueFactory(new PropertyValueFactory<>("totalParticipants"));

        // Handle challenge selection
        challengesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> onChallengeSelected());
    }

    private void setupComingSoonTable() {
        csIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        csTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        csAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        csGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        csReleaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        csStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Handle coming soon selection
        comingSoonTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> onComingSoonSelected());
    }

    private void setupDatePicker() {
        authorBirthDatePicker.setConverter(new StringConverter<LocalDate>() {
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
        });
    }

    private void loadInitialData() {
        loadAllUsers();
        loadAllBooks();
        loadAllAuthors();
        loadAllReviews();
        loadAllChallenges();
        loadAllComingSoon();
        updateStats();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFullName() + " (Admin)");
        updateStats();
    }

    private void loadAllUsers() {
        List<User> users = userRepository.getAllUsers();
        // Remove role column data and calculate books read
        for (User user : users) {
            user.setBooksRead(calculateBooksRead(user.getUserId()));
        }
        usersTable.setItems(FXCollections.observableArrayList(users));
    }

    private int calculateBooksRead(int userId) {
        String sql = "SELECT COUNT(*) FROM user_books WHERE user_id = ? AND status = 'COMPLETED'";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(">>> ERROR in calculateBooksRead: " + e.getMessage());
        }
        return 0;
    }

    private void loadAllBooks() {
        List<Book> books = bookRepository.getAllBooks();
        allBooksCache = new ArrayList<>(books);
        booksTable.setItems(FXCollections.observableArrayList(books));
    }

    private void loadAllAuthors() {
        List<Author> authors = authorRepository.getAllAuthors();
        authorsTable.setItems(FXCollections.observableArrayList(authors));
    }

    private void loadAllReviews() {
        List<Review> reviews = getAllReviewsWithDetails();
        allReviewsCache = new ArrayList<>(reviews);
        reviewsTable.setItems(FXCollections.observableArrayList(reviews));
        updateReviewStats();
    }

    private List<Review> getAllReviewsWithDetails() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as user_name, b.title as book_title " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "JOIN books b ON r.book_id = b.book_id " +
                "ORDER BY r.created_at DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setUserId(rs.getInt("user_id"));
                review.setBookId(rs.getInt("book_id"));
                review.setRating(rs.getInt("rating"));
                review.setReviewText(rs.getString("review_text"));

                if (rs.getTimestamp("created_at") != null) {
                    review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }

                review.setUserFullName(rs.getString("user_name"));
                review.setBookTitle(rs.getString("book_title"));

                reviews.add(review);
            }
        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAllReviewsWithDetails: " + e.getMessage());
        }

        return reviews;
    }

    private void loadAllChallenges() {
        List<AppChallenge> challenges = getAllAppChallenges();
        challengesTable.setItems(FXCollections.observableArrayList(challenges));
    }

    private List<AppChallenge> getAllAppChallenges() {
        List<AppChallenge> challenges = new ArrayList<>();
        String sql = "SELECT ac.*, " +
                "(SELECT COUNT(*) FROM user_app_challenges WHERE challenge_id = ac.challenge_id) as participant_count " +
                "FROM app_challenges ac " +
                "ORDER BY ac.created_at DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AppChallenge challenge = new AppChallenge();
                challenge.setChallengeId(rs.getInt("challenge_id"));
                challenge.setTitle(rs.getString("name"));
                challenge.setDescription(rs.getString("description"));
                challenge.setChallengeType(rs.getString("challenge_type"));
                challenge.setTargetBooks(rs.getInt("target"));
                challenge.setRequiredGenre(rs.getString("required_genre"));

                if (rs.getDate("start_date") != null) {
                    challenge.setStartDate(rs.getDate("start_date").toLocalDate());
                }
                if (rs.getDate("end_date") != null) {
                    challenge.setEndDate(rs.getDate("end_date").toLocalDate());
                }

                challenge.setBadgeName(rs.getString("badge_name"));
                challenge.setBadgeIcon(rs.getString("badge_icon"));
                challenge.setActive(rs.getBoolean("is_active"));
                challenge.setTotalParticipants(rs.getInt("participant_count"));

                if (rs.getTimestamp("created_at") != null) {
                    challenge.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }

                challenges.add(challenge);
            }
        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAllAppChallenges: " + e.getMessage());
        }

        return challenges;
    }

    private void loadAllComingSoon() {
        List<ComingSoonBook> comingSoonBooks = getAllComingSoonBooks();
        comingSoonTable.setItems(FXCollections.observableArrayList(comingSoonBooks));
    }

    private List<ComingSoonBook> getAllComingSoonBooks() {
        List<ComingSoonBook> books = new ArrayList<>();
        String sql = "SELECT * FROM coming_soon_books ORDER BY release_date ASC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ComingSoonBook book = new ComingSoonBook();
                book.setId(rs.getInt("id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setGenre(rs.getString("genre"));
                book.setDescription(rs.getString("description"));

                if (rs.getDate("release_date") != null) {
                    book.setReleaseDate(rs.getDate("release_date").toLocalDate());
                }

                book.setStatus(rs.getString("status"));
                book.setCoverImage(rs.getString("cover_image"));

                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println(">>> ERROR in getAllComingSoonBooks: " + e.getMessage());
        }

        return books;
    }

    private void updateStats() {
        int userCount = usersTable.getItems().size();
        int bookCount = booksTable.getItems().size();
        int authorCount = authorsTable.getItems().size();
        int reviewCount = reviewsTable.getItems().size();
        int challengeCount = challengesTable.getItems().size();
        int comingSoonCount = comingSoonTable.getItems().size();

        statsLabel.setText(String.format("Users: %d | Books: %d | Authors: %d | Reviews: %d | Challenges: %d | Coming Soon: %d",
                userCount, bookCount, authorCount, reviewCount, challengeCount, comingSoonCount));
    }

    private void updateReviewStats() {
        int reviewCount = reviewsTable.getItems().size();
        double avgRating = reviewsTable.getItems().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        reviewStatsLabel.setText(String.format("Total Reviews: %d | Average Rating: %.1f/5", reviewCount, avgRating));
    }

    @FXML
    protected void onSearchUsers() {
        String searchText = userSearchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            loadAllUsers();
            return;
        }

        List<User> filteredUsers = usersTable.getItems().stream()
                .filter(user ->
                        user.getUsername().toLowerCase().contains(searchText) ||
                                user.getFullName().toLowerCase().contains(searchText) ||
                                user.getEmail().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        usersTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }

    @FXML
    protected void onFilterBooks() {
        String searchText = bookSearchField.getText().trim().toLowerCase();
        String sortOption = bookSortCombo.getValue();

        List<Book> filteredBooks = allBooksCache;

        if (!searchText.isEmpty()) {
            filteredBooks = filteredBooks.stream()
                    .filter(book ->
                            book.getTitle().toLowerCase().contains(searchText) ||
                                    book.getAuthorName().toLowerCase().contains(searchText) ||
                                    (book.getGenre() != null && book.getGenre().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
        }

        if (sortOption != null) {
            switch (sortOption) {
                case "Title A-Z":
                    filteredBooks.sort((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
                    break;
                case "Title Z-A":
                    filteredBooks.sort((b1, b2) -> b2.getTitle().compareToIgnoreCase(b1.getTitle()));
                    break;
                case "Rating High-Low":
                    filteredBooks.sort((b1, b2) -> Double.compare(b2.getAverageRating(), b1.getAverageRating()));
                    break;
                case "Rating Low-High":
                    filteredBooks.sort((b1, b2) -> Double.compare(b1.getAverageRating(), b2.getAverageRating()));
                    break;
                case "Year New-Old":
                    filteredBooks.sort((b1, b2) -> Integer.compare(b2.getPublicationYear(), b1.getPublicationYear()));
                    break;
                case "Year Old-New":
                    filteredBooks.sort((b1, b2) -> Integer.compare(b1.getPublicationYear(), b2.getPublicationYear()));
                    break;
            }
        }

        booksTable.setItems(FXCollections.observableArrayList(filteredBooks));
    }

    @FXML
    protected void onFilterReviews() {
        String filter = reviewFilterCombo.getValue();
        String sortOption = reviewSortCombo.getValue();

        List<Review> filteredReviews = allReviewsCache;

        if (filter != null && !filter.equals("All Reviews")) {
            int rating = Integer.parseInt(filter.split(" ")[0]);
            filteredReviews = filteredReviews.stream()
                    .filter(review -> review.getRating() == rating)
                    .collect(Collectors.toList());
        }


        if (sortOption != null) {
            switch (sortOption) {
                case "Newest First":
                    filteredReviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
                    break;
                case "Oldest First":
                    filteredReviews.sort((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));
                    break;
                case "Highest Rating":
                    filteredReviews.sort((r1, r2) -> Integer.compare(r2.getRating(), r1.getRating()));
                    break;
                case "Lowest Rating":
                    filteredReviews.sort((r1, r2) -> Integer.compare(r1.getRating(), r2.getRating()));
                    break;
            }
        }

        reviewsTable.setItems(FXCollections.observableArrayList(filteredReviews));
        updateReviewStats();
    }

    @FXML
    protected void onBookSelected() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            System.out.println("Selected book: " + selectedBook.getTitle());
        }
    }

    @FXML
    protected void onAuthorSelected() {
        Author selectedAuthor = authorsTable.getSelectionModel().getSelectedItem();
        if (selectedAuthor != null) {
            selectedAuthorForEdit = selectedAuthor;
            authorNameField.setText(selectedAuthor.getName());
            authorBioArea.setText(selectedAuthor.getBiography());
            authorBirthDatePicker.setValue(selectedAuthor.getBirthDate());
            authorNationalityField.setText(selectedAuthor.getNationality());
            authorWebsiteField.setText(selectedAuthor.getWebsite());
        }
    }

    @FXML
    protected void onReviewSelected() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            System.out.println("Selected review for book: " + selectedReview.getBookTitle());
        }
    }

    @FXML
    protected void onChallengeSelected() {
        AppChallenge selectedChallenge = challengesTable.getSelectionModel().getSelectedItem();
        if (selectedChallenge != null) {
            System.out.println("Selected challenge: " + selectedChallenge.getTitle());
        }
    }

    @FXML
    protected void onComingSoonSelected() {
        ComingSoonBook selectedBook = comingSoonTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            selectedComingSoonForEdit = selectedBook;
            System.out.println("Selected coming soon book: " + selectedBook.getTitle());
        }
    }

    @FXML
    protected void onViewUserProfile() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a user to view profile!");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("User Profile");
        dialog.setHeaderText("Profile: " + selectedUser.getFullName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        int booksRead = calculateBooksRead(selectedUser.getUserId());
        int reviewsCount = getUserReviewsCount(selectedUser.getUserId());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(new Label(selectedUser.getUsername()), 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(new Label(selectedUser.getFullName()), 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(new Label(selectedUser.getEmail()), 1, 2);
        grid.add(new Label("Joined:"), 0, 3);
        grid.add(new Label(selectedUser.getJoinDate() != null ? selectedUser.getJoinDate() : "N/A"), 1, 3);
        grid.add(new Label("Books Read:"), 0, 4);
        grid.add(new Label(String.valueOf(booksRead)), 1, 4);
        grid.add(new Label("Reviews Written:"), 0, 5);
        grid.add(new Label(String.valueOf(reviewsCount)), 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private int getUserReviewsCount(int userId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(">>> ERROR in getUserReviewsCount: " + e.getMessage());
        }
        return 0;
    }

    @FXML
    protected void onViewEditAuthor() {
        Author selectedAuthor = authorsTable.getSelectionModel().getSelectedItem();

        if (selectedAuthor == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an author to view/edit!");
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("author-edit-view.fxml"));
            Parent root = loader.load();


            AuthorEditController controller = loader.getController();
            controller.setAuthor(selectedAuthor);
            controller.setAdminController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Author: " + selectedAuthor.getName());
            stage.setScene(new Scene(root, 600, 500));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load author edit screen.");
        }
    }

    @FXML
    protected void onSelectAuthor() {
        // Open a dialog to select author from list
        Dialog<Author> dialog = new Dialog<>();
        dialog.setTitle("Select Author");
        dialog.setHeaderText("Choose an author from the list");

        TableView<Author> authorSelectTable = new TableView<>();
        TableColumn<Author, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("authorId"));
        TableColumn<Author, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        authorSelectTable.getColumns().addAll(idCol, nameCol);
        authorSelectTable.setItems(FXCollections.observableArrayList(authorRepository.getAllAuthors()));

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(authorSelectTable);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return authorSelectTable.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Author> result = dialog.showAndWait();
        result.ifPresent(author -> {
            authorIdField.setText(String.valueOf(author.getAuthorId()));
        });
    }

    @FXML
    protected void onAddComingSoon() {
        Dialog<ComingSoonBook> dialog = new Dialog<>();
        dialog.setTitle("Add Coming Soon Book");
        dialog.setHeaderText("Add a new upcoming book release");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title *");
        TextField authorField = new TextField();
        authorField.setPromptText("Author *");
        TextField genreField = new TextField();
        genreField.setPromptText("Genre");
        DatePicker releaseDatePicker = new DatePicker();
        releaseDatePicker.setValue(LocalDate.now().plusMonths(1));
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description...");
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);
        grid.add(new Label("Release Date:"), 0, 3);
        grid.add(releaseDatePicker, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionArea, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                ComingSoonBook book = new ComingSoonBook();
                book.setTitle(titleField.getText().trim());
                book.setAuthor(authorField.getText().trim());
                book.setGenre(genreField.getText().trim());
                book.setReleaseDate(releaseDatePicker.getValue());
                book.setDescription(descriptionArea.getText().trim());
                book.setStatus("UPCOMING");
                return book;
            }
            return null;
        });

        Optional<ComingSoonBook> result = dialog.showAndWait();
        result.ifPresent(book -> {
            boolean success = addComingSoonToDatabase(book);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Coming soon book added!");
                loadAllComingSoon();
                updateStats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add coming soon book.");
            }
        });
    }

    private boolean addComingSoonToDatabase(ComingSoonBook book) {
        String sql = "INSERT INTO coming_soon_books (title, author, genre, description, release_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setString(4, book.getDescription());
            stmt.setDate(5, java.sql.Date.valueOf(book.getReleaseDate()));
            stmt.setString(6, book.getStatus());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addComingSoonToDatabase: " + e.getMessage());
            return false;
        }
    }

    @FXML
    protected void onEditComingSoon() {
        if (selectedComingSoonForEdit == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a coming soon book to edit!");
            return;
        }

        // Similar to onAddComingSoon but with pre-filled data
        showAlert(Alert.AlertType.INFORMATION, "Edit", "Edit functionality for coming soon books will be implemented.");
    }

    @FXML
    protected void onDeleteComingSoon() {
        ComingSoonBook selectedBook = comingSoonTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a coming soon book to delete!");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Delete Coming Soon Book",
                "Are you sure you want to delete '" + selectedBook.getTitle() + "' from coming soon list?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = deleteComingSoonFromDatabase(selectedBook.getId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Coming soon book deleted!");
                loadAllComingSoon();
                updateStats();
                selectedComingSoonForEdit = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete coming soon book.");
            }
        }
    }

    private boolean deleteComingSoonFromDatabase(int id) {
        String sql = "DELETE FROM coming_soon_books WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deleteComingSoonFromDatabase: " + e.getMessage());
            return false;
        }
    }

    @FXML
    protected void onAddBook() {
        // Validation
        String title = titleField.getText().trim();
        String authorIdText = authorIdField.getText().trim();
        String genre = genreField.getText().trim();
        String yearText = yearField.getText().trim();
        String pagesText = pagesField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty() || authorIdText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title and Author ID are required!");
            return;
        }

        int authorId, year = 0, pages = 0;
        try {
            authorId = Integer.parseInt(authorIdText);
            if (!yearText.isEmpty()) year = Integer.parseInt(yearText);
            if (!pagesText.isEmpty()) pages = Integer.parseInt(pagesText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Author ID, Year, and Pages must be numbers!");
            return;
        }


        String coverImagePath = saveUploadedImage();
        boolean success = addBookToDatabase(title, authorId, genre, year, pages, description, coverImagePath);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Book added successfully!" + (coverImagePath != null ? " (with cover image)" : ""));
            loadAllBooks();
            clearBookForm();
            updateStats();
            resetImageSelection();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add book. Check Author ID exists.");
        }
    }

    private boolean addBookToDatabase(String title, int authorId, String genre, int year, int pages,
                                      String description, String coverImagePath) {
        String sql = "INSERT INTO books (title, author_id, genre, publication_year, page_count, description, cover_image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setInt(2, authorId);
            stmt.setString(3, genre);
            if (year > 0) {
                stmt.setInt(4, year);
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            if (pages > 0) {
                stmt.setInt(5, pages);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setString(6, description);
            stmt.setString(7, coverImagePath);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addBookToDatabase: " + e.getMessage());
            return false;
        }
    }

    @FXML
    protected void onUpdateBook() {
        // If we're already editing a book, save the changes
        if (selectedBookForEdit != null) {
            saveUpdatedBook();
            return;
        }

        // Otherwise, load a book for editing
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a book to edit!");
            return;
        }

        selectedBookForEdit = selectedBook;

        // Load data into form
        titleField.setText(selectedBook.getTitle());
        authorIdField.setText(String.valueOf(selectedBook.getAuthorId()));
        genreField.setText(selectedBook.getGenre());
        yearField.setText(String.valueOf(selectedBook.getPublicationYear()));
        pagesField.setText(String.valueOf(selectedBook.getPageCount()));
        descriptionArea.setText(selectedBook.getDescription());

        // Load cover image if exists
        if (selectedBook.getCoverImage() != null && !selectedBook.getCoverImage().isEmpty()) {
            try {
                Image image = new Image("file:" + selectedBook.getCoverImage());
                coverPreview.setImage(image);
                coverPathLabel.setText("Current: " + Paths.get(selectedBook.getCoverImage()).getFileName());
            } catch (Exception e) {
                coverPathLabel.setText("Current image unavailable");
            }
        }

        // Change button text to indicate save mode (optional - if you want dynamic button text)
        // updateBookButton.setText("Save Changes");

        showAlert(Alert.AlertType.INFORMATION, "Edit Mode",
                "Book '" + selectedBook.getTitle() + "' loaded for editing.\n" +
                        "Make your changes and click 'Edit/Update Book' again to save.");
    }

    @FXML
    protected void onDeleteBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a book to delete!");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Delete Book",
                "Are you sure you want to delete '" + selectedBook.getTitle() + "'?\n" +
                        "This will also remove the book from all users' reading lists and reviews."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = bookRepository.deleteBook(selectedBook.getBookId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book deleted!");
                loadAllBooks();
                updateStats();
                if (selectedBookForEdit != null && selectedBookForEdit.getBookId() == selectedBook.getBookId()) {
                    clearBookForm();
                    selectedBookForEdit = null;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete book.");
            }
        }
    }

    @FXML
    protected void onUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Book Cover Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        selectedImageFile = fileChooser.showOpenDialog(null);

        if (selectedImageFile != null) {
            try {
                // Validate file size (max 5MB)
                if (selectedImageFile.length() > 5 * 1024 * 1024) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Image file is too large. Maximum size is 5MB.");
                    return;
                }

                // Display the image
                Image image = new Image(selectedImageFile.toURI().toString());
                coverPreview.setImage(image);
                coverPathLabel.setText("Selected: " + selectedImageFile.getName());

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image: " + e.getMessage());
                resetImageSelection();
            }
        }
    }

    @FXML
    protected void onClearImage() {
        resetImageSelection();
        showAlert(Alert.AlertType.INFORMATION, "Image Cleared", "Selected image has been cleared.");
    }

    @FXML
    protected void onSearchAuthors() {
        String searchText = authorSearchField.getText().trim();

        if (searchText.isEmpty()) {
            loadAllAuthors();
            return;
        }

        List<Author> authors = authorRepository.searchAuthors(searchText);
        authorsTable.setItems(FXCollections.observableArrayList(authors));

        if (authors.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Search Results", "No authors found for: " + searchText);
        }
    }

    @FXML
    protected void onAddAuthor() {
        String name = authorNameField.getText().trim();
        String bio = authorBioArea.getText().trim();
        String nationality = authorNationalityField.getText().trim();
        String website = authorWebsiteField.getText().trim();
        LocalDate birthDate = authorBirthDatePicker.getValue();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Author name is required!");
            return;
        }

        Author author = new Author();
        author.setName(name);
        author.setBiography(bio);
        author.setBirthDate(birthDate);
        author.setNationality(nationality);
        author.setWebsite(website);

        Author addedAuthor = authorRepository.addAuthor(author);

        if (addedAuthor != null && addedAuthor.getAuthorId() > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Author added successfully!");
            loadAllAuthors();
            clearAuthorForm();
            updateStats();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add author.");
        }
    }
    private void saveUpdatedBook() {
        // Validation
        String title = titleField.getText().trim();
        String authorIdText = authorIdField.getText().trim();
        String genre = genreField.getText().trim();
        String yearText = yearField.getText().trim();
        String pagesText = pagesField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty() || authorIdText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title and Author ID are required!");
            return;
        }

        int authorId, year = 0, pages = 0;
        try {
            authorId = Integer.parseInt(authorIdText);
            if (!yearText.isEmpty()) year = Integer.parseInt(yearText);
            if (!pagesText.isEmpty()) pages = Integer.parseInt(pagesText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Author ID, Year, and Pages must be numbers!");
            return;
        }

        // Get cover image path (use existing if no new image selected)
        String coverImagePath = selectedBookForEdit.getCoverImage();
        if (selectedImageFile != null) {
            coverImagePath = saveUploadedImage();
        }

        // Update the book object
        selectedBookForEdit.setTitle(title);
        selectedBookForEdit.setAuthorId(authorId);
        selectedBookForEdit.setGenre(genre);
        selectedBookForEdit.setPublicationYear(year);
        selectedBookForEdit.setPageCount(pages);
        selectedBookForEdit.setDescription(description);
        selectedBookForEdit.setCoverImage(coverImagePath);

        // Save to database using the updateBook method you added to BookRepository
        boolean success = bookRepository.updateBook(selectedBookForEdit);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Book '" + selectedBookForEdit.getTitle() + "' updated successfully!");
            loadAllBooks(); // Refresh the table
            clearBookForm();
            resetImageSelection();
            selectedBookForEdit = null;

            // Reset button text if you changed it (optional)
            // updateBookButton.setText("Edit/Update Book");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update book. Check Author ID exists.");
        }
    }
    @FXML
    protected void onCancelEdit() {
        if (selectedBookForEdit != null) {
            showAlert(Alert.AlertType.INFORMATION, "Edit Cancelled",
                    "Edit mode cancelled for '" + selectedBookForEdit.getTitle() + "'.");
            selectedBookForEdit = null;
            clearBookForm();
            resetImageSelection();
        }
    }
    @FXML
    protected void onUpdateAuthor() {
        if (selectedAuthorForEdit == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an author to update!");
            return;
        }

        String name = authorNameField.getText().trim();
        String bio = authorBioArea.getText().trim();
        String nationality = authorNationalityField.getText().trim();
        String website = authorWebsiteField.getText().trim();
        LocalDate birthDate = authorBirthDatePicker.getValue();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Author name is required!");
            return;
        }

        selectedAuthorForEdit.setName(name);
        selectedAuthorForEdit.setBiography(bio);
        selectedAuthorForEdit.setBirthDate(birthDate);
        selectedAuthorForEdit.setNationality(nationality);
        selectedAuthorForEdit.setWebsite(website);

        boolean success = authorRepository.updateAuthor(selectedAuthorForEdit);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Author updated successfully!");
            loadAllAuthors();
            clearAuthorForm();
            selectedAuthorForEdit = null;
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update author.");
        }
    }

    @FXML
    protected void onDeleteAuthor() {
        Author selectedAuthor = authorsTable.getSelectionModel().getSelectedItem();

        if (selectedAuthor == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an author to delete!");
            return;
        }

        // Check if author has books
        List<Book> authorBooks = authorRepository.getBooksByAuthor(selectedAuthor.getAuthorId());
        if (!authorBooks.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Cannot delete author with existing books!\n" +
                            "This author has " + authorBooks.size() + " book(s).\n" +
                            "Delete or reassign the books first.");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Delete Author",
                "Are you sure you want to delete '" + selectedAuthor.getName() + "'?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = deleteAuthorFromDatabase(selectedAuthor.getAuthorId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Author deleted!");
                loadAllAuthors();
                updateStats();
                if (selectedAuthorForEdit != null && selectedAuthorForEdit.getAuthorId() == selectedAuthor.getAuthorId()) {
                    clearAuthorForm();
                    selectedAuthorForEdit = null;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete author.");
            }
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

    @FXML
    protected void onViewReview() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();

        if (selectedReview == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a review to view!");
            return;
        }

        selectedReviewForView = selectedReview;

        // Show review details in a dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Review Details");
        alert.setHeaderText("Review by: " + selectedReview.getUserFullName());
        alert.setContentText(
                "Book: " + selectedReview.getBookTitle() + "\n" +
                        "Rating: " + selectedReview.getRating() + "/5\n" +
                        "Date: " + (selectedReview.getCreatedAt() != null ?
                        selectedReview.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A") + "\n" +
                        "Comment:\n" + (selectedReview.getReviewText() != null ? selectedReview.getReviewText() : "No comment")
        );
        alert.showAndWait();
    }

    @FXML
    protected void onDeleteReview() {
        Review selectedReview = reviewsTable.getSelectionModel().getSelectedItem();

        if (selectedReview == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a review to delete!");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Delete Review",
                "Are you sure you want to delete this review?\n" +
                        "By: " + selectedReview.getUserFullName() + "\n" +
                        "For: " + selectedReview.getBookTitle()
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = reviewRepository.deleteReview(selectedReview.getUserId(), selectedReview.getBookId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Review deleted!");
                loadAllReviews();
                if (selectedReviewForView != null && selectedReviewForView.getReviewId() == selectedReview.getReviewId()) {
                    selectedReviewForView = null;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete review.");
            }
        }
    }

    private List<String> getGenresFromDatabase() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM books WHERE genre IS NOT NULL AND genre != '' ORDER BY genre";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String genre = rs.getString("genre");
                if (genre != null && !genre.trim().isEmpty()) {
                    // Capitalize first letter of each word for consistency
                    String capitalizedGenre = capitalizeGenre(genre.trim());
                    genres.add(capitalizedGenre);
                }
            }

            System.out.println(">>> Loaded " + genres.size() + " genres from database");

        } catch (SQLException e) {
            System.out.println(">>> ERROR in getGenresFromDatabase: " + e.getMessage());
            e.printStackTrace();
        }

        return genres;
    }

    private String capitalizeGenre(String genre) {
        if (genre == null || genre.isEmpty()) {
            return genre;
        }

        // Split by space or comma
        String[] words = genre.split("[\\s,]+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1).toLowerCase());

                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
        }

        return result.toString();
    }
    @FXML
    protected void onAddChallenge() {

        Dialog<AppChallenge> dialog = new Dialog<>();
        dialog.setTitle("Create New Challenge");
        dialog.setHeaderText("Create a new reading challenge");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Challenge Title");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("SEASONAL", "GENRE", "READING_GOAL", "EVENT");
        typeCombo.setValue("SEASONAL");

        TextField targetField = new TextField();
        targetField.setPromptText("Target number of books");

        ComboBox<String> genreCombo = new ComboBox<>();

        List<String> genres = getGenresFromDatabase();
        genreCombo.getItems().add("Any Genre");
        genreCombo.getItems().addAll(genres);
        genreCombo.setValue("Any Genre");



        TextField badgeNameField = new TextField();
        badgeNameField.setPromptText("Badge Name");

        TextField badgeIconField = new TextField();
        badgeIconField.setPromptText("Badge Icon (emoji)");

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now());

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now().plusMonths(1));

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description...");
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Target Books:"), 0, 2);
        grid.add(targetField, 1, 2);
        grid.add(new Label("Required Genre:"), 0, 3);
        grid.add(genreCombo, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);
        grid.add(new Label("Badge Name:"), 0, 6);
        grid.add(badgeNameField, 1, 6);
        grid.add(new Label("Badge Icon:"), 0, 7);
        grid.add(badgeIconField, 1, 7);
        grid.add(new Label("Description:"), 0, 8);
        grid.add(descriptionArea, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    int target = Integer.parseInt(targetField.getText().trim());
                    AppChallenge challenge = new AppChallenge(
                            titleField.getText().trim(),
                            typeCombo.getValue(),
                            target,
                            startDatePicker.getValue(),
                            endDatePicker.getValue()
                    );
                    challenge.setDescription(descriptionArea.getText().trim());

                    // Handle genre selection
                    String selectedGenre = genreCombo.getValue();
                    if ("Any Genre".equals(selectedGenre) || selectedGenre == null) {
                        challenge.setRequiredGenre(null);
                    } else {
                        challenge.setRequiredGenre(selectedGenre);
                    }

                    challenge.setBadgeName(badgeNameField.getText().trim());
                    challenge.setBadgeIcon(badgeIconField.getText().trim());
                    return challenge;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<AppChallenge> result = dialog.showAndWait();
        result.ifPresent(challenge -> {
            boolean success = addChallengeToDatabase(challenge);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Challenge created successfully!");
                loadAllChallenges();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create challenge.");
            }
        });
    }

    private boolean addChallengeToDatabase(AppChallenge challenge) {
        String sql = "INSERT INTO app_challenges (name, description, challenge_type, target, required_genre, " +
                "start_date, end_date, badge_name, badge_icon, is_active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, challenge.getTitle());
            stmt.setString(2, challenge.getDescription());
            stmt.setString(3, challenge.getChallengeType());
            stmt.setInt(4, challenge.getTargetBooks());
            stmt.setString(5, challenge.getRequiredGenre());
            stmt.setDate(6, java.sql.Date.valueOf(challenge.getStartDate()));
            stmt.setDate(7, java.sql.Date.valueOf(challenge.getEndDate()));
            stmt.setString(8, challenge.getBadgeName());
            stmt.setString(9, challenge.getBadgeIcon());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in addChallengeToDatabase: " + e.getMessage());
            return false;
        }
    }

    @FXML
    protected void onViewChallenge() {
        AppChallenge selectedChallenge = challengesTable.getSelectionModel().getSelectedItem();

        if (selectedChallenge == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a challenge to view!");
            return;
        }

        selectedChallengeForView = selectedChallenge;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Challenge Details");
        alert.setHeaderText(selectedChallenge.getTitle());
        alert.setContentText(
                "Type: " + selectedChallenge.getChallengeTypeDisplay() + "\n" +
                        "Target: " + selectedChallenge.getTargetBooks() + " books\n" +
                        "Required Genre: " + selectedChallenge.getGenreRequirementDisplay() + "\n" +
                        "Date Range: " + selectedChallenge.getDateRangeString() + "\n" +
                        "Participants: " + selectedChallenge.getTotalParticipants() + "\n" +
                        "Badge: " + selectedChallenge.getBadgeDisplay() + "\n" +
                        "Status: " + selectedChallenge.getStatusString() + "\n" +
                        "Description:\n" + (selectedChallenge.getDescription() != null ? selectedChallenge.getDescription() : "No description")
        );

        alert.showAndWait();
    }
    @FXML
    protected void onEditChallenge() {
        AppChallenge selectedChallenge = challengesTable.getSelectionModel().getSelectedItem();

        if (selectedChallenge == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a challenge to edit!");
            return;
        }

        Dialog<AppChallenge> dialog = new Dialog<>();
        dialog.setTitle("Edit Challenge");
        dialog.setHeaderText("Edit: " + selectedChallenge.getTitle());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(selectedChallenge.getTitle());

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("SEASONAL", "GENRE", "READING_GOAL", "EVENT");
        typeCombo.setValue(selectedChallenge.getChallengeType());

        TextField targetField = new TextField(String.valueOf(selectedChallenge.getTargetBooks()));

        ComboBox<String> genreCombo = new ComboBox<>();
        List<String> genres = getGenresFromDatabase();
        genreCombo.getItems().add("Any Genre");
        genreCombo.getItems().addAll(genres);

        String currentGenre = selectedChallenge.getRequiredGenre();
        if (currentGenre == null || currentGenre.isEmpty()) {
            genreCombo.setValue("Any Genre");
        } else {
            genreCombo.setValue(currentGenre);
        }

        TextField badgeNameField = new TextField(selectedChallenge.getBadgeName());
        TextField badgeIconField = new TextField(selectedChallenge.getBadgeIcon());

        DatePicker startDatePicker = new DatePicker(selectedChallenge.getStartDate());
        DatePicker endDatePicker = new DatePicker(selectedChallenge.getEndDate());

        TextArea descriptionArea = new TextArea(selectedChallenge.getDescription());
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Target Books:"), 0, 2);
        grid.add(targetField, 1, 2);
        grid.add(new Label("Required Genre:"), 0, 3);
        grid.add(genreCombo, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);
        grid.add(new Label("Badge Name:"), 0, 6);
        grid.add(badgeNameField, 1, 6);
        grid.add(new Label("Badge Icon:"), 0, 7);
        grid.add(badgeIconField, 1, 7);
        grid.add(new Label("Description:"), 0, 8);
        grid.add(descriptionArea, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int target = Integer.parseInt(targetField.getText().trim());
                    AppChallenge updatedChallenge = new AppChallenge(
                            titleField.getText().trim(),
                            typeCombo.getValue(),
                            target,
                            startDatePicker.getValue(),
                            endDatePicker.getValue()
                    );
                    updatedChallenge.setChallengeId(selectedChallenge.getChallengeId());
                    updatedChallenge.setDescription(descriptionArea.getText().trim());

                    // Handle genre selection
                    String selectedGenre = genreCombo.getValue();
                    if ("Any Genre".equals(selectedGenre) || selectedGenre == null) {
                        updatedChallenge.setRequiredGenre(null);
                    } else {
                        updatedChallenge.setRequiredGenre(selectedGenre);
                    }

                    updatedChallenge.setBadgeName(badgeNameField.getText().trim());
                    updatedChallenge.setBadgeIcon(badgeIconField.getText().trim());
                    updatedChallenge.setActive(selectedChallenge.isActive());

                    return updatedChallenge;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<AppChallenge> result = dialog.showAndWait();
        result.ifPresent(challenge -> {
            boolean success = updateChallengeInDatabase(challenge);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Challenge updated successfully!");
                loadAllChallenges();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update challenge.");
            }
        });
    }

    private boolean updateChallengeInDatabase(AppChallenge challenge) {
        String sql = "UPDATE app_challenges SET " +
                "name = ?, description = ?, challenge_type = ?, target = ?, " +
                "required_genre = ?, start_date = ?, end_date = ?, " +
                "badge_name = ?, badge_icon = ? " +
                "WHERE challenge_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, challenge.getTitle());
            stmt.setString(2, challenge.getDescription());
            stmt.setString(3, challenge.getChallengeType());
            stmt.setInt(4, challenge.getTargetBooks());

            if (challenge.getRequiredGenre() != null && !challenge.getRequiredGenre().isEmpty()) {
                stmt.setString(5, challenge.getRequiredGenre());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.setDate(6, java.sql.Date.valueOf(challenge.getStartDate()));
            stmt.setDate(7, java.sql.Date.valueOf(challenge.getEndDate()));
            stmt.setString(8, challenge.getBadgeName());
            stmt.setString(9, challenge.getBadgeIcon());
            stmt.setInt(10, challenge.getChallengeId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println(">>> ERROR in updateChallengeInDatabase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    @FXML
    protected void onDeleteChallenge() {
        AppChallenge selectedChallenge = challengesTable.getSelectionModel().getSelectedItem();

        if (selectedChallenge == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a challenge to delete!");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Delete Challenge",
                "Are you sure you want to delete '" + selectedChallenge.getTitle() + "'?\n" +
                        "This will also remove all user progress for this challenge."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = deleteChallengeFromDatabase(selectedChallenge.getChallengeId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Challenge deleted!");
                loadAllChallenges();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete challenge.");
            }
        }
    }

    private boolean deleteChallengeFromDatabase(int challengeId) {
        // First delete user progress
        String deleteProgressSql = "DELETE FROM user_app_challenges WHERE challenge_id = ?";
        String deleteChallengeSql = "DELETE FROM app_challenges WHERE challenge_id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(deleteProgressSql);
                 PreparedStatement stmt2 = conn.prepareStatement(deleteChallengeSql)) {

                // Delete user progress first
                stmt1.setInt(1, challengeId);
                stmt1.executeUpdate();

                // Delete the challenge
                stmt2.setInt(1, challengeId);
                int rows = stmt2.executeUpdate();

                conn.commit();
                return rows > 0;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println(">>> ERROR in deleteChallengeFromDatabase: " + e.getMessage());
            return false;
        }
    }

    @FXML
    protected void onRefresh() {
        loadAllUsers();
        loadAllBooks();
        loadAllAuthors();
        loadAllReviews();
        loadAllChallenges();
        loadAllComingSoon();
        updateStats();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "All data reloaded from database.");
    }

    @FXML
    protected void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Library App - Login");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load login screen.");
        }
    }

    private void clearBookForm() {
        titleField.clear();
        authorIdField.clear();
        genreField.clear();
        yearField.clear();
        pagesField.clear();
        descriptionArea.clear();
    }

    private void clearAuthorForm() {
        authorNameField.clear();
        authorBioArea.clear();
        authorBirthDatePicker.setValue(null);
        authorNationalityField.clear();
        authorWebsiteField.clear();
    }

    private void resetImageSelection() {
        selectedImageFile = null;
        coverPreview.setImage(null);
        try {
            Image placeholder = new Image(getClass().getResourceAsStream("/images/book-placeholder.png"));
            coverPreview.setImage(placeholder);
        } catch (Exception e) {
            // Ignore
        }
        coverPathLabel.setText("No image selected");
    }

    private String saveUploadedImage() {
        if (selectedImageFile == null) {
            return null;
        }

        try {
            Path imagesDir = Paths.get("uploaded-images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }

            String originalFilename = selectedImageFile.getName();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = "book_" + System.currentTimeMillis() + fileExtension;

            Path destination = imagesDir.resolve(newFilename);
            Files.copy(selectedImageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            return "uploaded-images/" + newFilename;

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save image: " + e.getMessage());
            return null;
        }
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


    public void refreshAuthors() {
        loadAllAuthors();
    }
}