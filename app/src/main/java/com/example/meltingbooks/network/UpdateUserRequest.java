package com.example.meltingbooks.network;

public class UpdateUserRequest {
    private String email;
    private String nickname;
    private String username;
    private String bio;
    private String tagId;
    private String profileImage; // 일단은 null 또는 기존 값

    public UpdateUserRequest(String email,String nickname, String username, String bio, String tagId, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.username = username;
        this.bio = bio;
        this.tagId = tagId;
        this.profileImage = profileImage;
    }

    // Getter & Setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
