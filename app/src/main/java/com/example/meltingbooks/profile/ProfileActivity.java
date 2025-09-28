package com.example.meltingbooks.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
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
import com.example.meltingbooks.network.profile.FollowApi;
import com.example.meltingbooks.network.profile.FollowUser;
import com.example.meltingbooks.network.profile.UserResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.activity.result.contract.ActivityResultContracts;

public class ProfileActivity extends BaseActivity {

    private TextView profileName, profileLevel, profileBio;
    private TextView followerCount, followingCount, reviewCount;
    private TextView profileTagId;
    private ImageView profileImage;

    // 🔥 추가: 피드 관련
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList = new ArrayList<>();

    //피드 갱신용
    private ActivityResultLauncher<Intent> feedDetailLauncher;

    private TextView actionButton; // goTo_Profile_setting
    private boolean isFollowing = false; // 현재 팔로우 상태
    private int viewedUserId; // 지금 보고 있는 프로필 주인
    private int myUserId; // 내 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ActivityResultLauncher 초기화
        feedDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // FeedDetailActivity에서 삭제/수정 후 돌아올 때 처리
                        loadUserProfile(); // 프로필 화면의 피드 갱신
                    }
                }
        );

        setupBottomNavigation();

        // SharedPreferences에서 내 userId
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        myUserId = prefs.getInt("userId", -1);

        // Intent에서 다른 사람 userId 받기
        viewedUserId = getIntent().getIntExtra("userId", myUserId);

        actionButton = findViewById(R.id.goTo_Profile_setting);

        if (viewedUserId == myUserId) {
            // 내 프로필일 때 → 프로필 수정 이동
            actionButton.setText("Edit Profile");
            actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingProfile.class);
                startActivity(intent);
            });
        } else {
            // 다른 사람 프로필 일 때 → 팔로우 버튼
            checkFollowStatus();
            actionButton.setOnClickListener(v -> toggleFollow());
        }

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

        //피드 갱신용(수정)
        feedAdapter = new FeedAdapter(this, feedList, feedDetailLauncher);
        feedRecyclerView.setAdapter(feedAdapter);

        // 프로필 불러오기
        loadUserProfile();

        // 팔로워 클릭 → FollowListActivity (followers)
        followerCount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
            Log.d("ProfileActivity", "팔로워 클릭, viewedUserId: " + viewedUserId);
            intent.putExtra("userId", viewedUserId);  // viewedUserId 넘기기
            intent.putExtra("type", "followers");
            startActivity(intent);
        });

        // 팔로잉 클릭 → FollowListActivity (following)
        followingCount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
            Log.d("ProfileActivity", "팔로잉 클릭, viewedUserId: " + viewedUserId);
            intent.putExtra("userId", viewedUserId);  // viewedUserId 넘기기
            intent.putExtra("type", "following");
            startActivity(intent);
        });
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
        Call<ApiResponse<UserResponse>> call = apiService.getUserProfile("Bearer " + token, viewedUserId);

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

                    // recentBooks 세팅
                    List<BookListHelper.BookItem> books = new ArrayList<>();
                    if (user.getRecentBooks() != null) {
                        for (UserResponse.Book book : user.getRecentBooks()) {
                            books.add(new BookListHelper.BookItem(book.getCover(), false));
                        }
                    }

                    // 레이아웃에 실제로 반영
                    ViewGroup bookListContainer = findViewById(R.id.book_list_container);
                    BookListHelper.setupBooks(ProfileActivity.this, bookListContainer, books, false);

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

    private void checkFollowStatus() {
        String token = getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", null);
        FollowApi followApi = ApiClient.getClient(token).create(FollowApi.class);

        // 내 팔로잉 목록 조회해서 상대방 있는지 확인
        followApi.getFollowing("Bearer " + token, myUserId).enqueue(new Callback<ApiResponse<List<FollowUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FollowUser>>> call, Response<ApiResponse<List<FollowUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FollowUser> following = response.body().getData();
                    isFollowing = false;
                    for (FollowUser f : following) {
                        if (f.getId() == viewedUserId) {
                            isFollowing = true;
                            break;
                        }
                    }
                    updateFollowButton();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FollowUser>>> call, Throwable t) {
                Log.e("Profile", "팔로잉 조회 실패: " + t.getMessage());
            }
        });
    }

    private void toggleFollow() {
        String token = getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", null);
        FollowApi followApi = ApiClient.getClient(token).create(FollowApi.class);

        if (isFollowing) {
            // 언팔로우
            followApi.unfollowUser("Bearer " + token, myUserId, viewedUserId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.code() == 204) {
                                isFollowing = false;
                                runOnUiThread(() -> updateFollowButton());
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("Profile", "언팔로우 실패: " + t.getMessage());
                        }
                    });
        } else {
            // 팔로우
            followApi.followUser("Bearer " + token, myUserId, viewedUserId)
                    .enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {
                                isFollowing = true;
                                runOnUiThread(() -> updateFollowButton());
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Log.e("Profile", "팔로우 실패: " + t.getMessage());
                        }
                    });
        }
    }


    private void updateFollowButton() {
        if (isFollowing) {
            actionButton.setText("Unfollow");
            actionButton.setBackgroundResource(R.drawable.bg_unfollow_button);
            actionButton.setTextColor(getColor(android.R.color.white));
        } else {
            actionButton.setText("Follow");
            actionButton.setBackgroundResource(R.drawable.bg_follow_button);
            actionButton.setTextColor(getColor(android.R.color.white));
        }
    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Profile;
    }
}
