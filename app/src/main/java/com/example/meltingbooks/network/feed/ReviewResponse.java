package com.example.meltingbooks.network.feed;

import java.util.List;

public class ReviewResponse {
    private int reviewId;
    private String content;
    private List<String> reviewImageUrls;
    private int rating;
    private int userId;
    private int bookId;
    private String createdAt;
    private String updatedAt;

    // Getter & Setter
    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getReviewImageUrls() {
        return reviewImageUrls;
    }

    public void setReviewImageUrls(List<String> reviewImageUrls) {
        this.reviewImageUrls = reviewImageUrls;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
