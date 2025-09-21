package com.example.meltingbooks.group;

public class GroupFeedItem {
    private String userName;
    private String content;
    private String content2;

    private String writeDate;
    private String imageUrl;  // 게시글에 이미지 있을 수도 있으니

    public GroupFeedItem(String userName, String content, String content2, String writeDate, String imageUrl) {
        this.userName = userName;
        this.content = content;
        this.content2 = content2;
        this.writeDate = writeDate;
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }
    public String getContent2() {
        return content2;
    }


    public String getWriteDate() {
        return writeDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    private int commentCount;

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
