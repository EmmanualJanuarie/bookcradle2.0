package com.bookcradle.models;

import java.time.LocalDate;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private LocalDate dueDate;
    private double lateFee;
    private boolean lateFeePaid;
    private boolean isReturned;
    private LocalDate returnedDate;

    public Book(String title, String author, String isbn, LocalDate dueDate, double lateFee) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.dueDate = dueDate;
        this.lateFee = lateFee;
        this.lateFeePaid = false;
        this.isReturned = false;
        this.returnedDate = null;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public double getLateFee() {
        return lateFee;
    }

    public boolean isLateFeePaid() {
        return lateFeePaid;
    }

    public boolean isReturned() {
        return isReturned;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    // Setters
    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public void setLateFeePaid(boolean paid) {
        this.lateFeePaid = paid;
    }

    public void setReturned(boolean returned) {
        this.isReturned = returned;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }
}
