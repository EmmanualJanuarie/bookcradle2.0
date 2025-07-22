package com.bookcradle.user;

import com.bookcradle.models.Book;
import com.bookcradle.models.User;
import com.bookcradle.services.BookService;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    private FlowPane availableBooksHBox;
    @FXML
    private TextField isbnSearchBar;
    @FXML
    private Label notificationBadge;
    @FXML
    private StackPane notificationBell;

    @FXML
    private ListView<String> historyListView;

    @FXML
    private TextField genreSearchBar;

    @FXML
    private TextField yearSearchBar;
    @FXML
    private TextField titleSearchBar;
    @FXML
    private TextField authorSearchBar;
    @FXML
    private Button logoutButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label lateFeeLabel;
    @FXML
    private Button payLateFeeButton;

    @FXML
    public void initialize() {
        payLateFeeButton.setOnAction(this::handlePayLateFee);
        logoutButton.setOnAction(this::handleLogout);
        deleteButton.setOnAction(this::handleDeleteAccount);

        isbnSearchBar.setOnKeyReleased(this::searchBooks);
        titleSearchBar.setOnKeyReleased(this::searchBooks);
        authorSearchBar.setOnKeyReleased(this::searchBooks);
        genreSearchBar.setOnKeyReleased(this::searchBooks);
        yearSearchBar.setOnKeyReleased(this::searchBooks);
    }

    @FXML
    private void handleNotificationClick() {
        showOutstandingNotificationIfNeeded();
    }

    public void setLoggedInEmail(String email) {
        loadUserInfo(email);
        loadBorrowedBooks(email);
        userNameLabel.setText(currentUser.getName());
        userSurnameLabel.setText(currentUser.getSurname());
        updateBorrowedBooksLabel();
        checkLateFee();
        searchBooks();
        loadUserHistory();
        updateNotificationBubble();
        loadBorrowedBooksDetailsFromLibraryData();
    }

    private void showOutstandingNotificationIfNeeded() {
        LocalDate today = LocalDate.now();
        List<Book> borrowedBooks = currentUser.getBorrowedBooks();

        // Filter still borrowed and overdue books
        List<Book> stillBorrowed = borrowedBooks.stream()
                .filter(book -> !book.isReturned())
                .toList();

        List<Book> overdueBooks = stillBorrowed.stream()
                .filter(book -> book.getDueDate() != null && book.getDueDate().isBefore(today))
                .toList();

        double totalLateFees = 0;
        for (Book book : stillBorrowed) {
            if (book.getDueDate() != null && book.getDueDate().isBefore(today)) {
                long daysLate = today.toEpochDay() - book.getDueDate().toEpochDay();
                totalLateFees += daysLate * 20.10;
            }
        }
        for (Book book : borrowedBooks) {
            if (book.isReturned() && book.getLateFee() > 0 && !book.isLateFeePaid()) {
                totalLateFees += book.getLateFee();
            }
        }

        // Capture in effectively final variables for use inside runLater
        final List<Book> booksToShow = stillBorrowed;
        final double feesToShow = totalLateFees;

        Platform.runLater(() -> {
            StringBuilder message = new StringBuilder();
            message.append("📚 Books still borrowed:\n");
            if (booksToShow.isEmpty()) {
                message.append("  None\n");
            } else {
                for (Book book : booksToShow) {
                    message.append("  • ").append(book.getTitle())
                            .append(" (Due: ").append(book.getDueDate()).append(")\n");
                }
            }

            message.append("\n⚠️ Outstanding (Overdue) books:\n");
            if (overdueBooks.isEmpty()) {
                message.append("  None\n");
            } else {
                for (Book book : overdueBooks) {
                    long daysLate = today.toEpochDay() - book.getDueDate().toEpochDay();
                    message.append("  • ").append(book.getTitle())
                            .append(" (Due: ").append(book.getDueDate())
                            .append(", ").append(daysLate).append(" days late)\n");
                }
            }

            message.append("\n💳 Total late fees to pay: R").append(String.format("%.2f", feesToShow));
            message.append(
                    "\n\nPlease pay your late fees at your nearest library or using the 'Pay Fee' button below.");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/styles/UserStyle.css").toExternalForm());
            dialog.setTitle("Library Notification");
            dialog.initStyle(StageStyle.TRANSPARENT);

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(new Label(message.toString()));

            ButtonType payFeeButtonType = new ButtonType("Pay Fee", ButtonBar.ButtonData.OK_DONE);
            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(payFeeButtonType, okButtonType);

            dialogPane.setStyle("""
                        -fx-background-radius: 20;
                        -fx-border-radius: 20;
                        -fx-border-color: #cccccc;
                        -fx-border-width: 2px;
                        -fx-background-color: rgba(255, 255, 255, 0.95);
                        -fx-font-family: 'SF Pro Text', 'Segoe UI', sans-serif;
                        -fx-font-size: 14px;
                        -fx-padding: 20;
                    """);
            ((Label) dialogPane.getContent()).setStyle("""
                        -fx-text-fill: #333;
                        -fx-wrap-text: true;
                        -fx-font-size: 14px;
                        -fx-line-spacing: 4px;
                    """);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == payFeeButtonType) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/PaymentForm.fxml"));
                    System.out.println("FXML URL: " + loader);
                    Parent paymentRoot = loader.load();

                    PaymentController controller = loader.getController();
                    controller.setCurrentUser(currentUser);

                    Stage paymentStage = new Stage();
                    paymentStage.setTitle("Pay Late Fees");
                    paymentStage.setScene(new Scene(paymentRoot));
                    paymentStage.initStyle(StageStyle.UTILITY);

                    paymentStage.setOnHidden(_ -> {
                        saveBorrowedBooks();
                        updateBorrowedBooksLabel();
                        checkLateFee();
                        updateNotificationBubble();
                    });

                    paymentStage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateNotificationBubble() {
        boolean hasBorrowedBooks = currentUser.getBorrowedBooks().stream()
                .anyMatch(book -> !book.isReturned());

        boolean hasOverdue = currentUser.getBorrowedBooks().stream()
                .anyMatch(book -> !book.isReturned() && book.getDueDate() != null
                        && book.getDueDate().isBefore(LocalDate.now()));

        boolean hasLateFees = currentUser.getBorrowedBooks().stream()
                .anyMatch(book -> book.getLateFee() > 0 && !book.isLateFeePaid());

        boolean shouldShowBadge = hasBorrowedBooks || hasOverdue || hasLateFees;
        notificationBadge.setVisible(shouldShowBadge);
    }

    private void loadUserHistory() {
        historyListView.getItems().clear();
        String email = currentUser.getEmail();
        File file = new File("user_history/" + email + "_history.txt");

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    historyListView.getItems().add(line);
                }
            } catch (IOException e) {
                historyListView.getItems().add("⚠️ Could not load history.");
                e.printStackTrace();
            }
        } else {
            historyListView.getItems().add("No history found.");
        }
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

    private void loadBorrowedBooksDetailsFromLibraryData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("LibraryData.txt"))) {
            String line;
            List<Book> borrowedBooks = currentUser.getBorrowedBooks();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String email = parts[0];
                    String bookTitle = parts[1];
                    LocalDate borrowDate = LocalDate.parse(parts[2]);
                    LocalDate returnDate = parts[3].isEmpty() ? null : LocalDate.parse(parts[3]);
                    double lateFee = Double.parseDouble(parts[4]);

                    if (email.equals(currentUser.getEmail())) {
                        for (Book book : borrowedBooks) {
                            if (book.getTitle().equalsIgnoreCase(bookTitle)) {
                                // Update book details
                                if (returnDate == null) {
                                    book.setReturned(false);
                                    book.setDueDate(borrowDate.plusDays(14)); // assuming 14 day borrow period
                                    book.setReturnedDate(null);
                                } else {
                                    book.setReturned(true);
                                    book.setReturnedDate(returnDate);
                                    book.setDueDate(borrowDate.plusDays(14)); // still set due date
                                }
                                book.setLateFee(lateFee);
                                book.setLateFeePaid(lateFee == 0);
                            }
                        }
                    }
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
            if (!book.isReturned()) { // <-- add this check here
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
        String genre = genreSearchBar.getText().trim();
        String yearText = yearSearchBar.getText().trim();

        Integer year = null;
        if (!yearText.isEmpty()) {
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                System.err.println("Invalid year entered: " + yearText);
            }
        }

        List<Book> books = bookService.searchBooks(isbn, title, author, genre, year);
        availableBooksHBox.getChildren().clear();

        if (books.isEmpty()) {
            Label noResult = new Label("No books found.");
            noResult.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            availableBooksHBox.getChildren().add(noResult);
            return;
        }

        for (Book book : books) {
            VBox card = new VBox(25);
            card.getStyleClass().add("book-card");
            card.setPrefWidth(420);
            card.setMinWidth(420);
            card.setMaxWidth(420);

            Label titleLabel = new Label("Title: " + book.getTitle());
            titleLabel.getStyleClass().add("book-title");

            Label authorLabel = new Label("Author: " + book.getAuthor());
            authorLabel.getStyleClass().add("book-author");

            Label genreLabel = new Label("Genre: " + book.getGenre());
            Label yearLabel = new Label("Year: " + book.getYear());

            Button borrowButton = new Button("Borrow Book");
            borrowButton.getStyleClass().add("book-borrow-btn");
            borrowButton.setOnAction(e -> borrowBook(book));

            card.getChildren().addAll(titleLabel, authorLabel, genreLabel, yearLabel, borrowButton);
            availableBooksHBox.getChildren().add(card);
        }
    }

    private void borrowBook(Book book) {
        if (currentUser.getBorrowedBooks().stream().anyMatch(b -> b.getTitle().equalsIgnoreCase(book.getTitle())))
            return;

        book.setDueDate(LocalDate.now().plusDays(10));
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
        updateNotificationBubble();
    }

    private void returnBook(Book book) {
        LocalDate returnDate = LocalDate.now();
        bookService.returnBook(currentUser, book, returnDate);

        book.setReturned(true);
        book.setReturnedDate(returnDate);

        double lateFee = 0;
        if (returnDate.isAfter(book.getDueDate())) {
            long daysLate = returnDate.toEpochDay() - book.getDueDate().toEpochDay();
            lateFee = daysLate * 20.10; // returns late fee of R20.10 for every late book
        }
        book.setLateFee(lateFee);
        book.setLateFeePaid(false);

        logToFile(currentUser.getEmail(), book.getTitle(), returnDate.minusDays(14), returnDate, lateFee);

        saveBorrowedBooks();
        updateBorrowedBooksLabel();
        checkLateFee();
        searchBooks();
        updateNotificationBubble();
    }

    private void checkLateFee() {
        LocalDate today = LocalDate.now();

        double totalLateFee = 0;

        for (Book book : currentUser.getBorrowedBooks()) {
            if (!book.isReturned()) {
                // Book still borrowed — calculate dynamic late fee if overdue
                if (book.getDueDate() != null && book.getDueDate().isBefore(today)) {
                    long daysLate = today.toEpochDay() - book.getDueDate().toEpochDay();
                    totalLateFee += daysLate * 20.10;
                }
            } else {
                // Book returned — use stored late fee if unpaid
                if (book.getLateFee() > 0 && !book.isLateFeePaid()) {
                    totalLateFee += book.getLateFee();
                }
            }
        }

        if (totalLateFee > 0) {
            lateFeeLabel.setText("R" + String.format("%.2f", totalLateFee));
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
        updateNotificationBubble();
    }

    private void logToFile(String userEmail, String bookTitle, LocalDate borrowDate, LocalDate returnDate,
            double lateFee) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("LibraryData.txt", true))) {
            StringBuilder line = new StringBuilder();
            line.append(userEmail).append(",")
                    .append(bookTitle).append(",")
                    .append(borrowDate).append(",")
                    .append(returnDate != null ? returnDate : "").append(",")
                    .append(String.format("%.2f", lateFee));

            if (lateFee > 0 && returnDate != null) {
                line.append(", [PAID]");
            }

            writer.write(line.toString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File dir = new File("user_history");
            if (!dir.exists())
                dir.mkdir();

            File file = new File(dir, userEmail + "_history.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                LocalDate date = (returnDate == null) ? borrowDate : returnDate;
                String entry;

                if (lateFee > 0 && returnDate != null) {
                    entry = String.format("%s | Paid Late Fee | %s | Amount: R%.2f", date, bookTitle, lateFee);
                } else {
                    String action = (returnDate == null) ? "Borrowed" : "Returned";
                    entry = String.format("%s | %s | %s", date, action, bookTitle);
                }

                writer.write(entry);
                writer.newLine();
            }

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

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Are you sure you want to delete your account?");
        confirmation.setContentText("This action is permanent and cannot be undone.");

        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(deleteButton, cancelButton);

        DialogPane dialogPane = confirmation.getDialogPane();

        dialogPane.setStyle("""
                -fx-background-radius: 25;
                -fx-border-radius: 25;
                -fx-border-color: transparent; /* ✨ Removes the border */
                -fx-border-color: #e0e0e0;
                -fx-border-width: 1.5px;
                -fx-background-color: rgba(255, 255, 255, 0.85);
                -fx-font-family: 'SF Pro Text', 'Segoe UI', sans-serif;
                -fx-font-size: 14px;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 4);
                """);

        Node deleteNode = dialogPane.lookupButton(deleteButton);
        if (deleteNode != null) {
            deleteNode.setStyle("""
                    -fx-background-color: #ff3b30;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-background-radius: 20;
                    -fx-padding: 8 20;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);
                    """);
        }

        Node cancelNode = dialogPane.lookupButton(cancelButton);
        if (cancelNode != null) {
            cancelNode.setStyle("""
                    -fx-background-color: #e0e0e0;
                    -fx-text-fill: #333;
                    -fx-font-weight: normal;
                    -fx-background-radius: 20;
                    -fx-padding: 8 20;
                    """);
        }

        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().clear();

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            String email = currentUser.getEmail();
            deleteUserFromSignUpData(email);
            deleteLineFromFile("UserBooks_data.txt", "email=" + email);
            deleteUserHistory(email);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Account Deleted");
            success.setHeaderText(null);
            success.setContentText("Your account has been deleted successfully.");
            success.showAndWait();

            handleLogout(null);
        }
    }

    private void deleteUserFromSignUpData(String email) {
        deleteLineFromFile("SignUp_data.txt", "email=" + email);
    }

    private void deleteUserHistory(String email) {
        File historyFile = new File("user_history/" + email + "_history.txt");
        if (historyFile.exists()) {
            historyFile.delete();
        }
    }

    private void deleteLineFromFile(String filename, String startsWith) {
        File inputFile = new File(filename);
        File tempFile = new File(filename + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith(startsWith))
                    continue;
                writer.write(currentLine);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inputFile.delete()) {
            System.err.println("Could not delete original file");
            return;
        }

        if (!tempFile.renameTo(inputFile)) {
            System.err.println("Could not rename temp file");
        }
    }
}
