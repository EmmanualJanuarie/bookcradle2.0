package com.bookcradle.services;

import com.bookcradle.models.Book;
import com.bookcradle.models.User;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookService {

    private static final String BOOKS_FILE = "books.txt";
    private static final String USERS_FILE = "users.txt";
    private static final String LOG_FILE = "UserBooks_data.txt";

    // Add a new book to the library
    public void addBook(Book book) {
        List<Book> books = loadBooks();
        books.add(book);
        saveBooks(books);
    }

    // Get all books
    public List<Book> getAllBooks() {
        return loadBooks();
    }

    // Load all books from the file
    private List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Expect format: title,author,isbn,genre,year,dueDate,lateFee
                String[] bookData = line.split(",");
                if (bookData.length == 7) {
                    String title = bookData[0].trim();
                    String author = bookData[1].trim();
                    String isbn = bookData[2].trim();
                    String genre = bookData[3].trim();
                    int year = Integer.parseInt(bookData[4].trim());
                    LocalDate dueDate = LocalDate.parse(bookData[5].trim());
                    double lateFee = Double.parseDouble(bookData[6].trim());
                    books.add(new Book(title, author, isbn, genre, year, dueDate, lateFee));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Save books to file with genre & year
    private void saveBooks(List<Book> books) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books) {
                writer.write(book.getTitle() + "," +
                        book.getAuthor() + "," +
                        book.getIsbn() + "," +
                        book.getGenre() + "," +
                        book.getYear() + "," +
                        book.getDueDate() + "," +
                        book.getLateFee());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Remove a book from the list and save
    public void removeBook(Book bookToRemove) {
        List<Book> books = loadBooks();
        books.removeIf(b -> b.getTitle().equals(bookToRemove.getTitle()) &&
                b.getAuthor().equals(bookToRemove.getAuthor()) &&
                b.getIsbn().equals(bookToRemove.getIsbn()));
        saveBooks(books);
    }

    // Update a book: replace oldBook with newBook in the list and save
    public void updateBook(Book oldBook, Book newBook) {
        List<Book> books = loadBooks();
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            if (b.getTitle().equals(oldBook.getTitle()) &&
                    b.getAuthor().equals(oldBook.getAuthor()) &&
                    b.getIsbn().equals(oldBook.getIsbn())) {
                books.set(i, newBook);
                break;
            }
        }
        saveBooks(books);
    }

    // Search books with genre and year filters added
    public List<Book> searchBooks(String isbn, String title, String author, String genre, Integer year) {
        List<Book> books = loadBooks();
        return books.stream()
                .filter(book -> (isbn == null || isbn.isBlank()
                        || book.getIsbn().toLowerCase().contains(isbn.toLowerCase())) &&
                        (title == null || title.isBlank()
                                || book.getTitle().toLowerCase().contains(title.toLowerCase()))
                        &&
                        (author == null || author.isBlank()
                                || book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                        &&
                        (genre == null || genre.isBlank()
                                || book.getGenre().toLowerCase().contains(genre.toLowerCase()))
                        &&
                        (year == null || book.getYear() == year))
                .collect(Collectors.toList());
    }

    // Overloaded for single text search on isbn,title,author,genre
    public List<Book> searchBooks(String searchText) {
        return searchBooks(searchText, searchText, searchText, searchText, null);
    }

    // Overloaded without year, for convenience (you can use null)
    public List<Book> searchBooks(String isbn, String title, String author, String genre) {
        return searchBooks(isbn, title, author, genre, null);
    }

    // Borrow a book
    public void borrowBook(User user, Book book) {
        user.getBorrowedBooks().add(book);
        saveUser(user);
        logUserBook(user.getEmail(), book.getTitle(), LocalDate.now(), null, 0.0, false);
    }

    // Return a book
    public void returnBook(User user, Book book, LocalDate returnDate) {
        user.getBorrowedBooks().remove(book);
        double lateFee = 0.0;

        if (returnDate.isAfter(book.getDueDate())) {
            long daysLate = returnDate.toEpochDay() - book.getDueDate().toEpochDay();
            lateFee = daysLate * 1.0;
        }

        book.setLateFee(lateFee);
        saveUser(user);
        saveBooks(loadBooks());

        logUserBook(user.getEmail(), book.getTitle(), book.getDueDate().minusDays(14), returnDate, lateFee, false);
    }

    // Save user
    private void saveUser(User user) {
        List<User> users = loadUsers();
        boolean userExists = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equals(user.getEmail())) {
                users.set(i, user);
                userExists = true;
                break;
            }
        }

        if (!userExists) {
            users.add(user);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                writer.write(u.getEmail() + "," +
                        u.getName() + "," +
                        u.getSurname() + "," +
                        u.getBorrowedBooks().size() + "," +
                        u.getBorrowedBooks().stream()
                                .map(Book::getTitle)
                                .collect(Collectors.joining(";")));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load users
    private List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(",");
                if (userData.length >= 5) {
                    String email = userData[0].trim();
                    String name = userData[1].trim();
                    String surname = userData[2].trim();
                    int borrowedCount = Integer.parseInt(userData[3].trim());

                    List<Book> borrowedBooks = new ArrayList<>();
                    if (borrowedCount > 0) {
                        String[] books = userData[4].split(";");
                        List<Book> allBooks = loadBooks();
                        for (String bookTitle : books) {
                            allBooks.stream()
                                    .filter(book -> book.getTitle().equals(bookTitle))
                                    .findFirst()
                                    .ifPresent(borrowedBooks::add);
                        }
                    }
                    users.add(new User(name, surname, email, borrowedBooks));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User searchUserByEmail(String email) {
        return loadUsers().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    // Log action
    private void logUserBook(String email, String bookTitle, LocalDate borrowDate,
            LocalDate returnDate, double lateFee, boolean feePaid) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(email + "," +
                    bookTitle + "," +
                    borrowDate + "," +
                    (returnDate != null ? returnDate : "") + "," +
                    lateFee + "," +
                    feePaid);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ NEW: Mark late fee as paid
    public void markLateFeeAsPaid(String userEmail, String bookTitle, LocalDate borrowDate) {
        List<String> updatedLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String email = parts[0].trim();
                    String book = parts[1].trim();
                    LocalDate borrow = LocalDate.parse(parts[2].trim());

                    if (email.equals(userEmail) && book.equals(bookTitle) && borrow.equals(borrowDate)) {
                        parts[5] = "true"; // Set paid = true
                        line = String.join(",", parts);
                    }
                }
                updatedLines.add(line);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE))) {
                for (String l : updatedLines) {
                    writer.write(l);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
