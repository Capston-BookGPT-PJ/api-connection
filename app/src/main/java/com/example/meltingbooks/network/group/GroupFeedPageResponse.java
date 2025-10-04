package com.example.meltingbooks.network.group;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupFeedPageResponse implements Serializable {

    private List<GroupFeedResponse.Post> notices;
    private List<GroupFeedResponse.Post> recommendedBooks;
    private List<GroupFeedResponse.Post> goals;
    private PostsWrapper posts;

    public List<GroupFeedResponse.Post> getNotices() { return notices; }
    public List<GroupFeedResponse.Post> getRecommendedBooks() { return recommendedBooks; }
    public List<GroupFeedResponse.Post> getGoals() { return goals; }
    public PostsWrapper getPosts() { return posts; }

    public static class PostsWrapper implements Serializable {
        private List<GroupFeedResponse.Post> content;
        private int page;
        private int size;
        private int totalPages;

        private int totalElements;
        private boolean last;

        public List<GroupFeedResponse.Post> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public int getTotalPages() { return totalPages; }
        public int getTotalElements() { return totalElements; }
        public boolean isLast() { return last; }
    }
}
