package org.andrei.sample_project;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import java.util.List;

public class FavoritesDialog {

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void show(List<Book> favorites, Consumer<Book> onBookSelected) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("❤ Favorite Books");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<Book> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(favorites));

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        authorCol.setPrefWidth(150);

        TableColumn<Book, Double> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        ratingCol.setPrefWidth(80);
        ratingCol.setCellFactory(col -> new TableCell<Book, Double>() {
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

        table.getColumns().addAll(titleCol, authorCol, ratingCol);

        // Double-click handler
        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    Book selectedBook = row.getItem();
                    dialog.close();
                    if (currentUser != null) {
                        new org.andrei.sample_project.BookDialog(currentUser).showBookDetails(selectedBook);
                    } else {
                        onBookSelected.accept(selectedBook);
                    }
                }
            });
            return row;
        });

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        if (favorites.isEmpty()) {
            Label emptyLabel = new Label("No favorite books yet. Add some by clicking ❤ on books!");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
            content.getChildren().add(emptyLabel);
        } else {
            content.getChildren().addAll(new Label("Double-click on a book to view details:"), table);
        }

        dialog.getDialogPane().setContent(content);
        dialog.setResizable(true);
        dialog.showAndWait();
    }
}