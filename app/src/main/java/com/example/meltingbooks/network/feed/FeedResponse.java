package com.example.meltingbooks.network.feed;

import java.util.List;

public class FeedResponse {
    private int reviewId;
    private int userId;
    private String tagId;
    private String username;
    private String userProfileImage;
    private String content;
    private int rating;
    private List<String> reviewImageUrls;
    private String createdAt;
    private int likeCount;
    private int commentCount;
    private List<String> hashtags;
    private int bookId;
    private String bookTitle;
    private String nickname;


    // Getter
    public int getReviewId() { return reviewId; }
    public int getUserId() { return userId; }
    public String getTagId() { return tagId; }
    public String getUsername() { return username; }
    public String getUserProfileImage() { return userProfileImage; }
    public String getContent() { return content; }
    public int getRating() { return rating; }
    public List<String> getReviewImageUrls() { return reviewImageUrls; }
    public String getCreatedAt() { return createdAt; }
    public String getFormattedCreatedAt() {
        try {
            java.time.LocalDateTime dateTime =
                    java.time.LocalDateTime.parse(createdAt, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return createdAt;
        }
    }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public List<String> getHashtags() { return hashtags; }
    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }

    public String getNickname() { return nickname; }


}