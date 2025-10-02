package com.example.meltingbooks.network.group;

public class UpdatePostRequest {
    private String postType;   // NOTICE, GOAL, REVIEW, RECOMMENDED 등
    private String title;
    private String content;
    private String imageUrl;   // 이미지가 없으면 null

    // 기본 생성자
    public UpdatePostRequest() {}

    // 전체 생성자
    public UpdatePostRequest(String postType, String title, String content, String imageUrl) {
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // Getter & Setter
    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
