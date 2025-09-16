package com.example.meltingbooks.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.feed.FeedAdapter;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.profile.UserResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private TextView profileName, profileLevel, profileBio;
    private TextView followerCount, followingCount, reviewCount;
    private TextView profileTagId;
    private ImageView profileImage;

    // 🔥 추가: 피드 관련
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupBottomNavigation();

        // 프로필 수정 화면 이동
        TextView goToProfileSet = findViewById(R.id.goTo_Profile_setting);
        goToProfileSet.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingProfile.class);
            startActivity(intent);
        });

        // 앱 설정 화면 이동
        ImageButton goToAppSet = findViewById(R.id.goTo_App_setting);
        goToAppSet.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingApp.class);
            startActivity(intent);
        });

        // UI 연결
        profileName = findViewById(R.id.profile_name);
        profileLevel = findViewById(R.id.profile_level);
        profileTagId = findViewById(R.id.profile_tagid);
        profileBio = findViewById(R.id.profile_bio);
        profileImage = findViewById(R.id.profile_image);

        followerCount = findViewById(R.id.follower_count);
        followingCount = findViewById(R.id.following_count);
        reviewCount = findViewById(R.id.review_count);

        // 🔥 리사이클러뷰 설정
        feedRecyclerView = findViewById(R.id.feedRecyclerView);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, feedList);
        feedRecyclerView.setAdapter(feedAdapter);

        // 프로필 불러오기
        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(); // 다시 진입할 때마다 최신 데이터
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("Profile", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        Call<ApiResponse<UserResponse>> call = apiService.getUserProfile("Bearer " + token, userId);

        call.enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call,
                                   Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserResponse user = response.body().getData();

                    // UI 업데이트
                    profileName.setText(user.getNickname());
                    profileLevel.setText("Lv." + user.getLevel());
                    profileBio.setText(user.getBio() != null ? user.getBio() : "소개가 없습니다.");
                    profileTagId.setText(user.getTagId() != null ? "@" + user.getTagId() : "");
                    followerCount.setText(String.valueOf(user.getFollowerCount()));
                    followingCount.setText(String.valueOf(user.getFollowingCount()));
                    reviewCount.setText(String.valueOf(user.getReviewCount()));

                    Glide.with(ProfileActivity.this)
                            .load(user.getProfileImageUrl())
                            .placeholder(R.drawable.sample_profile)
                            .circleCrop()
                            .into(profileImage);

                    // recentBooks 세팅 나중에 되나 확인해보기
                    List<BookListHelper.BookItem> books = new ArrayList<>();
                    if (user.getRecentBooks() != null) {
                        for (UserResponse.Book book : user.getRecentBooks()) {
                            books.add(new BookListHelper.BookItem(book.getCover(), false));
                        }
                    }

                    //리뷰 불러오기

                    // 최신순 리뷰 리스트
                    List<UserResponse.Review> reviews = user.getRecentReviews();
                    feedList.clear();

                    for (UserResponse.Review review : reviews) {
                        feedList.add(new FeedItem(
                                user.getNickname(),
                                review.getContent(),
                                review.getFormattedCreatedAt(),
                                review.getImageUrl(),
                                user.getProfileImageUrl(),
                                review.getBookId() //책 Id

                        ));
                    }
                    feedAdapter.notifyDataSetChanged();

                } else {
                    Log.e("Profile", "프로필 응답이 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Log.e("Profile", "API 호출 실패: " + t.getMessage());
            }
        });

    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Profile;
    }
}
