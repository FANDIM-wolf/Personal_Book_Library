package com.example.booklibrary0;

public class Loan {
    private int id;
    private int bookId;
    private String bookTitle;
    private int userId;
    private String userName;
    private String dateGiven;

    public Loan(int id, int bookId, String bookTitle, int userId, String userName, String dateGiven) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.userId = userId;
        this.userName = userName;
        this.dateGiven = dateGiven;
    }

    // getters
    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getDateGiven() { return dateGiven; }
}