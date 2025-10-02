package com.example.meltingbooks.network.group;

import java.io.Serializable;

public class PostSingleResponse implements Serializable {
    private int id;
    private int groupId;
    private int userId;
    private String postType;
    private String title;
    private String content;
    private String imageUrl;
    private String createdAt;

    // getter
    public int getId() { return id; }
    public int getGroupId() { return groupId; }
    public int getUserId() { return userId; }
    public String getPostType() { return postType; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getCreatedAt() { return createdAt; }
}

