package org.andrei.sample_project;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.andrei.sample_project.BookDialog;
import org.andrei.sample_project.repository.*;

import java.util.List;

/**
 * Displays author profile with photo, biography, and books.
 */
public class AuthorController {

    @FXML private Button menuButton;
    @FXML private ImageView authorImageView;
    @FXML private Label authorInitialsLabel;
    @FXML private Label authorNameLabel;
    @FXML private Label nationalityLabel;
    @FXML private Label lifespanLabel;
    @FXML private Label bookCountLabel;
    @FXML private Label biographyLabel;
    @FXML private StackPane avatarStack;
    @FXML private Label avatarInitialsLabel;
    @FXML private Label websiteLabel;
    @FXML private FlowPane booksContainer;

    private User currentUser;
    private Author author;
    private BookDialog bookDialog;

    private AuthorRepository authorRepository;
    private BookRepository bookRepository;
    private FavoriteRepository favoriteRepository;

    @FXML
    public void initialize() {
        authorRepository = new AuthorRepository();
        bookRepository = new BookRepository();
        favoriteRepository = new FavoriteRepository();

        if (menuButton != null)
            menuButton.setOnAction(e -> onMenuClick());

        System.out.println(">>> AuthorController.initialize()");
    }

    public void setData(User user, Author author) {
        this.currentUser = user;
        this.author = author;
        this.bookDialog = new BookDialog(currentUser);

        System.out.println(">>> AuthorController.setData: " + author.getName());

        loadAuthorInfo();
        loadAuthorBooks();
    }

    private void loadAuthorInfo() {
        System.out.println(">>> Loading author info for: " + author.getName());

        authorNameLabel.setText(author.getName());
        bookCountLabel.setText("📚 " + author.getBookCountDisplay());

        if (author.getNationality() != null && !author.getNationality().isEmpty()) {
            nationalityLabel.setText("🌍 " + author.getNationality());
            nationalityLabel.setVisible(true);
        } else {
            nationalityLabel.setVisible(false);
        }

        if (author.getLifeSpan() != null && !author.getLifeSpan().isEmpty()) {
            lifespanLabel.setText("📅 " + author.getLifeSpan());
            lifespanLabel.setVisible(true);
        } else {
            lifespanLabel.setVisible(false);
        }

        if (author.getBiography() != null && !author.getBiography().isEmpty()) {
            biographyLabel.setText(author.getBiography());
            biographyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");
        } else {
            biographyLabel.setText("No biography available.");
            biographyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 13;");
        }

        if (author.getWebsite() != null && !author.getWebsite().isEmpty()) {
            websiteLabel.setText("🔗 " + author.getWebsite());
            websiteLabel.setVisible(true);
        } else {
            websiteLabel.setVisible(false);
        }

        loadAuthorImage();
    }

    private void loadAuthorImage() {
        System.out.println(">>> Loading author image for: " + author.getName());

        String url = author.getProfileImageUrl();

        try {
            if (url != null && !url.isEmpty()) {
                System.out.println(">>> Trying to load image from URL: " + url);
                Image img = new Image(url, 150, 180, true, true);
                if (!img.isError()) {
                    authorImageView.setImage(img);
                    authorImageView.setVisible(true);

                    // Hide avatar stack
                    if (avatarStack != null) {
                        avatarStack.setVisible(false);
                    }
                    if (authorInitialsLabel != null) {
                        authorInitialsLabel.setVisible(false);
                    }
                    return;
                } else {
                    System.out.println(">>> Image load error");
                }
            }
        } catch (Exception e) {
            System.out.println(">>> Exception loading image: " + e.getMessage());
        }

        System.out.println(">>> Using initials fallback: " + author.getInitials());

        if (avatarStack != null) {
            avatarStack.setVisible(true);
            if (avatarInitialsLabel != null) {
                avatarInitialsLabel.setText(author.getInitials());
            }
        }

        if (authorInitialsLabel != null) {
            authorInitialsLabel.setText(author.getInitials());
            authorInitialsLabel.setVisible(true);
        }

        authorImageView.setVisible(false);
    }
//future implementation
    private void openWebsite(String url) {
        try {
            if (!url.startsWith("http"))
                url = "https://" + url;

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win"))
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            else if (os.contains("mac"))
                Runtime.getRuntime().exec(new String[]{"open", url});
            else
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});

        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, url);
            a.setHeaderText("Copy this link into your browser");
            a.showAndWait();
        }
    }


    private void loadAuthorBooks() {
        System.out.println(">>> Loading books for author: " + author.getName());

        if (booksContainer == null) {
            System.out.println(">>> ERROR: booksContainer is null!");
            return;
        }

        booksContainer.getChildren().clear();
        List<Book> books = authorRepository.getBooksByAuthor(author.getAuthorId());

        System.out.println(">>> Found " + books.size() + " books");

        if (books.isEmpty()) {
            Label emptyLabel = new Label("No books found.");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            booksContainer.getChildren().add(emptyLabel);
            return;
        }

        books.forEach(b -> booksContainer.getChildren().add(createBookCard(b)));
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(8);
        card.setPrefWidth(160);
        card.setMaxWidth(160);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-padding: 15; -fx-background-radius: 12; -fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setOnMouseEntered(e -> card.setStyle("-fx-padding: 15; -fx-background-radius: 12; " +
                "-fx-background-color: #f8f9ff; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 7, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-padding: 15; -fx-background-radius: 12; " +
                "-fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));

        StackPane cover = new StackPane();
        cover.setPrefSize(120, 160);
        cover.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2); " +
                "-fx-background-radius: 8;");

        if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
            try {
                ImageView iv = new ImageView(new Image(book.getCoverImage(), 120, 160, true, true));
                iv.setPreserveRatio(true);
                cover.getChildren().add(iv);
            } catch (Exception e) {
                // Fallback to icon
                Label icon = new Label("📖");
                icon.setStyle("-fx-font-size: 30; -fx-text-fill: white;");
                cover.getChildren().add(icon);
            }
        } else {
            Label icon = new Label("📖");
            icon.setStyle("-fx-font-size: 30; -fx-text-fill: white;");
            cover.getChildren().add(icon);
        }


        if (currentUser != null && favoriteRepository.isFavorite(currentUser.getUserId(), book.getBookId())) {
            Label heart = new Label("❤");
            heart.setStyle("-fx-font-size: 14;");
            StackPane.setAlignment(heart, Pos.TOP_RIGHT);
            StackPane.setMargin(heart, new Insets(5));
            cover.getChildren().add(heart);
        }

        Label title = new Label(book.getTitle());
        title.setWrapText(true);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #333;");
        title.setMaxWidth(130);
        title.setAlignment(Pos.CENTER);

        // Rating label
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER);

        if (book.getAverageRating() > 0) {
            Label rating = new Label(String.format("⭐ %.1f", book.getAverageRating()));
            rating.setStyle("-fx-font-size: 11; -fx-text-fill: #f39c12;");
            ratingBox.getChildren().add(rating);

            if (book.getRatingCount() > 0) {
                Label count = new Label("(" + book.getRatingCount() + ")");
                count.setStyle("-fx-font-size: 10; -fx-text-fill: #888;");
                ratingBox.getChildren().add(count);
            }
        } else {
            Label noRating = new Label("No ratings yet");
            noRating.setStyle("-fx-font-size: 10; -fx-text-fill: #aaa; -fx-font-style: italic;");
            ratingBox.getChildren().add(noRating);
        }

        card.getChildren().addAll(cover, title, ratingBox);
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                if (currentUser != null) {
                    bookDialog.showBookDetails(book);
                } else {
                   //book dialog not loading
                }
            }
        });

        return card;
    }
    @FXML
    private void onMenuClick() {
        Stage stage = (Stage) menuButton.getScene().getWindow();
        MenuHelper.showMenu(menuButton, currentUser, stage, "author");
    }
}