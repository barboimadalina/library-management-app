package org.andrei.sample_project;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.andrei.sample_project.BookDialog;
import org.andrei.sample_project.repository.*;

import java.time.LocalDate;
import java.util.List;

/**
 * MainController
 * Main view controller with book lists, search, sidebar features.
 */
public class MainController {
    @FXML private Label welcomeLabel;
    @FXML private Label progressLabel;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button menuButton;
    @FXML private ComboBox<String> searchFilterCombo;
    @FXML private Button refreshButton;
    @FXML private TextField pageNumberField;
    @FXML private Button updateProgressButton;
    @FXML private TabPane tabPane;
    @FXML private TableView<Book> readingTable;
    @FXML private TableColumn<Book, Void> readingCoverCol;
    @FXML private TableColumn<Book, String> readingTitleCol;
    @FXML private TableColumn<Book, String> readingAuthorCol;
    @FXML private TableColumn<Book, String> readingGenreCol;
    @FXML private TableColumn<Book, String> readingProgressCol;
    @FXML private TableView<Book> toReadTable;
    @FXML private TableColumn<Book, Void> toReadCoverCol;
    @FXML private TableColumn<Book, String> toReadTitleCol;
    @FXML private TableColumn<Book, String> toReadAuthorCol;
    @FXML private TableColumn<Book, String> toReadGenreCol;
    @FXML private TableColumn<Book, Double> toReadRatingCol;
    @FXML private TableView<Book> completedTable;
    @FXML private TableColumn<Book, Void> completedCoverCol;
    @FXML private TableColumn<Book, String> completedTitleCol;
    @FXML private TableColumn<Book, String> completedAuthorCol;
    @FXML private TableColumn<Book, String> completedGenreCol;
    @FXML private TableColumn<Book, Double> completedRatingCol;
    @FXML private VBox comingSoonContainer;
    @FXML private VBox topRatedContainer;
    @FXML private Label statsReadingLabel;
    @FXML private Label statsToReadLabel;
    @FXML private Label statsCompletedLabel;
    @FXML private Button viewAllTopRatedBtn;


    private User currentUser;
    private Book selectedReadingBook;
    private volatile boolean isRefreshing = false;

    private BookRepository bookRepository;
    private UserRepository userRepository;
    private ReviewRepository reviewRepository;
    private FavoriteRepository favoriteRepository;
    private ChallengeRepository challengeRepository;
    private FollowRepository followRepository;
    private AuthorRepository authorRepository;
    private ComingSoonRepository comingSoonRepository;

    @FXML
    public void initialize() {
        System.out.println(">>> MainController.initialize()");

        // initialize repositories
        bookRepository = new BookRepository();
        userRepository = new UserRepository();
        reviewRepository = new ReviewRepository();
        favoriteRepository = new FavoriteRepository();
        challengeRepository = new ChallengeRepository();
        followRepository = new FollowRepository();
        authorRepository = new AuthorRepository();
        comingSoonRepository = new ComingSoonRepository();

        //setup search filter
        if (searchFilterCombo != null) {
            searchFilterCombo.setItems(FXCollections.observableArrayList(
                    "🔍 All", "📚 Books", "✍️ Authors", "👤 Users"
            ));
            searchFilterCombo.setValue("🔍 All");
        }


        setupReadingTable();
        setupToReadTable();
        setupCompletedTable();


        updateProgressControls(false);


        readingTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> onReadingBookSelected(newSelection));

        // double click handlers
        setupDoubleClickHandlers();
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> refreshAllData());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println(">>> MainController.setCurrentUser: " + user.getUsername());

        welcomeLabel.setText("Welcome, " + user.getFullName() + "! 📚");
        progressLabel.setText("Select a book to update progress");

        refreshAllData();
    }

    private void setupReadingTable() {
        readingTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        readingAuthorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        readingGenreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        readingProgressCol.setCellValueFactory(new PropertyValueFactory<>("progressDisplay"));
        setupCoverColumn(readingCoverCol);
    }

    private void setupToReadTable() {
        toReadTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        toReadAuthorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        toReadGenreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        toReadRatingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        toReadRatingCol.setCellFactory(col -> new TableCell<Book, Double>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    setText(String.format("⭐ %.1f", rating));
                }
            }
        });

        setupCoverColumn(toReadCoverCol);
    }

    private void setupCompletedTable() {
        completedTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        completedAuthorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        completedGenreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        completedRatingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));


        completedRatingCol.setCellFactory(col -> new TableCell<Book, Double>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    setText(String.format("⭐ %.1f", rating));
                }
            }
        });


        setupCoverColumn(completedCoverCol);
    }

    private void setupCoverColumn(TableColumn<Book, Void> column) {
        column.setCellFactory(col -> new TableCell<Book, Void>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(45);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    String coverUrl = book.getCoverImage();

                    if (coverUrl != null && !coverUrl.isEmpty()) {
                        try {
                            imageView.setImage(new Image(coverUrl, 45, 60, true, true, true));
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(createPlaceholder(book));
                        }
                    } else {
                        setGraphic(createPlaceholder(book));
                    }
                }
            }

            private VBox createPlaceholder(Book book) {
                VBox placeholder = new VBox();
                placeholder.setAlignment(Pos.CENTER);
                placeholder.setPrefSize(45, 60);
                placeholder.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                        "-fx-background-radius: 4;");

                Label icon = new Label("📖");
                icon.setStyle("-fx-font-size: 16;");
                placeholder.getChildren().add(icon);

                return placeholder;
            }
        });
    }

    private void setupDoubleClickHandlers() {
        readingTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selected = readingTable.getSelectionModel().getSelectedItem();
                if (selected != null) showBookOptionsDialog(selected, "READING");
            }
        });

        toReadTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selected = toReadTable.getSelectionModel().getSelectedItem();
                if (selected != null) showBookOptionsDialog(selected, "TO_READ");
            }
        });

        completedTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selected = completedTable.getSelectionModel().getSelectedItem();
                if (selected != null) showBookOptionsDialog(selected, "COMPLETED");
            }
        });
    }

    private void refreshAllData() {
        if (isRefreshing) {
            System.out.println(">>> Already refreshing, skipping...");
            return;
        }

        isRefreshing = true;
        System.out.println(">>> refreshAllData() started");

        Platform.runLater(() -> {
            try {
                loadUserBooks();
                loadComingSoonBooks();
                loadTopRatedPreview();
                updateSidebarStats();
                readingTable.refresh();
                toReadTable.refresh();
                completedTable.refresh();

                System.out.println(">>> refreshAllData() completed");
            } finally {
                isRefreshing = false;
            }
        });
    }

    private void loadUserBooks() {
        System.out.println(">>> Loading user books for user: " + currentUser.getUserId());

        //load reading books
        List<Book> readingBooks = bookRepository.getUserBooksByStatus(currentUser.getUserId(), "READING");
        System.out.println(">>> Loaded " + readingBooks.size() + " reading books");
        readingTable.setItems(FXCollections.observableArrayList(readingBooks));

        //load to-read books
        List<Book> toReadBooks = bookRepository.getUserBooksByStatus(currentUser.getUserId(), "TO_READ");
        System.out.println(">>> Loaded " + toReadBooks.size() + " to-read books");
        toReadTable.setItems(FXCollections.observableArrayList(toReadBooks));

        //load completed books
        List<Book> completedBooks = bookRepository.getUserBooksByStatus(currentUser.getUserId(), "COMPLETED");
        System.out.println(">>> Loaded " + completedBooks.size() + " completed books");
        completedTable.setItems(FXCollections.observableArrayList(completedBooks));

    }

    private void loadComingSoonBooks() {
        if (comingSoonContainer == null) {
            System.out.println(">>> ERROR: comingSoonContainer is null!");
            return;
        }

        System.out.println(">>> loadComingSoonBooks - Starting...");
        comingSoonContainer.getChildren().clear();

        List<ComingSoonBook> books = comingSoonRepository.getUpcomingBooks();
        System.out.println(">>> Retrieved " + books.size() + " coming soon books");

        if (books.isEmpty()) {
            Label empty = new Label("No upcoming releases");
            empty.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            comingSoonContainer.getChildren().add(empty);
            return;
        }

        int count = 0;
        for (ComingSoonBook book : books) {
            if (count >= 3) break;

            VBox card = new VBox(5);
            card.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

            Label title = new Label(book.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
            title.setWrapText(true);

            Label author = new Label("by " + book.getAuthor());
            author.setStyle("-fx-text-fill: #666; -fx-font-size: 10;");

            String countdownText = getCountdownText(book);
            String color = getUrgencyColor(book);

            System.out.println(">>> Book: " + book.getTitle() + ", Countdown: " + countdownText + ", Color: " + color);

            Label countdown = new Label(countdownText);
            countdown.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10; -fx-font-weight: bold;");

            card.getChildren().addAll(title, author, countdown);
            comingSoonContainer.getChildren().add(card);
            count++;
        }
    }

    private String getCountdownText(ComingSoonBook book) {
        if (book.getReleaseDate() == null) {
            return "Release date TBA";
        }

        LocalDate today = LocalDate.now();
        LocalDate releaseDate = book.getReleaseDate();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, releaseDate);

        if (daysUntil == 0) {
            return "Out today!";
        } else if (daysUntil == 1) {
            return "1 day until release";
        } else if (daysUntil > 0) {
            return daysUntil + " days until release";
        } else {
            return "Released";
        }
    }

    private String getUrgencyColor(ComingSoonBook book) {
        if (book.getReleaseDate() == null) {
            return "#666666";
        }

        LocalDate today = LocalDate.now();
        LocalDate releaseDate = book.getReleaseDate();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, releaseDate);

        if (daysUntil <= 0) {
            return "#4CAF50";
        } else if (daysUntil <= 7) {
            return "#FF5722";
        } else if (daysUntil <= 30) {
            return "#2196F3";
        } else {
            return "#666666";
        }
    }

    private void loadTopRatedPreview() {
        if (topRatedContainer == null) return;

        topRatedContainer.getChildren().clear();

        List<Book> topBooks = bookRepository.getTopRatedBooks(5);

        if (topBooks.isEmpty()) {
            Label empty = new Label("No rated books yet");
            empty.setStyle("-fx-text-fill: #999;");
            topRatedContainer.getChildren().add(empty);
            return;
        }

        int rank = 1;
        for (Book book : topBooks) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-cursor: hand;");

            Label rankLabel = new Label("#" + rank);
            rankLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 25; " +
                    "-fx-text-fill: " + (rank <= 3 ? "#f39c12" : "#888") + ";");

            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label title = new Label(book.getTitle());
            title.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
            title.setMaxWidth(150);

            Label rating = new Label("⭐ " + String.format("%.1f", book.getAverageRating()));
            rating.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");

            info.getChildren().addAll(title, rating);
            row.getChildren().addAll(rankLabel, info);

            row.setOnMouseClicked(e -> showBookDetailsDialog(book));
            row.setOnMouseEntered(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: #f0f4ff; -fx-background-radius: 5;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));

            topRatedContainer.getChildren().add(row);
            rank++;
        }
    }

    private void updateSidebarStats() {
        Platform.runLater(() -> {
            if (statsReadingLabel != null) {
                statsReadingLabel.setText(String.valueOf(readingTable.getItems().size()));
            }
            if (statsToReadLabel != null) {
                statsToReadLabel.setText(String.valueOf(toReadTable.getItems().size()));
            }
            if (statsCompletedLabel != null) {
                statsCompletedLabel.setText(String.valueOf(completedTable.getItems().size()));
            }
        });
    }

    @FXML
    protected void onViewTopRated() {
        showTopRatedBooks();
    }

    private void showTopRatedBooks() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("⭐ Top Rated Books");
        dialog.setHeaderText("Highest rated books in our library");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        List<Book> topBooks = bookRepository.getTopRatedBooks(20);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");

        int rank = 1;
        for (Book book : topBooks) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 8; -fx-cursor: hand;");

            Label rankLabel = new Label("#" + rank);
            String badgeColor = rank == 1 ? "#ffd700" : rank == 2 ? "#c0c0c0" : rank == 3 ? "#cd7f32" : "#667eea";
            rankLabel.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15;");

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label title = new Label(book.getTitle());
            title.setStyle("-fx-font-weight: bold;");

            Label author = new Label("by " + book.getAuthorName());
            author.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");

            info.getChildren().addAll(title, author);

            Label rating = new Label(String.format("⭐ %.1f (%d)", book.getAverageRating(), book.getRatingCount()));
            rating.setStyle("-fx-font-size: 13;");

            row.getChildren().addAll(rankLabel, info, rating);

            row.setOnMouseClicked(e -> {
                dialog.close();
                showBookDetailsDialog(book);
            });

            content.getChildren().add(row);
            rank++;
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(500, 400);

        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

    @FXML
    protected void onSearchBooks() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            showAlert("Error", "Please enter a search term!");
            return;
        }

        String filter = searchFilterCombo != null ? searchFilterCombo.getValue() : "🔍 All";

        if (filter.contains("Users")) {
            searchUsers(searchTerm);
        } else if (filter.contains("Authors")) {
            searchAuthors(searchTerm);
        } else if (filter.contains("Books")) {
            searchBooksOnly(searchTerm);
        } else {
            searchAll(searchTerm);
        }
    }

    private void searchAll(String searchTerm) {
        System.out.println(">>> Search All: " + searchTerm);

        List<Book> books = bookRepository.searchBooks(searchTerm);
        List<Author> authors = authorRepository.searchAuthors(searchTerm);
        List<User> users = followRepository.searchUsers(searchTerm, currentUser.getUserId());

        int totalResults = books.size() + authors.size() + users.size();

        if (totalResults == 0) {
            showAlert("No Results", "No results found for: " + searchTerm);
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🔍 Search Results");
        dialog.setHeaderText("Found " + totalResults + " results for: " + searchTerm);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        if (!books.isEmpty()) {
            Tab booksTab = new Tab("📚 Books (" + books.size() + ")");
            ListView<Book> booksList = new ListView<>(FXCollections.observableArrayList(books));
            booksList.setCellFactory(lv -> createBookListCell());
            booksList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Book selected = booksList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        dialog.close();
                        showBookDetailsDialog(selected);
                    }
                }
            });
            booksTab.setContent(booksList);
            tabPane.getTabs().add(booksTab);
        }

        if (!authors.isEmpty()) {
            Tab authorsTab = new Tab("✍️ Authors (" + authors.size() + ")");
            ListView<Author> authorsList = new ListView<>(FXCollections.observableArrayList(authors));
            authorsList.setCellFactory(lv -> createAuthorListCell());
            authorsList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Author selected = authorsList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        dialog.close();
                        navigateToAuthor(selected);
                    }
                }
            });
            authorsTab.setContent(authorsList);
            tabPane.getTabs().add(authorsTab);
        }

        if (!users.isEmpty()) {
            Tab usersTab = new Tab("👤 Users (" + users.size() + ")");
            ListView<User> usersList = new ListView<>(FXCollections.observableArrayList(users));
            usersList.setCellFactory(lv -> createUserListCell());
            usersList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    User selected = usersList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        dialog.close();
                        navigateToUserProfile(selected);
                    }
                }
            });
            usersTab.setContent(usersList);
            tabPane.getTabs().add(usersTab);
        }

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefSize(550, 450);
        dialog.showAndWait();
    }

    private void searchBooksOnly(String searchTerm) {
        System.out.println(">>> Search Books: " + searchTerm);

        List<Book> books = bookRepository.searchBooks(searchTerm);

        if (books.isEmpty()) {
            showAlert("No Results", "No books found for: " + searchTerm);
            return;
        }

        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("📚 Book Search Results");
        dialog.setHeaderText("Found " + books.size() + " book(s)");

        ButtonType viewType = new ButtonType("View Book", ButtonBar.ButtonData.OK_DONE);
        ButtonType addType = new ButtonType("Add to List", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(viewType, addType, ButtonType.CANCEL);

        ListView<Book> listView = new ListView<>(FXCollections.observableArrayList(books));
        listView.setCellFactory(lv -> createBookListCell());

        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().setPrefSize(450, 350);

        Button viewBtn = (Button) dialog.getDialogPane().lookupButton(viewType);
        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addType);
        viewBtn.setDisable(true);
        addBtn.setDisable(true);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            viewBtn.setDisable(n == null);
            addBtn.setDisable(n == null);
        });

        dialog.setResultConverter(btn -> {
            Book selected = listView.getSelectionModel().getSelectedItem();
            if (btn == viewType && selected != null) {
                showBookDetailsDialog(selected);
            } else if (btn == addType && selected != null) {
                showAddToListDialog(selected);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void searchAuthors(String searchTerm) {
        System.out.println(">>> Search Authors: " + searchTerm);

        List<Author> authors = authorRepository.searchAuthors(searchTerm);

        if (authors.isEmpty()) {
            showAlert("No Results", "No authors found for: " + searchTerm);
            return;
        }

        Dialog<Author> dialog = new Dialog<>();
        dialog.setTitle("✍️ Author Search Results");
        dialog.setHeaderText("Found " + authors.size() + " author(s)");

        ButtonType viewType = new ButtonType("View Author", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(viewType, ButtonType.CANCEL);

        ListView<Author> listView = new ListView<>(FXCollections.observableArrayList(authors));
        listView.setCellFactory(lv -> createAuthorListCell());

        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().setPrefSize(450, 350);

        Button viewBtn = (Button) dialog.getDialogPane().lookupButton(viewType);
        viewBtn.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> viewBtn.setDisable(n == null));

        dialog.setResultConverter(btn -> btn == viewType ? listView.getSelectionModel().getSelectedItem() : null);

        dialog.showAndWait().ifPresent(this::navigateToAuthor);
    }

    private void searchUsers(String searchTerm) {
        System.out.println(">>> Search Users: " + searchTerm);

        List<User> users = followRepository.searchUsers(searchTerm, currentUser.getUserId());

        if (users.isEmpty()) {
            showAlert("No Results", "No users found for: " + searchTerm);
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("👤 User Search Results");
        dialog.setHeaderText("Found " + users.size() + " user(s)");

        ButtonType viewType = new ButtonType("View Profile", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(viewType, ButtonType.CANCEL);

        ListView<User> listView = new ListView<>(FXCollections.observableArrayList(users));
        listView.setCellFactory(lv -> createUserListCell());

        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().setPrefSize(450, 350);

        Button viewBtn = (Button) dialog.getDialogPane().lookupButton(viewType);
        viewBtn.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> viewBtn.setDisable(n == null));

        dialog.setResultConverter(btn -> btn == viewType ? listView.getSelectionModel().getSelectedItem() : null);

        dialog.showAndWait().ifPresent(this::navigateToUserProfile);
    }

    private ListCell<Book> createBookListCell() {
        return new ListCell<Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(12);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setStyle("-fx-padding: 5;");

                    VBox info = new VBox(3);
                    Label title = new Label(book.getTitle());
                    title.setStyle("-fx-font-weight: bold;");
                    Label author = new Label("by " + book.getAuthorName());
                    author.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
                    HBox meta = new HBox(10);
                    meta.getChildren().addAll(
                            new Label("📚 " + book.getGenre()),
                            new Label("⭐ " + String.format("%.1f", book.getAverageRating()))
                    );
                    info.getChildren().addAll(title, author, meta);

                    hbox.getChildren().add(info);
                    setGraphic(hbox);
                }
            }
        };
    }

    private ListCell<Author> createAuthorListCell() {
        return new ListCell<Author>() {
            @Override
            protected void updateItem(Author author, boolean empty) {
                super.updateItem(author, empty);
                if (empty || author == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(12);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setStyle("-fx-padding: 5;");

                    Label avatar = new Label(author.getInitials());
                    avatar.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; " +
                            "-fx-background-radius: 20; -fx-min-width: 36; -fx-alignment: center;");

                    VBox info = new VBox(3);
                    Label name = new Label(author.getName());
                    name.setStyle("-fx-font-weight: bold;");

                    HBox meta = new HBox(10);
                    if (author.getNationality() != null) {
                        meta.getChildren().add(new Label("🌍 " + author.getNationality()));
                    }
                    meta.getChildren().add(new Label("📚 " + author.getBookCount() + " books"));

                    info.getChildren().addAll(name, meta);
                    hbox.getChildren().addAll(avatar, info);
                    setGraphic(hbox);
                }
            }
        };
    }

    private ListCell<User> createUserListCell() {
        return new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(12);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setStyle("-fx-padding: 5;");

                    Label avatar = new Label(user.getInitials());
                    avatar.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; " +
                            "-fx-background-radius: 20; -fx-min-width: 36; -fx-alignment: center;");

                    VBox info = new VBox(3);
                    Label name = new Label(user.getFullName());
                    name.setStyle("-fx-font-weight: bold;");
                    Label username = new Label("@" + user.getUsername());
                    username.setStyle("-fx-text-fill: #667eea; -fx-font-size: 11;");

                    info.getChildren().addAll(name, username);
                    hbox.getChildren().addAll(avatar, info);
                    setGraphic(hbox);
                }
            }
        };
    }

    private void onReadingBookSelected(Book book) {
        selectedReadingBook = book;
        if (book != null) {
            updateProgressControls(true);
            progressLabel.setText("Reading: " + book.getTitle() + " (Page " + book.getCurrentPage() +
                    " of " + book.getPageCount() + ")");
        } else {
            updateProgressControls(false);
            progressLabel.setText("Select a book to update progress");
        }
    }

    private void updateProgressControls(boolean enabled) {
        pageNumberField.setDisable(!enabled);
        updateProgressButton.setDisable(!enabled);
        if (!enabled) {
            pageNumberField.clear();
        }
    }

    @FXML
    protected void onUpdateProgress() {
        if (selectedReadingBook == null) {
            showAlert("Error", "Please select a book first!");
            return;
        }

        String pageText = pageNumberField.getText().trim();
        if (pageText.isEmpty()) {
            showAlert("Error", "Please enter a page number!");
            return;
        }

        try {
            int newPage = Integer.parseInt(pageText);
            int totalPages = selectedReadingBook.getPageCount();
            int selectedBookId = selectedReadingBook.getBookId();
            String bookTitle = selectedReadingBook.getTitle();

            if (newPage < 0) {
                showAlert("Error", "Page number cannot be negative!");
                return;
            }


            if (newPage > totalPages) {
                newPage = totalPages;
            }

            // update progress in database
            boolean success = bookRepository.updateReadingProgress(
                    currentUser.getUserId(),
                    selectedBookId,
                    newPage
            );

            if (success) {

                boolean reachedEnd = (newPage >= totalPages);
                boolean nearCompletion = (newPage >= totalPages * 0.95);

                if (reachedEnd) {
                    bookRepository.updateBookStatus(currentUser.getUserId(), selectedBookId, "COMPLETED");

                    Platform.runLater(() -> {
                        refreshAllData();
                        progressLabel.setText("Select a book to update progress");
                        pageNumberField.clear();
                        updateProgressControls(false);


                        showRatingDialog(selectedReadingBook);
                    });

                    showAlert("Congratulations!",
                            "You've finished reading \"" + bookTitle + "\"! It has been marked as completed.");

                } else if (nearCompletion) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Book Almost Complete");
                    confirmAlert.setHeaderText("You've reached page " + newPage + " of " + totalPages);
                    confirmAlert.setContentText("Would you like to mark \"" + bookTitle + "\" as completed?");

                    int finalNewPage = newPage;
                    int finalNewPage1 = newPage;
                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            bookRepository.updateBookStatus(currentUser.getUserId(), selectedBookId, "COMPLETED");

                            Platform.runLater(() -> {
                                refreshAllData();
                                progressLabel.setText("Select a book to update progress");
                                pageNumberField.clear();
                                updateProgressControls(false);

                                // Show rating dialog
                                showRatingDialog(selectedReadingBook);
                            });

                            showAlert("Book Completed!",
                                    "\"" + bookTitle + "\" has been marked as completed!");
                        } else {
                            // Just update progress
                            Platform.runLater(() -> {
                                refreshAllData();
                                progressLabel.setText("Reading: " + bookTitle + " (Page " + finalNewPage +
                                        " of " + totalPages + ")");
                                pageNumberField.clear();
                            });

                            showAlert("Progress Updated",
                                    "Updated to page " + finalNewPage1 + " of " + totalPages);
                        }
                    });
                } else {

                    int finalNewPage2 = newPage;
                    Platform.runLater(() -> {
                        refreshAllData();
                        progressLabel.setText("Reading: " + bookTitle + " (Page " + finalNewPage2 +
                                " of " + totalPages + ")");
                        pageNumberField.clear();
                    });

                    showAlert("Progress Updated",
                            "Updated to page " + newPage + " of " + totalPages);
                }

            } else {
                showAlert("Error", "Could not update progress. Please try again.");
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid page number!");
        }
    }

    private void showBookOptionsDialog(Book book, String currentStatus) {
        new BookDialog(currentUser).showBookOptions(book, currentStatus, this::refreshAllData);
    }

    public void showBookDetailsDialog(Book book) {
        new BookDialog(currentUser).showBookDetails(book);
    }

    private void showAddToListDialog(Book book) {
        System.out.println(">>> showAddToListDialog for: " + book.getTitle());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add to List");
        alert.setHeaderText("Add \"" + book.getTitle() + "\" to:");

        ButtonType toRead = new ButtonType("📚 To Read");
        ButtonType reading = new ButtonType("📖 Currently Reading");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(toRead, reading, cancel);

        alert.showAndWait().ifPresent(response -> {
            if (response == toRead) {
                addBookToListWithRefresh(book, "TO_READ", 1);
            } else if (response == reading) {
                addBookToListWithRefresh(book, "READING", 0);
            }
        });
    }

    private void addBookToListWithRefresh(Book book, String status, int tabIndex) {
        System.out.println(">>> Adding book to " + status + ": " + book.getTitle());

        boolean success = bookRepository.addBookToUserList(currentUser.getUserId(), book.getBookId(), status);

        if (success) {
            // Refresh data immediately
            Platform.runLater(() -> {
                refreshAllData();

                // Switch to the appropriate tab
                if (tabPane != null && tabIndex >= 0 && tabIndex < tabPane.getTabs().size()) {
                    tabPane.getSelectionModel().select(tabIndex);
                    System.out.println(">>> Switched to tab " + tabIndex);
                }

                // Show notification
                String message = status.equals("TO_READ")
                        ? "added to your To Read list."
                        : "added to Currently Reading.";
                showNonBlockingAlert("Added!", "\"" + book.getTitle() + "\" " + message);
            });
        } else {
            showAlert("Error", "Failed to add book. It may already be in your list.");
        }
    }

    private void showRatingDialog(Book book) {
        // Small delay to ensure UI is refreshed first
        new Thread(() -> {
            try {
                Thread.sleep(500); // Wait half a second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                Dialog<Integer> dialog = new Dialog<>();
                dialog.setTitle("⭐ Rate Book");
                dialog.setHeaderText("Rate \"" + book.getTitle() + "\"");

                ButtonType submitType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

                VBox content = new VBox(15);
                content.setStyle("-fx-padding: 20;");
                content.setAlignment(Pos.CENTER);

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
                        for (int j = 0; j < starsBox.getChildren().size(); j++) {
                            ToggleButton s = (ToggleButton) starsBox.getChildren().get(j);
                            s.setText(j < rating ? "⭐" : "☆");
                        }
                    });

                    starsBox.getChildren().add(star);
                }

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
                        String reviewText = reviewArea.getText().trim();
                        boolean success = reviewRepository.addOrUpdateReview(
                                currentUser.getUserId(),
                                book.getBookId(),
                                rating,
                                reviewText.isEmpty() ? null : reviewText
                        );

                        if (success) {
                            showAlert("Thank you!", "Your " + rating + "-star review has been submitted!");
                            refreshAllData(); // Refresh to show new review
                        }
                    }
                });
            });
        }).start();
    }

    @FXML
    protected void onMenuClick() {
        Stage stage = (Stage) menuButton.getScene().getWindow();
        MenuHelper.showMenu(menuButton, currentUser, stage, "main");
    }

    private void navigateToAuthor(Author author) {
        System.out.println(">>> Navigating to author: " + author.getName());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("author-view.fxml"));
            Parent root = loader.load();

            AuthorController controller = loader.getController();
            controller.setData(currentUser, author);

            Stage stage = (Stage) menuButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Author: " + author.getName());

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to author: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToUserProfile(User user) {
        System.out.println(">>> Navigating to user profile: " + user.getUsername());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-profile-view.fxml"));
            Parent root = loader.load();

            UserProfileController controller = loader.getController();
            controller.setUsers(currentUser, user);

            Stage stage = (Stage) menuButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle(user.getFullName() + "'s Profile");

        } catch (Exception e) {
            System.out.println(">>> ERROR navigating to user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showNonBlockingAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show(); // Non-blocking
        });
    }

}