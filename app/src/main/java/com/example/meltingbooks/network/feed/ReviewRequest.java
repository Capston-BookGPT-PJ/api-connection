package com.example.meltingbooks.network.feed;

// 리뷰 작성 요청 DTO
public class ReviewRequest {
        private int bookId;
        private String content;
        private int rating;

        public ReviewRequest(int bookId, String content, int rating) {
            this.bookId = bookId;
            this.content = content;
            this.rating = rating;
        }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}