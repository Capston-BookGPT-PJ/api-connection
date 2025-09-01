package com.example.meltingbooks.feed;

public class FeedItem {
    private String userName;//유저 이름
    private String reviewContent;//감상문
    private String reviewDate;//작성 날짜
    private String imageUrl;  // 이미지 URL 추가
    //private boolean hasVote;  // 투표 기능 활성화 여부
    //private List<String> pollOptions; 투표 항목 리스트 추가
    //private int selectedOption; // 사용자가 선택한 옵션 (없으면 -1)
    //private String postId;

    //feed와 group 구분 코드
    private String postId;      // 댓글 조회/등록용 ID
    private String postType;    // "feed" 또는 "group"


    public FeedItem(String userName, String reviewContent, String reviewDate, String imageUrl) {
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.reviewDate = reviewDate;
        this.imageUrl = imageUrl;

        //this.postId = postId;
        //this.hasVote = hasVote;
        //this.pollOptions = pollOptions;
        //this.selectedOption = -1; // 초기값 설정
    }

    //getter and setter
    public String getUserName() {
        return userName;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public String getReviewDate() {
        return reviewDate;
    }
    //public String getPostId() { return postId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
    //public boolean hasVote() { return hasVote; }
    //public List<String> getPollOptions() { return pollOptions; }
    //public int getSelectedOption() { return selectedOption; }
    //public void setSelectedOption(int selectedOption) { this.selectedOption = selectedOption; }


    private int commentCount;

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

}
