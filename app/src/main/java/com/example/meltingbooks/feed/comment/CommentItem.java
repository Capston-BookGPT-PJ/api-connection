package com.example.meltingbooks.feed.comment;

public class CommentItem {
    private int userId;
    private String userName;
    private String content;
    private int profileImageResId;  // 예시로 이미지 리소스 ID 사용
    private String commentDate;//작성 날짜
    private String profileImageUrl;


    // 기존 그룹 피드에서 쓰는 생성자 (유지)
    public CommentItem(String userName, String content, int profileImageResId) {
        this.userName = userName;
        this.content = content;
        this.profileImageResId = profileImageResId;
    }

    // 서버 댓글용 생성자
    public CommentItem(String userName, String content, String profileImageUrl, String commentDate) {
        this.userName = userName;
        this.content = content;
        this.profileImageUrl = profileImageUrl;// 서버에서 URL로 가져오면 여기 바꿔도 됨
        this.commentDate = commentDate;
    }

    public int getUserId() { return userId; }
    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getCommentDate() { return commentDate; }
    public int getProfileImageResId() {
        return profileImageResId;
    }

    public String getProfileImageUrl() { return profileImageUrl; }
}
