package com.example.meltingbooks.group.menu;

public class GroupListItem {
    private String name;
    private int imageResId;       // drawable 리소스 ID
    private String imageUrl;      // 선택사항

    public GroupListItem(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public GroupListItem(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}