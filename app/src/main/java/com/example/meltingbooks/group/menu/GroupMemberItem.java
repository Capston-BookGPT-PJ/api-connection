package com.example.meltingbooks.group.menu;

public class GroupMemberItem {
    private String name;
    private int imageResId;       // 리소스 ID (예: R.drawable.profile1)
    private String imageUrl;      // URL 로딩용 (선택사항)

    public GroupMemberItem(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public GroupMemberItem(String name, String imageUrl) {
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
