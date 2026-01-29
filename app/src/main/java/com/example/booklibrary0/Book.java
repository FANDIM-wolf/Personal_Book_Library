package com.example.booklibrary0;

public class Book {

    private int id;
    private String title;
    private String author;
    private String year;
    private int amount;

    public Book(int id, String title, String author, String year, int amount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.amount = amount;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getYear() { return year; }
    public int getAmount() { return amount; }
}
