package com.example.meltingbooks;

public class User {
    private String name;
    private String intro;
    private int imageResId;

    public User(String name, String intro, int imageResId) {
        this.name = name;
        this.intro = intro;
        this.imageResId = imageResId;

    }

    public String getName() {
        return name;
    }

    public String getIntro() {
        return intro;
    }
    public int getImageResId() {
        return imageResId;
    }

    //------프로필 부분에서 사용 정의----------------------
    private int id;
    private String email;
    private String nickname;
    private String username;
    private String profileImage;
    private String bio;
    private int level;
    private int followerCount;
    private int followingCount;
    private int reviewCount;

    // ✅ Getter & Setter (Retrofit이 데이터 매핑할 때 필요)
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

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getFollowerCount() { return followerCount; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}