package com.example.meltingbooks;

public class Book {
    private String title;
    private String imageUrl;
    private String author;
    private String publisher;

    public Book(String title, String imageUrl, String author, String publisher) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.author = author;
        this.publisher = publisher;
    }

    // 2개 필드 생성자 (author 없을 때)
    public Book(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.author = ""; // 기본값
    }


    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
}

