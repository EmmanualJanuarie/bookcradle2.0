package com.bookcradle.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserLogsController {

    @FXML
    private TableView<UserLog> userLogsTable;

    @FXML
    private TableColumn<UserLog, String> userColumn;
    @FXML
    private TableColumn<UserLog, String> bookColumn;
    @FXML
    private TableColumn<UserLog, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<UserLog, LocalDate> returnDateColumn;
    @FXML
    private TableColumn<UserLog, Double> lateFeeColumn;
    @FXML
    private TableColumn<UserLog, String> statusColumn;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        bookColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        lateFeeColumn.setCellValueFactory(new PropertyValueFactory<>("lateFee"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        userLogsTable.setItems(loadLogsFromFile("LibraryData.txt"));
    }

    private ObservableList<UserLog> loadLogsFromFile(String filePath) {
        ObservableList<UserLog> logs = FXCollections.observableArrayList();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3)
                    continue;

                String user = parts[0].trim();
                String book = parts[1].trim();
                LocalDate borrowDate = LocalDate.parse(parts[2].trim(), DATE_FORMAT);
                LocalDate returnDate = null;
                if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                    returnDate = LocalDate.parse(parts[3].trim(), DATE_FORMAT);
                }

                double lateFee = 0;
                String status;
                if (returnDate != null) {
                    if (lateFee > 0) {
                        status = "Returned Late";
                    } else {
                        status = "Returned On Time";
                    }
                    lateFee = parts.length > 4 ? Double.parseDouble(parts[4].trim()) : 0;
                } else {
                    if (LocalDate.now().isAfter(borrowDate.plusDays(14))) {
                        long daysLate = LocalDate.now().toEpochDay() - borrowDate.plusDays(14).toEpochDay();
                        lateFee = daysLate * 2.0;
                        status = "Overdue";
                    } else {
                        status = "Borrowed";
                    }
                }

                logs.add(new UserLog(user, book, borrowDate, returnDate, lateFee, status));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logs;
    }

    public static class UserLog {
        private final String userName;
        private final String bookTitle;
        private final LocalDate borrowDate;
        private final LocalDate returnDate;
        private final Double lateFee;
        private final String status;

        public UserLog(String userName, String bookTitle, LocalDate borrowDate, LocalDate returnDate, Double lateFee,
                String status) {
            this.userName = userName;
            this.bookTitle = bookTitle;
            this.borrowDate = borrowDate;
            this.returnDate = returnDate;
            this.lateFee = lateFee;
            this.status = status;
        }

        public String getUserName() {
            return userName;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public LocalDate getBorrowDate() {
            return borrowDate;
        }

        public LocalDate getReturnDate() {
            return returnDate;
        }

        public Double getLateFee() {
            return lateFee;
        }

        public String getStatus() {
            return status;
        }
    }
}
