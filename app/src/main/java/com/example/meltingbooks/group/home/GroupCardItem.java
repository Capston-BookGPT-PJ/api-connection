package com.example.meltingbooks.group.home;

public class GroupCardItem {
    private String name;
    private String intro;
    private int imageResId;
    private String imageUrl;
    private String category = "";
    private String introTitle = "";
    private String introDetail = "";

    // 생성자 (로컬 이미지 사용)
    public GroupCardItem(String name, String intro, int imageResId) {
        this.name = name;
        this.intro = intro;
        this.imageResId = imageResId;
    }

    // 생성자 (URL 이미지 사용)
    public GroupCardItem(String name, String intro, String imageUrl) {
        this.name = name;
        this.intro = intro;
        this.imageUrl = imageUrl;
    }

    // getter / setter
    public String getName() {
        return name;
    }

    public String getIntro() {
        return intro;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIntroTitle() {
        return introTitle;
    }

    public void setIntroTitle(String introTitle) {
        this.introTitle = introTitle;
    }
    public String getIntroDetail() {
        return introDetail;
    }

    public void setIntroDetail(String introDetail) {
        this.introDetail = introDetail;
    }
}
