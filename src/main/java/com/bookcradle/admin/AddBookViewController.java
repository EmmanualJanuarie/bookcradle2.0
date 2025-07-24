package com.bookcradle.admin;

import com.bookcradle.models.Book;
import com.bookcradle.services.BookService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AddBookViewController {

    private final BookService bookService = new BookService();

    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField isbnField;
    @FXML
    private TextField genreField;
    @FXML
    private TextField yearField;

    @FXML
    public void initialize() {
        // No dueDateField anymore
    }

    @FXML
    public void handleAddBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        String genre = genreField.getText();
        String yearText = yearField.getText();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || genre.isEmpty() || yearText.isEmpty()) {
            showAlert("Error", "All fields must be filled out!");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Year must be a valid number!");
            return;
        }

        // Set dueDate to null because it's set during borrowing
        Book book = new Book(title, author, isbn, genre, year, null, 0);
        bookService.addBook(book);

        showAlert("Success", "Book added successfully!");
        clearFields();
    }

    private void clearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        genreField.clear();
        yearField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
