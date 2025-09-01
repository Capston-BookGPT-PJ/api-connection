package com.example.meltingbooks.feed.comment;

public class CommentItem {
    private String userName;
    private String content;
    private int profileImageResId;  // 예시로 이미지 리소스 ID 사용

    public CommentItem(String userName, String content, int profileImageResId) {
        this.userName = userName;
        this.content = content;
        this.profileImageResId = profileImageResId;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }
}
