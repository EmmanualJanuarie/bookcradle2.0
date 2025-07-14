package com.bookcradle.admin;

import com.bookcradle.models.Book;
import com.bookcradle.services.BookService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class AddBookViewController {

    private BookService bookService = new BookService();

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
    private DatePicker dueDateField;

    @FXML
    public void handleAddBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        String genre = genreField.getText();
        String yearText = yearField.getText();
        LocalDate dueDate = dueDateField.getValue();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || genre.isEmpty() || yearText.isEmpty()
                || dueDate == null) {
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

        Book book = new Book(title, author, isbn, genre, year, dueDate, 0);
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
        dueDateField.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
