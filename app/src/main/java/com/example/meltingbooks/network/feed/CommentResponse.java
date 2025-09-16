package com.example.meltingbooks.network.feed;// 댓글 조회 응답용

// 조회 응답용
public class CommentResponse {
    private int id;
    private String content;
    private int userId;
    private int reviewId;
    private String createdAt;

    // 기본 생성자
    public CommentResponse() {}

    // 전체 필드 생성자
    public CommentResponse(int id, String content, int userId, int reviewId, String createdAt) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.reviewId = reviewId;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        try {
            java.time.LocalDateTime dateTime =
                    java.time.LocalDateTime.parse(createdAt, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return createdAt;
        }
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
