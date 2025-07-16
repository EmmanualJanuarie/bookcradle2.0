package com.bookcradle.admin;

import com.bookcradle.models.Book;
import com.bookcradle.services.BookService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardViewController {

    @javafx.fxml.FXML
    private FlowPane bookCardsPane;

    @FXML private TextField isbnSearchBar;
    @FXML private TextField titleSearchBar;
    @FXML private TextField authorSearchBar;
    @FXML private TextField genreSearchBar;
    @FXML private TextField yearSearchBar;

    private BookService bookService = new BookService();

    @javafx.fxml.FXML
    public void initialize() {
        List<Book> books = bookService.getAllBooks();
        titleSearchBar.setOnKeyReleased(e -> handleSearch());
        updateBookList(books);
    }

    @FXML
    private void handleSearch() {
        String isbn = isbnSearchBar.getText().trim();
        String title = titleSearchBar.getText().trim();
        String author = authorSearchBar.getText().trim();
        String genre = genreSearchBar.getText().trim();
        String yearText = yearSearchBar.getText().trim();

        Integer year = null;
        if (!yearText.isEmpty()) {
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                System.err.println("Invalid year input: " + yearText);
            }
        }

        List<Book> results = bookService.searchBooks(isbn, title, author, genre, year);
        updateBookList(results);
    }


    private void updateBookList(List<Book> books) {
        bookCardsPane.getChildren().clear();

        for (Book book : books) {
            VBox card = new VBox(10);
            card.getStyleClass().add("book-card");
            card.setPadding(new Insets(15));
            card.setPrefWidth(660);

            Label title = new Label(book.getTitle());
            title.getStyleClass().add("book-title");

            Label author = new Label("by " + book.getAuthor());
            author.getStyleClass().add("book-author");

            Label isbn = new Label("ISBN: " + book.getIsbn());
            isbn.getStyleClass().add("book-meta");

            Label genre = new Label("Genre: " + book.getGenre());
            genre.getStyleClass().add("book-meta");

            Label year = new Label("Year: " + book.getYear());
            year.getStyleClass().add("book-meta");

            // Button container
            HBox buttonBox = new HBox(10);
            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("btn-edit");
            editBtn.setOnAction(e -> handleEditBook(book));

            Button removeBtn = new Button("Remove");
            removeBtn.getStyleClass().add("btn-remove");
            removeBtn.setOnAction(e -> handleRemoveBook(book));

            buttonBox.getChildren().addAll(editBtn, removeBtn);

            card.getChildren().addAll(title, author, isbn, genre, year, buttonBox);

            bookCardsPane.getChildren().add(card);
        }
    }

    private void handleEditBook(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/EditBookView.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the book to edit
            EditBookController controller = loader.getController();
            controller.setBook(book);

            // Create new stage (modal window)
            Stage stage = new Stage();
            stage.setTitle("Edit Book - " + book.getTitle());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
            stage.showAndWait();

            // Refresh the book list after edit window closes
            updateBookList(bookService.getAllBooks());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveBook(Book book) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Book");
        confirm.setContentText("Are you sure you want to remove \"" + book.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                bookService.removeBook(book);
                updateBookList(bookService.getAllBooks());
            }
        });
    }
}
