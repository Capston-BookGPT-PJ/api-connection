package com.example.meltingbooks.network.feed;

public class UserReviewResponse {
    private int reviewId;
    private String content;
    private String imageUrl;
    private int rating;
    private int userId;
    private int bookId;

    //getter
    public int getReviewId() { return reviewId; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public int getRating() { return rating; }
    public int getUserId() { return userId; }
    public int getBookId() { return bookId; }
}