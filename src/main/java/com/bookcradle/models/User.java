package com.bookcradle.models;

import java.util.List;

public class User {
    private String name;
    private String surname;
    private String email;
    private List<Book> borrowedBooks;

    public User(String name, String surname, String email, List<Book> borrowedBooks) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.borrowedBooks = borrowedBooks;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    // **Add this setter to fix your error**
    public void setBorrowedBooks(List<Book> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }
}
