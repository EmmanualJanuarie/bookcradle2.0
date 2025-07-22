package com.bookcradle.user;

import com.bookcradle.models.Book;
import com.bookcradle.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class PaymentController {

    @FXML
    private TextField cardNumberField, cardNameField, ccvField, expiryDateField;
    @FXML
    private Label errorLabel, amountDueLabel;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        displayTotalAmount();
    }

    private void displayTotalAmount() {
        double totalLateFee = 0;
        for (Book book : currentUser.getBorrowedBooks()) {
            if (!book.isReturned() && book.getDueDate() != null && book.getDueDate().isBefore(LocalDate.now())) {
                long daysLate = LocalDate.now().toEpochDay() - book.getDueDate().toEpochDay();
                totalLateFee += daysLate * 20.10;
            } else if (book.getLateFee() > 0 && !book.isLateFeePaid()) {
                totalLateFee += book.getLateFee();
            }
        }
        amountDueLabel.setText("R" + String.format("%.2f", totalLateFee));
    }

    @FXML
    private void handleConfirmPayment() {
        if (!validateCardDetails()) {
            errorLabel.setText("❌ Invalid card details. Please double-check.");
            return;
        }

        boolean paymentMade = false;

        for (Book book : currentUser.getBorrowedBooks()) {
            if (!book.isLateFeePaid() && (book.getLateFee() > 0
                    || (book.getDueDate() != null && book.getDueDate().isBefore(LocalDate.now())))) {

                double calculatedLateFee = 0;

                if (book.getLateFee() > 0) {
                    calculatedLateFee = book.getLateFee();
                } else if (book.getDueDate() != null && book.getDueDate().isBefore(LocalDate.now())) {
                    long daysLate = LocalDate.now().toEpochDay() - book.getDueDate().toEpochDay();
                    calculatedLateFee = daysLate * 20.10;
                }

                // Finalize payment
                book.setLateFeePaid(true);
                book.setLateFee(0);

                if (!book.isReturned()) {
                    book.setReturned(true);
                    book.setReturnedDate(LocalDate.now());
                }

                // Log with actual paid fee and [PAID] tag
                logToFile(currentUser.getEmail(), book.getTitle(),
                        book.getDueDate() != null ? book.getDueDate().minusDays(14) : LocalDate.now().minusDays(14),
                        LocalDate.now(), calculatedLateFee);

                // Log to user history
                logUserHistory(currentUser.getEmail(), book.getTitle(), LocalDate.now());

                paymentMade = true;
            }
        }

        if (paymentMade)

        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText(null);
            alert.setContentText("✅ Your late fees have been cleared.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Payment Needed");
            alert.setHeaderText(null);
            alert.setContentText("You have no outstanding fees.");
            alert.showAndWait();
        }

        // Remove returned & paid books from user view
        currentUser.getBorrowedBooks().removeIf(book -> book.isReturned() && book.isLateFeePaid());

        // Close payment window
        ((Stage) cardNumberField.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancelPayment() {
        ((Stage) cardNumberField.getScene().getWindow()).close();
    }

    private boolean validateCardDetails() {
        return cardNumberField.getText().matches("\\d{16}")
                && cardNameField.getText().trim().length() > 0
                && ccvField.getText().matches("\\d{3}")
                && expiryDateField.getText().matches("(0[1-9]|1[0-2])/\\d{2}");
    }

    private void logToFile(String userEmail, String bookTitle, LocalDate borrowDate, LocalDate returnDate,
            double lateFee) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("LibraryData.txt", true))) {
            String line = userEmail + "," + bookTitle + "," + borrowDate + "," + returnDate + "," + lateFee + ",[PAID]";
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logUserHistory(String userEmail, String bookTitle, LocalDate date) {
        try {
            File dir = new File("user_history");
            if (!dir.exists())
                dir.mkdir();

            File file = new File(dir, userEmail + "_history.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                String entry = String.format("%s | Paid Late Fee | %s", date, bookTitle);
                writer.write(entry);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
