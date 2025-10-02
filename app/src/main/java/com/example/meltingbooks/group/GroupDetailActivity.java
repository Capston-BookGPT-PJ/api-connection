package com.example.meltingbooks.group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedDetailActivity;
import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.feed.comment.CommentAdapter;
import com.example.meltingbooks.feed.comment.CommentItem;
import com.example.meltingbooks.group.comment.GroupCommentAdapter;
import com.example.meltingbooks.group.comment.GroupCommentItem;
import com.example.meltingbooks.group.write.GroupWriteActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.feed.CommentRequest;
import com.example.meltingbooks.network.feed.CommentResponse;
import com.example.meltingbooks.network.feed.FeedResponse;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.GroupFeedResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailActivity extends AppCompatActivity {

    private RecyclerView commentRecyclerView;
    private GroupCommentAdapter commentAdapter;
    private List<GroupCommentItem> commentList;
    private EditText commentEditText;
    private ImageView postCommentButton;

    // 게시글 카드뷰 내부
    private ImageView postUserProfile, groupImage;
    private TextView postUserName;
    private TextView postDate;
    private TextView postTypeContent;
    private TextView postTitle;
    private TextView postContent;

    // 하단 아이콘
    private ImageView chatButton;
    private ImageView likeButton;
    private TextView likeCount, commentCount;

    // 리뷰 관련
    private ImageButton btnEditPost; // 게시글 수정 버튼
    private ImageButton btnDeletePost; // 게시글 삭제 버튼


    private int postId;
    private int groupId;
    private GroupFeedItem currentFeed;
    private int currentUserId; // 현재 로그인된 사용자 ID
    private String token;
    private ApiService apiService;
    private GroupApi groupApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }



        postUserProfile = findViewById(R.id.postUserProfile);
        postUserName = findViewById(R.id.postUserName);
        postDate = findViewById(R.id.postDate);
        postTypeContent = findViewById(R.id.postTypeContent);

        postTitle = findViewById(R.id.postTitle);
        postContent = findViewById(R.id.postContent);

        //댓글
        chatButton = findViewById(R.id.chat_button);
        likeButton = findViewById(R.id.like_button);
        commentCount = findViewById(R.id.comment_count);
        likeCount = findViewById(R.id.like_count);
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);

        // 리뷰 수정 버튼
        btnEditPost = findViewById(R.id.btn_edit_post);
        btnDeletePost = findViewById(R.id.btn_delete_post);


        // GroupFeedAdpater.java에서 GroupFeedItem 받아오기
        currentFeed = (GroupFeedItem) getIntent().getSerializableExtra("groupFeedItem");

        if (currentFeed == null) {
            Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        postId = currentFeed.getPostId();
        groupId = currentFeed.getGroupId();


        // 초기화
        setupRecyclerView();
        initializeApiClients();
        setupListeners();

        // 데이터 바인딩
        bindDataToViews(currentFeed);

        // 댓글 불러오기
        fetchComments();
    }


    //댓글 리사이클러뷰
    private void setupRecyclerView() {
        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new GroupCommentAdapter(this, commentList);
        commentRecyclerView.setAdapter(commentAdapter);
    }

    private void initializeApiClients() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        currentUserId = prefs.getInt("userId", -1);

        if (token == null || currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        apiService = ApiClient.getClient(token).create(ApiService.class);
        groupApi = ApiClient.getClient(token).create(GroupApi.class);

    }

    // --- 이벤트 리스너 설정 ---
    private void setupListeners() {
        // 게시글 수정 버튼 리스너
        btnEditPost.setOnClickListener(v -> {
            if (postId == -1 || currentFeed == null) {
                Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            int groupId = currentFeed.getGroupId();

            Intent intent = new Intent(GroupDetailActivity.this, GroupWriteActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("postId", postId);
            intent.putExtra("isEdit", true);
            // feedEditLauncher는 ActivityResultLauncher<Intent> 로 선언해둬야 함
            feedEditLauncher.launch(intent);
        });

        // 게시글 삭제 버튼 리스너
        btnDeletePost.setOnClickListener(v -> {
            if (postId == -1 || currentFeed == null) {
                Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            int groupId = currentFeed.getGroupId();
            groupApi.deletePost(groupId, postId)
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(GroupDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                                // 삭제 후 이전 화면 갱신
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("deletedPostId", postId);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Toast.makeText(GroupDetailActivity.this, "게시글 삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(GroupDetailActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 좋아요 버튼
        likeButton.setOnClickListener(v -> toggleLike());

        // 댓글 작성 버튼
        postCommentButton.setOnClickListener(v -> postComment());
    }


    private void bindDataToViews(GroupFeedItem feed) {
        // 게시글 작성자와 본문
        postUserName.setText(feed.getAuthorName());
        postTitle.setText(feed.getTitle());
        postContent.setText(feed.getContent());
        postDate.setText(feed.getCreatedAt());

        // 댓글 / 좋아요
        commentCount.setText(String.valueOf(feed.getCommentCount()));
        likeCount.setText(String.valueOf(feed.getLikeCount()));
        likeButton.setImageResource(feed.isLiked() ? R.drawable.feed_like_full : R.drawable.feed_like_button);

        // 프로필 이미지
        if (feed.getProfileImageUrl() != null && !feed.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(feed.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile)
                    .error(R.drawable.sample_profile)
                    .into(postUserProfile);
        } else {
            postUserProfile.setImageResource(R.drawable.sample_profile);
        }

        // 게시글 이미지
        if (feed.getImageUrl() != null && !feed.getImageUrl().isEmpty()) {
            groupImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(feed.getImageUrl())
                    .placeholder(R.drawable.sample_profile)
                    .error(R.drawable.sample_profile)
                    .into(groupImage);
        } else {
            groupImage.setVisibility(View.GONE);
        }

        // 게시글 유형
        postTypeContent.setText(feed.getType());
    }


    private void fetchComments() {
        apiService.getComments("Bearer " + token, postId)
                .enqueue(new Callback<ApiResponse<List<CommentResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<CommentResponse>>> call, Response<ApiResponse<List<CommentResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<CommentResponse> responseList = response.body().getData();
                            commentList.clear();

                            for (CommentResponse c : responseList) {
                                commentList.add(new GroupCommentItem(
                                        c.getNickname(),
                                        c.getContent(),
                                        c.getUserProfileImage(),   // ✅ 서버 값 사용
                                        c.getFormattedCreatedAt()
                                ));
                            }

                            commentAdapter.notifyDataSetChanged();

                            // 댓글 수 업데이트
                            if (currentFeed != null) {
                                currentFeed.setCommentCount(responseList.size());
                                commentCount.setText(String.valueOf(currentFeed.getCommentCount()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CommentResponse>>> call, Throwable t) {
                        Log.e("Comments", "댓글 로드 실패: " + t.getMessage());
                    }
                });
    }

    private void postComment() {
        String commentContent = commentEditText.getText().toString().trim();
        if (commentContent.isEmpty()) {
            Toast.makeText(this, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentRequest request = new CommentRequest(commentContent);

        apiService.postComment("Bearer " + token,  currentUserId, postId, request)
                .enqueue(new Callback<ApiResponse<CommentResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CommentResponse>> call, Response<ApiResponse<CommentResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(GroupDetailActivity.this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            Log.d("postComment", "postId=" + postId + ", currentUserId=" + currentUserId + ", content=" + request.getContent());


                            // 입력창 비우기
                            commentEditText.setText("");

                            // 등록 후 전체 댓글 다시 불러오기
                            fetchComments();

                            // 댓글 수 증가
                            if (currentFeed != null) {
                                currentFeed.setCommentCount(currentFeed.getCommentCount() + 1);
                                commentCount.setText(String.valueOf(currentFeed.getCommentCount()));
                            }

                        } else {
                            Toast.makeText(GroupDetailActivity.this, "댓글 등록 실패", Toast.LENGTH_SHORT).show();
                            Log.e("postComment", "응답 코드: " + response.code());
                            Log.e("postComment", "응답 바디: " + new Gson().toJson(response.body()));

                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                        Toast.makeText(GroupDetailActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void toggleLike() {
        if (currentFeed == null) return;

        boolean newState = !currentFeed.isLiked();
        currentFeed.setLiked(newState);

        // UI 즉시 반영
        likeButton.setImageResource(newState ? R.drawable.feed_like_full : R.drawable.feed_like_button);
        int newCount = currentFeed.getLikeCount() + (newState ? 1 : -1);
        currentFeed.setLikeCount(newCount);
        likeCount.setText(String.valueOf(newCount));

        // 서버 요청
        Call<ApiResponse<Void>> call = newState
                ? apiService.likeReview("Bearer " + token, postId)
                : apiService.unlikeReview("Bearer " + token, postId);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!response.isSuccessful() || (response.body() != null && !response.body().isSuccess())) {
                    rollbackLike(!newState);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                rollbackLike(!newState);
            }
        });
    }

    private void rollbackLike(boolean correctState) {
        if (currentFeed == null) return;

        currentFeed.setLiked(correctState);
        likeButton.setImageResource(correctState ? R.drawable.feed_like_full : R.drawable.feed_like_button);

        // UI 즉시 반영
        int correctedCount = correctState
                ? currentFeed.getLikeCount() + 1
                : currentFeed.getLikeCount() - 1;
        currentFeed.setLikeCount(correctedCount);
        likeCount.setText(String.valueOf(correctedCount));
    }

    //피드 갱신
    private final ActivityResultLauncher<Intent> feedEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    GroupFeedResponse updatedFeed = (GroupFeedResponse) result.getData().getSerializableExtra("updatedFeed");
                    if (updatedFeed != null) {
                        // FeedActivity로 전달
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedFeed", updatedFeed);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            });

}
