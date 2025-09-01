/**package com.example.meltingbooks.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedAdapter;
import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.calendar.utils.BookListHelper;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {
    //피드 리사이클
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //하단 메뉴
        setupBottomNavigation();

        //프로필 수정 화면으로 이동
        TextView goToProfileSet = findViewById(R.id.goTo_Profile_setting);

        goToProfileSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingProfile.class);
                startActivity(intent);
            }
        });

        //앱 설정 화면으로 이동
        ImageButton goToAppSet = findViewById(R.id.goTo_App_setting);

        goToAppSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingApp.class);
                startActivity(intent);
            }
        });

        //책 이미지 처리 리턴
        List<BookListHelper.BookItem> books = new ArrayList<>();

        books.add(new BookListHelper.BookItem(R.drawable.book_image_1, false));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_2, false));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_3, false));

        LinearLayout bookContainer = findViewById(R.id.book_list_container);
        BookListHelper.setupBooks(this, bookContainer, books, false);

        //피드 리사이클 뷰---------------------------------------------------
        feedRecyclerView = findViewById(R.id.feedRecyclerView);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 더미 데이터 추가
        feedList = new ArrayList<>();

        // 나중에 서버 통신으로 바꿀시 Null 처리 어떻게 할 것인지 생각해보기.
        // 이미지 게시글
        feedList.add(new FeedItem("Alice", "이 고양이좀 봐~! 책읽고 있어", "2025-03-10", "https://i.imgur.com/iWf9Yuh.jpeg"));

        //추후 서버 연결해서 바꾸는 식으로 조정.
        feedList.add(new FeedItem("Alice", "오늘 읽은 책 너무 재밌었어!------------------------------------------------------------------------------------------------------------------------------------------------------------------------", "2025-03-10", null));
        feedList.add(new FeedItem("Alice", "독서가 정말 힘이 되네.", "2025-03-09",null));

        feedAdapter = new FeedAdapter(this, feedList);
        feedRecyclerView.setAdapter(feedAdapter);
    }

    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Profile;
    }
}*/

package com.example.meltingbooks.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.User;
import com.example.meltingbooks.network.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private TextView profileName, profileLevel, profileBio;
    private TextView followerCount, followingCount, reviewCount;
    private TextView profileTagId;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupBottomNavigation();

        //프로필 수정 화면으로 이동
        TextView goToProfileSet = findViewById(R.id.goTo_Profile_setting);

        goToProfileSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingProfile.class);
                startActivity(intent);
            }
        });

        //앱 설정 화면으로 이동
        ImageButton goToAppSet = findViewById(R.id.goTo_App_setting);

        goToAppSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingApp.class);
                startActivity(intent);
            }
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

        loadUserProfile();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();  // ✅ 다시 진입할 때마다 최신 데이터로 갱신
    }

    private void loadUserProfile() {
        // ✅ SharedPreferences에서 token, userId 가져오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("Profile", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        Call<UserResponse> call = apiService.getUserProfile("Bearer " + token, userId);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();

                    /**Log.d("Profile", "서버 응답: " +
                            "nickname=" + user.getNickname() +
                            ", bio=" + user.getBio() +
                            ", image=" + user.getProfileImage());*/

                    // UI 업데이트
                    profileName.setText(user.getNickname());
                    profileLevel.setText("Lv." + user.getLevel());
                    profileBio.setText(user.getBio() != null ? user.getBio() : "소개가 없습니다.");
                    profileTagId.setText("@" + user.getTagId());
                    followerCount.setText(String.valueOf(user.getFollowerCount()));
                    followingCount.setText(String.valueOf(user.getFollowingCount()));
                    reviewCount.setText(String.valueOf(user.getReviewCount()));

                    // Glide로 프로필 이미지 로드
                    Glide.with(ProfileActivity.this)
                            .load(user.getProfileImage())
                            .placeholder(R.drawable.sample_profile)
                            .circleCrop()
                            .into(profileImage);

                    // ✅ 배지 RecyclerView에 연결/ 추후 추가 예정
                    /**List<UserResponse.Badge> badges = user.getBadges();
                    if (badges != null) {
                        badgeAdapter.submitList(badges);
                    }*/
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("Profile", "API 호출 실패: " + t.getMessage());
            }


        });
    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Profile;
    }
}
