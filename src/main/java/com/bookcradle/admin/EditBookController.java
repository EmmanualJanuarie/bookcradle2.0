package com.bookcradle.admin;

import com.bookcradle.models.Book;
import com.bookcradle.services.BookService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EditBookController {

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

    private Book originalBook;

    private final BookService bookService = new BookService();

    public void setBook(Book book) {
        this.originalBook = book;
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        isbnField.setText(book.getIsbn());
        genreField.setText(book.getGenre());
        yearField.setText(Integer.toString(book.getYear()));
        dueDateField.setValue(book.getDueDate());
    }

    @FXML
    private void handleSaveChanges() {
        String newTitle = titleField.getText();
        String newAuthor = authorField.getText();
        String newIsbn = isbnField.getText();
        String newGenre = genreField.getText();

        if (newTitle.isEmpty() || newAuthor.isEmpty() || newIsbn.isEmpty() || newGenre.isEmpty()
                || dueDateField.getValue() == null) {
            showAlert("Error", "All fields must be filled out.");
            return;
        }

        int newYear;
        try {
            newYear = Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Error", "Year must be a valid number.");
            return;
        }

        LocalDate newDueDate = dueDateField.getValue();

        Book updatedBook = new Book(newTitle, newAuthor, newIsbn, newGenre, newYear, newDueDate,
                originalBook.getLateFee());
        bookService.updateBook(originalBook, updatedBook);

        showAlert("Success", "Book updated successfully!");

        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
