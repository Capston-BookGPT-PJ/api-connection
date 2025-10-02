package com.example.meltingbooks.network.group;

import java.io.Serializable;
import java.util.List;

public class GroupFeedResponse implements Serializable {
    private boolean success;
    private GroupFeedData data;
    private String error;

    public boolean isSuccess() { return success; }
    public GroupFeedData getData() { return data; }
    public String getError() { return error; }

    public static class GroupFeedData implements Serializable {
        private List<Post> notices;
        private List<Post> recommendedBooks;
        private List<Post> goals;
        private PostsWrapper posts;

        public List<Post> getNotices() { return notices; }
        public List<Post> getRecommendedBooks() { return recommendedBooks; }
        public List<Post> getGoals() { return goals; }
        public PostsWrapper getPosts() { return posts; }
    }

    public static class PostsWrapper implements Serializable {
        private List<Post> content;

        public List<Post> getContent() { return content; }
    }

    public static class Post implements Serializable {
        private int postId;
        private String type;
        private String title;
        private String content;
        private String imageUrl;
        private int authorId;
        private String authorName;
        private String createdAt;
        private int commentCount;
        private int likeCount;
        private String userProfileImage;

        // --- 댓글 관련 필드 추가 ---
        private int commentId;
        private String commentContent;
        private String commentCreatedAt;

        private boolean liked;

        // --- Getter ---
        public int getPostId() {
            return postId;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public int getAuthorId() {
            return authorId;
        }

        public String getAuthorName() {
            return authorName;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public boolean isLiked() {
            return liked;
        }

        public int getCommentId() {
            return commentId;
        }

        public String getCommentContent() {
            return commentContent;
        }

        public String getCommentCreatedAt() {
            return commentCreatedAt;
        }

        public String getUserProfileImage() {
            return userProfileImage;
        }

        // --- Setter ---
        public void setPostId(int postId) {
            this.postId = postId;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setAuthorId(int authorId) {
            this.authorId = authorId;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public void setLiked(boolean liked) {
            this.liked = liked;
        }

        public void setCommentId(int commentId) {
            this.commentId = commentId;
        }

        public void setCommentContent(String commentContent) {
            this.commentContent = commentContent;
        }

        public void setCommentCreatedAt(String commentCreatedAt) {
            this.commentCreatedAt = commentCreatedAt;
        }

        public void setUserProfileImage(String userProfileImage) {
            this.userProfileImage = userProfileImage;
        }
    }
}
