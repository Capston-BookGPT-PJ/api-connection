// 유저 정보 네트워크 응답 전용 DTO
package com.example.meltingbooks.network;

import java.util.List;

public class UserResponse {
    private int id;
    private String email;
    private String nickname;
    private String username;
    private String profileImage;
    private String bio;
    private String tagId;
    private int level;
    private int experience;
    private List<Badge> badges;
    private int followerCount;
    private int followingCount;
    private int reviewCount;
    private List<Book> recentBooks;
    private List<Review> recentReviews;

    // ----------------- Nested Models -----------------
    public static class Badge {
        private int id;
        private String badgeName;
        private String tier;
        private String createdAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getBadgeName() { return badgeName; }
        public void setBadgeName(String badgeName) { this.badgeName = badgeName; }

        public String getTier() { return tier; }
        public void setTier(String tier) { this.tier = tier; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class Book {
        private int id;
        private String title;
        private String coverUrl;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    }

    public static class Review {
        private int id;
        private String content;
        private String createdAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    // ----------------- Getter & Setter -----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public List<Badge> getBadges() { return badges; }
    public void setBadges(List<Badge> badges) { this.badges = badges; }

    public int getFollowerCount() { return followerCount; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public List<Book> getRecentBooks() { return recentBooks; }
    public void setRecentBooks(List<Book> recentBooks) { this.recentBooks = recentBooks; }

    public List<Review> getRecentReviews() { return recentReviews; }
    public void setRecentReviews(List<Review> recentReviews) { this.recentReviews = recentReviews; }
}
