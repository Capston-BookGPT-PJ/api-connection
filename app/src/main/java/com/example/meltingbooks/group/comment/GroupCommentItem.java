package com.example.meltingbooks.group.comment;

public class GroupCommentItem {
    private String userName;
    private String content;
    private int profileImageResId;  // 이미지 리소스 ID

    public GroupCommentItem(String userName, String content, int profileImageResId) {
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
