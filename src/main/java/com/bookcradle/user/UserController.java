package com.bookcradle.user;

import com.bookcradle.models.Book;
import com.bookcradle.models.User;
import com.bookcradle.services.BookService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class UserController {

    private BookService bookService = new BookService();
    private User currentUser = new User("John", "Doe", "johndoe@example.com", new ArrayList<>());

    @FXML
    private Label userNameLabel;
    @FXML
    private Label userSurnameLabel;
    @FXML
    private VBox borrowedBooksVBox;
    @FXML
    private Pane availableBooksHBox; // works for HBox or FlowPane
    @FXML
    private TextField isbnSearchBar;
    @FXML
    private TextField titleSearchBar;
    @FXML
    private TextField authorSearchBar;
    @FXML
    private Button logoutButton;
    @FXML
    private Label lateFeeLabel;
    @FXML
    private Button payLateFeeButton;

    @FXML
    public void initialize() {
        payLateFeeButton.setOnAction(this::handlePayLateFee);
        logoutButton.setOnAction(this::handleLogout);

        isbnSearchBar.setOnKeyReleased(this::searchBooks);
        titleSearchBar.setOnKeyReleased(this::searchBooks);
        authorSearchBar.setOnKeyReleased(this::searchBooks);
    }

    public void setLoggedInEmail(String email) {
        loadUserInfo(email);
        loadBorrowedBooks(email);
        userNameLabel.setText(currentUser.getName());
        userSurnameLabel.setText(currentUser.getSurname());
        updateBorrowedBooksLabel();
        checkLateFee();
        searchBooks();
    }

    private void loadUserInfo(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader("SignUp_data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("email=" + email)) {
                    String[] parts = line.split(",");
                    String name = "", surname = "";
                    for (String part : parts) {
                        if (part.startsWith("name="))
                            name = part.split("=")[1];
                        if (part.startsWith("surname="))
                            surname = part.split("=")[1];
                    }
                    currentUser = new User(name, surname, email, new ArrayList<>());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBorrowedBooks(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader("UserBooks_data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("email=" + email)) {
                    String[] parts = line.split(",");
                    if (parts.length > 1 && parts[1].startsWith("books=")) {
                        String booksStr = parts[1].substring("books=".length());
                        if (!booksStr.isEmpty()) {
                            String[] bookTitles = booksStr.split(";");
                            List<Book> allBooks = bookService.getAllBooks();
                            List<Book> borrowed = new ArrayList<>();
                            for (String title : bookTitles) {
                                allBooks.stream()
                                        .filter(book -> book.getTitle().equalsIgnoreCase(title.trim()))
                                        .findFirst()
                                        .ifPresent(borrowed::add);
                            }
                            currentUser.setBorrowedBooks(borrowed);
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBorrowedBooks() {
        String email = currentUser.getEmail();
        Map<String, String> allUserBooks = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("UserBooks_data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length >= 1) {
                    String userEmailPart = parts[0];
                    allUserBooks.put(userEmailPart, line);
                }
            }
        } catch (IOException ignored) {
        }

        StringBuilder sb = new StringBuilder();
        sb.append("email=").append(email).append(",books=");
        List<Book> borrowed = currentUser.getBorrowedBooks();
        for (int i = 0; i < borrowed.size(); i++) {
            sb.append(borrowed.get(i).getTitle());
            if (i < borrowed.size() - 1)
                sb.append(";");
        }

        allUserBooks.put("email=" + email, sb.toString());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("UserBooks_data.txt"))) {
            for (String line : allUserBooks.values()) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateBorrowedBooksLabel() {
        borrowedBooksVBox.getChildren().clear();

        for (Book book : currentUser.getBorrowedBooks()) {
            HBox bookCard = new HBox(10);
            bookCard.setStyle(
                    "-fx-background-color: #f9f9f9; -fx-padding: 12px; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #ddd;");
            bookCard.setPrefWidth(480);

            Label bookLabel = new Label(book.getTitle() + " (Due: " + book.getDueDate() + ")");
            bookLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button returnButton = new Button("Return Book");
            returnButton.getStyleClass().add("btn-return-book");
            returnButton.setOnAction(e -> returnBook(book));

            bookCard.getChildren().addAll(bookLabel, spacer, returnButton);
            borrowedBooksVBox.getChildren().add(bookCard);
        }
    }

    @FXML
    public void searchBooks(KeyEvent event) {
        searchBooks();
    }

    @FXML
    public void searchBooks(ActionEvent event) {
        searchBooks();
    }

    private void searchBooks() {
        String isbn = isbnSearchBar.getText().trim();
        String title = titleSearchBar.getText().trim();
        String author = authorSearchBar.getText().trim();

        List<Book> books = bookService.searchBooks(isbn, title, author);
        availableBooksHBox.getChildren().clear();

        if (books.isEmpty()) {
            Label noResult = new Label("No books found.");
            noResult.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            availableBooksHBox.getChildren().add(noResult);
            return;
        }

        for (Book book : books) {
            VBox card = new VBox(10);
            card.getStyleClass().add("book-card");

            Label titleLabel = new Label("Title: " + book.getTitle());
            titleLabel.getStyleClass().add("book-title");

            Label authorLabel = new Label("Author: " + book.getAuthor());
            authorLabel.getStyleClass().add("book-author");

            Button borrowButton = new Button("Borrow Book");
            borrowButton.getStyleClass().add("book-borrow-btn");
            borrowButton.setOnAction(e -> borrowBook(book));

            card.getChildren().addAll(titleLabel, authorLabel, borrowButton);
            availableBooksHBox.getChildren().add(card);
        }
    }

    private void borrowBook(Book book) {
        if (currentUser.getBorrowedBooks().stream().anyMatch(b -> b.getTitle().equalsIgnoreCase(book.getTitle())))
            return;

        book.setDueDate(LocalDate.now().plusDays(14));
        book.setLateFee(0);
        book.setLateFeePaid(false);
        book.setReturned(false);
        book.setReturnedDate(null);

        bookService.borrowBook(currentUser, book);
        saveBorrowedBooks();
        logToFile(currentUser.getEmail(), book.getTitle(), LocalDate.now(), null, 0);
        updateBorrowedBooksLabel();
        checkLateFee();
        searchBooks();
    }

    private void returnBook(Book book) {
        LocalDate returnDate = LocalDate.now();
        bookService.returnBook(currentUser, book, returnDate);

        book.setReturned(true);
        book.setReturnedDate(returnDate);

        double lateFee = 0;
        if (returnDate.isAfter(book.getDueDate())) {
            long daysLate = returnDate.toEpochDay() - book.getDueDate().toEpochDay();
            lateFee = daysLate * 2.0;
        }
        book.setLateFee(lateFee);
        book.setLateFeePaid(false);

        logToFile(currentUser.getEmail(), book.getTitle(), returnDate.minusDays(14), returnDate, lateFee);

        saveBorrowedBooks();
        updateBorrowedBooksLabel();
        checkLateFee();
        searchBooks();
    }

    private void checkLateFee() {
        double totalLateFee = currentUser.getBorrowedBooks().stream()
                .filter(book -> !book.isLateFeePaid())
                .mapToDouble(Book::getLateFee)
                .sum();

        if (totalLateFee > 0) {
            lateFeeLabel.setText("R" + totalLateFee);
            lateFeeLabel.setVisible(true);
            payLateFeeButton.setVisible(true);
        } else {
            lateFeeLabel.setVisible(false);
            payLateFeeButton.setVisible(false);
        }
    }

    @FXML
    public void handlePayLateFee(ActionEvent event) {
        for (Book book : currentUser.getBorrowedBooks()) {
            if (!book.isLateFeePaid()) {
                book.setLateFeePaid(true);
                book.setLateFee(0);
            }
        }
        saveBorrowedBooks();
        updateBorrowedBooksLabel();
        checkLateFee();
    }

    private void logToFile(String userEmail, String bookTitle, LocalDate borrowDate, LocalDate returnDate,
            double lateFee) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("UserLogs_data.txt", true))) {
            String line = userEmail + "," + bookTitle + "," + borrowDate + "," + (returnDate == null ? "" : returnDate)
                    + "," + lateFee;
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/AuthUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene newScene = new Scene(root);
            newScene.getStylesheets().add(getClass().getResource("/styles/AuthStyle.css").toExternalForm());

            stage.setScene(newScene);
            stage.setTitle("BookCradle");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
