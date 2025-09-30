package com.example.meltingbooks.group;

import java.io.Serializable;

public class GroupFeedItem implements Serializable {
    private int postId;            // 게시글 ID
    private String type;           // 게시글 타입 (GOAL_SHARE, POST 등)
    private String title;          // 제목 (목표나 공지에 있을 수 있음)
    private String content;        // 본문 내용
    private String imageUrl;       // 게시글 이미지
    private int authorId;          // 작성자 ID
    private String authorName;     // 작성자 이름
    private String createdAt;      // 작성일
    private int commentCount;      // 댓글 수
    private int likeCount;         // 좋아요 수
    private String profileImageUrl; // 유저 프로필 이미지 URL
    private boolean liked;         // 좋아요 여부
    private boolean isGoal;        // 목표 공유 여부 (type 기반으로 편리하게 처리)
    private boolean isPost;        // 일반 게시글 여부
    private boolean isNotice;      // 공지 여부
    private boolean isRecommended; // 추천 도서 여부

    public GroupFeedItem(int postId, String type, String title, String content,
                         String imageUrl, int authorId, String authorName, String createdAt,
                         int commentCount, int likeCount, String profileImageUrl) {
        this.postId = postId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.profileImageUrl = profileImageUrl;

        // 포스트 타입에 따라 boolean 필드 설정
        this.isGoal = "GOAL_SHARE".equals(type);
        this.isPost = "POST".equals(type);
        this.isNotice = "NOTICE".equals(type);
        this.isRecommended = "RECOMMENDED".equals(type);
    }

    // --- Getter ---
    public int getPostId() { return postId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public int getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getCreatedAt() { return createdAt; }
    public int getCommentCount() { return commentCount; }
    public int getLikeCount() { return likeCount; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public boolean isLiked() { return liked; }

    // --- Setter ---
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setLiked(boolean liked) { this.liked = liked; }

    // --- 포스트 타입 편의 Getter ---
    public boolean isGoal() { return isGoal; }
    public boolean isPost() { return isPost; }
    public boolean isNotice() { return isNotice; }
    public boolean isRecommended() { return isRecommended; }
}
