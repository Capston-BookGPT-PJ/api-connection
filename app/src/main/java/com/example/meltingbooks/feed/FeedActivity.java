package com.example.meltingbooks.feed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.FeedPageResponse;
import com.example.meltingbooks.network.feed.FeedResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends BaseActivity {
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList = new ArrayList<>(); //Null 방지 초기화

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setupBottomNavigation();

        // 글 작성 화면으로 이동
        ImageButton goToUpload = findViewById(R.id.goToUpload);
        goToUpload.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, FeedWriteActivity.class);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // 상태바 디자인 설정
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }

            //리사이클러뷰 설정
            feedRecyclerView = findViewById(R.id.feedRecyclerView);
            feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            feedAdapter = new FeedAdapter(this, feedList);
            feedRecyclerView.setAdapter(feedAdapter);

            //서버에서 피드 불러오기
            loadFeeds();
        }
    }

    //서버에서 피드 목록 불러옴
    /**private void loadFeeds() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("Feed", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        Call<ApiResponse<List<FeedResponse>>> call =
                apiService.getUserFeeds("Bearer " + token, userId, 0, 10);

        call.enqueue(new Callback<ApiResponse<List<FeedResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FeedResponse>>> call,
                                   Response<ApiResponse<List<FeedResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<FeedResponse> feeds = response.body().getData();
                    feedList.clear();

                    for (FeedResponse feed : feeds) {
                        String firstImage = (feed.getReviewImageUrls() != null && !feed.getReviewImageUrls().isEmpty())
                                ? feed.getReviewImageUrls().get(0)
                                : null;

                        FeedItem feedItem = new FeedItem(
                                feed.getUsername(),
                                feed.getContent(),
                                feed.getFormattedCreatedAt(),
                                firstImage,
                                feed.getUserProfileImage(),
                                feed.getBookId(),
                                feed.getCommentCount(),
                                feed.getLikeCount()
                        );

                        // ✅ 리뷰ID를 postId로 세팅
                        feedItem.setPostId(Integer.valueOf(feed.getReviewId()));
                        feedItem.setPostType("feed");

                        feedList.add(feedItem);
                    }

                    feedAdapter.notifyDataSetChanged();
                } else {
                    Log.e("Feed", "Feed 응답이 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FeedResponse>>> call, Throwable t) {
                Log.e("Feed", "Feed API 실패: " + t.getMessage());
            }
        });
    }*/

    //서버에서 피드 목록 불러옴
    private void loadFeeds() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("Feed", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        // ✅ FeedPageResponse로 수정
        Call<ApiResponse<FeedPageResponse>> call =
                apiService.getUserFeeds("Bearer " + token, userId, 0, 10);

        call.enqueue(new Callback<ApiResponse<FeedPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FeedPageResponse>> call,
                                   Response<ApiResponse<FeedPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    FeedPageResponse pageResponse = response.body().getData();
                    List<FeedResponse> feeds = pageResponse.getContent();

                    feedList.clear();

                    for (FeedResponse feed : feeds) {
                        String firstImage = (feed.getReviewImageUrls() != null && !feed.getReviewImageUrls().isEmpty())
                                ? feed.getReviewImageUrls().get(0)
                                : null;

                        FeedItem feedItem = new FeedItem(
                                //feed.getUsername(),
                                feed.getNickname(),
                                feed.getContent(),
                                feed.getFormattedCreatedAt(),
                                firstImage,
                                feed.getUserProfileImage(),
                                feed.getBookId(),
                                feed.getCommentCount(),
                                feed.getLikeCount(),
                                feed.getTagId()
                        );

                        // ✅ 리뷰ID를 postId로 세팅
                        feedItem.setPostId(feed.getReviewId());
                        feedItem.setPostType("feed");

                        feedList.add(feedItem);
                    }

                    feedAdapter.notifyDataSetChanged();

                    // ✅ 페이징 정보도 로그 찍기
                    Log.d("Feed", "불러온 리뷰 개수: " + feeds.size());
                    Log.d("Feed", "전체 페이지 수: " + pageResponse.getTotalPages()
                            + ", 마지막 페이지 여부: " + pageResponse.isLast());

                } else {
                    Log.e("Feed", "Feed 응답이 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                Log.e("Feed", "Feed API 실패: " + t.getMessage());
            }
        });
    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Feed;
    }
}
