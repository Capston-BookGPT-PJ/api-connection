package com.example.meltingbooks.group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.group.goal.GroupGoalSetting;
import com.example.meltingbooks.group.menu.GroupListAdapter;
import com.example.meltingbooks.group.menu.GroupListItem;
import com.example.meltingbooks.group.menu.GroupMemberAdapter;
import com.example.meltingbooks.group.menu.GroupMemberItem;
import com.example.meltingbooks.group.write.GroupWriteActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.FeedResponse;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupFeedPageResponse;
import com.example.meltingbooks.network.group.GroupFeedResponse;
import com.example.meltingbooks.network.group.GroupProfileResponse;
import com.example.meltingbooks.network.group.GroupPostResponse;
import com.example.meltingbooks.network.group.GroupReviewResponse;
import com.example.meltingbooks.network.group.MyGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFeedActivity extends BaseActivity implements GroupGoalSetting.OnGoalSetListener {

    private int groupId;
    private String groupName;

    private LinearLayout noticeBox;
    private EditText noticeEditText, recommendBookEditText;
    private ImageButton groupWriteButton;
    private RecyclerView groupRecyclerView, groupMenuRecyclerView, groupMemberRecyclerView;

    private ImageButton menuButton, groupJoinBtn;
    private View groupScrollView, menuLayout;
    private GestureDetector gestureDetector;
    private GroupProfileResponse groupInfo;
    private TextView groupTitle;

    private String token;
    private int userId;

    private GroupApi groupApi;
    private ApiService apiService;


    private RecyclerView groupFeedRecyclerView;
    private GroupFeedAdapter groupFeedAdapter;
    private List<GroupFeedItem> groupFeedList = new ArrayList<>(); //Null 방지 초기화




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_feed);
        setupBottomNavigation();



        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // token 받아오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        groupApi = ApiClient.getClient(token).create(GroupApi.class);
        apiService = ApiClient.getClient(token).create(ApiService.class);

        // 그룹 Id
        groupId = getIntent().getIntExtra("groupId", -1);
        Log.d("GroupFeedActivity", "현재 그룹 ID: " + groupId);
        if (groupId == -1) {
            Toast.makeText(this, "그룹 정보가 없습니다. 일부 기능이 비활성화됩니다.", Toast.LENGTH_SHORT).show();
        }


        // 그룹명
        groupTitle = findViewById(R.id.groupTitle);

        groupName = getIntent().getStringExtra("groupName");
        Log.d("GroupFeedActivity", "현재 그룹 Name: " + groupName);
        // 그룹명이 있으면 세팅, 없으면 기본값
        if (groupName != null) {
            groupTitle.setText(groupName);
        } else {
            groupTitle.setText("그룹");
        }

        //리사이클러뷰 설정
        groupFeedRecyclerView = findViewById(R.id.groupRecyclerView);

        groupFeedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupFeedAdapter = new GroupFeedAdapter(this, groupFeedList, groupId, feedDetailLauncher);;
        groupFeedRecyclerView.setAdapter(groupFeedAdapter);



        //서버에서 피드 불러오기
        loadGroupFeeds(groupId);


        // 뷰 초기화
        noticeBox = findViewById(R.id.noticeBox);
        noticeEditText = findViewById(R.id.noticeEditText);
        recommendBookEditText = findViewById(R.id.recommendBookEditText);
        groupWriteButton = findViewById(R.id.groupWrite);
        //groupRecyclerView = findViewById(R.id.groupRecyclerView);
        //groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupScrollView = findViewById(R.id.groupScrollView);


        menuLayout = findViewById(R.id.group_menu_button_layout);
        menuLayout.setVisibility(View.GONE);
        menuLayout.setClickable(true);
        menuLayout.setFocusable(true);
        menuLayout.setFocusableInTouchMode(true);

        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> showGroupInfo());

        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        menuLayout.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        noticeBox.setOnClickListener(v -> {
            String notice = noticeEditText.getText().toString();
            String book = recommendBookEditText.getText().toString();
            Toast.makeText(this, "공지: " + notice + "\n추천도서: " + book, Toast.LENGTH_SHORT).show();
        });

        groupWriteButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupFeedActivity.this, GroupWriteActivity.class);
            intent.putExtra("groupId", groupId); // ✅ 반드시 전달
            startActivity(intent);
        });

        groupJoinBtn = findViewById(R.id.group_create_join);
        groupJoinBtn.setOnClickListener(v -> {
            if (groupId != -1) {
                new GroupController(this).joinGroup(groupId);
            }
        });


        // 그룹 정보 서버에서 가져오기
        fetchGroupInfo(); // <- 여기서 groupInfo를 채움

        // 그룹 탈퇴/삭제 버튼 클릭
        menuLayout.findViewById(R.id.btn_leave_group).setOnClickListener(v -> handleLeaveOrDeleteGroup());

        ///그룹 메뉴
        // 내 그룹 리스트
        groupMenuRecyclerView = menuLayout.findViewById(R.id.myGroupRecyclerView);
        if(groupMenuRecyclerView == null){
            Log.e("GroupFeedActivity", "groupMenuRecyclerView is null!");
        }
        groupMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // ← 꼭 먼저
        List<GroupListItem> menuGroupList = new ArrayList<>();
        GroupListAdapter adapter = new GroupListAdapter(menuGroupList, groupId); // groupId = 현재 피드 그룹
        groupMenuRecyclerView.setAdapter(adapter);

        // 그룹 멤버 RecyclerView
        groupMemberRecyclerView = menuLayout.findViewById(R.id.groupMemberRecyclerView);
        groupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<GroupMemberItem> menuMemberList = new ArrayList<>();
        GroupMemberAdapter memberAdapter = new GroupMemberAdapter(menuMemberList, groupId); // groupId = 현재 피드 그룹
        groupMemberRecyclerView.setAdapter(memberAdapter);


        // 내 그룹 조회
        groupApi.getMyGroups().enqueue(new Callback<ApiResponse<List<MyGroup>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MyGroup>>> call,
                                   Response<ApiResponse<List<MyGroup>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MyGroup> myGroups = response.body().getData();

                    // 각 내 그룹의 프로필 API 조회
                    for (MyGroup g : myGroups) {
                        groupApi.getGroupById(g.getGroupId()).enqueue(new Callback<GroupPostResponse>() {
                            @Override
                            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> res) {
                                if (res.isSuccessful() && res.body() != null) {
                                    GroupPostResponse groupDetail = res.body();

                                    // 내 그룹 RecyclerView에 추가
                                    GroupListItem item = new GroupListItem(
                                            groupDetail.getData().getId(),
                                            groupDetail.getData().getName(),
                                            groupDetail.getData().getGroupImageUrl()
                                    );

                                    // ✅ 현재 피드 그룹이면 맨 위로 추가
                                    if (groupDetail.getData().getId() == groupId) {
                                        menuGroupList.add(0, item); // 맨 위로 추가
                                    } else {
                                        menuGroupList.add(item); // 일반 그룹은 뒤에 추가
                                    }
                                    Log.d("GroupFeed", "불러온 게시글 개수: " + groupFeedList.size());
                                    adapter.notifyDataSetChanged(); // 데이터 갱신
                                }
                            }

                            @Override
                            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                                Log.e("GroupAPI", "그룹 프로필 조회 실패", t);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MyGroup>>> call, Throwable t) {
                Log.e("GroupAPI", "내 그룹 조회 실패", t);
            }
        });


        // 그룹 멤버 조회
        groupApi.getGroupById(groupId).enqueue(new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GroupProfileResponse groupData = response.body().getData();
                    int groupId = groupData.getId();

                    List<GroupMemberItem> memberItems = groupData.getMembers();

                    // 본인이면 맨 위로
                    List<GroupMemberItem> sortedMembers = new ArrayList<>();

                    for (GroupMemberItem member : memberItems) {
                        member.setGroupId(groupId); // groupId 세팅

                        if (member.getUserId() == userId) {
                            // 본인은 맨 위로 추가
                            sortedMembers.add(0, member);
                        } else {
                            // 나머지는 뒤로 추가
                            sortedMembers.add(member);
                        }
                    }

                    // RecyclerView에 반영
                    menuMemberList.clear();
                    menuMemberList.addAll(memberItems);
                    memberAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                Log.e("GroupAPI", "그룹 멤버 조회 실패", t);
            }
        });


        // Fragment 초기화
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.group_goal_fragment, new GroupGoalSetting())
                    .commit();
        }

        // 예시 목표 데이터 불러오기
        //loadGroupProgress();

        ImageButton goToUpload = findViewById(R.id.groupWrite);
        goToUpload.setOnClickListener(v -> {
            Intent intent = new Intent(GroupFeedActivity.this, GroupWriteActivity.class);
            intent.putExtra("groupId", groupId); // ✅ 올바르게 전달
            startActivity(intent);
        });
    }

    // 피드 갱신
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra("refreshFeed", false)) {
            loadGroupFeeds(groupId); // 서버에서 전체 피드 다시 불러오기
        }
    }

    private void loadGroupFeeds(int groupId) {

        groupApi = ApiClient.getClient(token).create(GroupApi.class);

        Call<ApiResponse<GroupFeedPageResponse>> call =
                groupApi.getGroupFeed("Bearer " + token, groupId, 0, 10);

        call.enqueue(new Callback<ApiResponse<GroupFeedPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<GroupFeedPageResponse>> call,
                                   Response<ApiResponse<GroupFeedPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null) {

                    GroupFeedPageResponse pageResponse = response.body().getData();
                    List<GroupFeedResponse.Post> posts = pageResponse.getPosts().getContent();
                    List<GroupFeedResponse.Post> notices = pageResponse.getNotices();
                    List<GroupFeedResponse.Post> recommendedBooks = pageResponse.getRecommendedBooks();
                    List<GroupFeedResponse.Post> goals = pageResponse.getGoals();

                    groupFeedList.clear();

                    // 각 카테고리 처리
                    for (GroupFeedResponse.Post post : posts) addSinglePost(post, "POST", groupId);
                    for (GroupFeedResponse.Post post : notices) addSinglePost(post, "NOTICE", groupId);
                    for (GroupFeedResponse.Post post : recommendedBooks) addSinglePost(post, "RECOMMENDED_BOOK", groupId);
                    for (GroupFeedResponse.Post post : goals) addSinglePost(post, "GOAL_SHARE", groupId);

                    groupFeedAdapter.notifyDataSetChanged();

                    Log.d("GroupFeed", "불러온 게시글 수: " + posts.size());
                    Log.d("GroupFeed", "전체 페이지: " + pageResponse.getPosts().getTotalPages()
                            + ", 마지막 페이지 여부: " + pageResponse.getPosts().isLast());

                } else {
                    Log.e("GroupFeed", "GroupFeed 응답 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<GroupFeedPageResponse>> call, Throwable t) {
                Log.e("GroupFeed", "GroupFeed API 실패: " + t.getMessage());
            }
        });
    }

    // 단일 게시글 처리 함수
    private void addSinglePost(GroupFeedResponse.Post post, String postType, int groupId) {
        String firstImage = (post.getReviewImageUrls() != null && !post.getReviewImageUrls().isEmpty())
                ? post.getReviewImageUrls().get(0)
                : null;

        groupApi.getPost("Bearer " + token, groupId, post.getReviewId(), post.getUserId())
                .enqueue(new Callback<ApiResponse<GroupReviewResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupReviewResponse>> call,
                                           Response<ApiResponse<GroupReviewResponse>> reviewResponse) {
                        if (reviewResponse.isSuccessful() && reviewResponse.body() != null &&
                                reviewResponse.body().getData() != null) {

                            GroupReviewResponse review = reviewResponse.body().getData();

                            GroupFeedItem item = new GroupFeedItem(
                                    postType,
                                    post.getNickname(),
                                    review.getTitle(),
                                    post.getContent(),
                                    post.getCreatedAt(),
                                    firstImage,
                                    post.getUserProfileImage(),
                                    post.getCommentCount(),
                                    post.getLikeCount(),
                                    post.getTagId(),
                                    groupId
                            );
                            item.setPostId(post.getReviewId());
                            groupFeedList.add(item);
                            groupFeedAdapter.notifyDataSetChanged();

                        } else {
                            Log.e("GroupFeed", "단일 게시글 조회 실패: " + reviewResponse.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GroupReviewResponse>> call, Throwable t) {
                        Log.e("GroupFeed", "단일 게시글 API 실패: " + t.getMessage());
                    }
                });
    }




    @Override
    public void onGoalSet(GroupGoalSetting.GroupGoal goal) {
        Toast.makeText(this, "목표 저장됨: " + goal.targetBooks + "권", Toast.LENGTH_SHORT).show();

        GroupGoalSetting fragment = (GroupGoalSetting)
                getSupportFragmentManager().findFragmentById(R.id.group_goal_fragment);
        if (fragment != null) {
            fragment.updateGoalProgress(goal.targetBooks, goal.targetReviews, goal.targetTime,
                    0, 0, 0);
        }
    }

    private void loadGroupProgress() {
        int targetBooks = 30, targetReviews = 15, targetTime = 100;
        int[] memberBooks = {5,3,2}, memberReviews = {1,2,0}, memberTimes = {10,5,3};

        int sumBooks=0, sumReviews=0, sumTimes=0;
        for(int i=0;i<memberBooks.length;i++){
            sumBooks += memberBooks[i];
            sumReviews += memberReviews[i];
            sumTimes += memberTimes[i];
        }

        GroupGoalSetting fragment = (GroupGoalSetting)
                getSupportFragmentManager().findFragmentById(R.id.group_goal_fragment);
        if(fragment != null){
            fragment.updateGoalProgress(targetBooks, targetReviews, targetTime,
                    sumBooks, sumReviews, sumTimes);
        }
    }

    private void showGroupInfo() {
        menuLayout.setVisibility(View.VISIBLE);
        groupScrollView.setVisibility(View.GONE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        menuLayout.startAnimation(slideUp);
    }

    private void hideGroupInfoAndFinish() {
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        menuLayout.startAnimation(slideDown);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                menuLayout.setVisibility(View.GONE);
                groupScrollView.setVisibility(View.VISIBLE);
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50, SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1==null||e2==null) return false;
            float diffY = e2.getY()-e1.getY();
            if(Math.abs(diffY)>SWIPE_THRESHOLD && Math.abs(velocityY)>SWIPE_VELOCITY_THRESHOLD){
                if(diffY>0){ hideGroupInfoAndFinish(); return true;}
            }
            return false;
        }
    }


    //그룹 탈퇴하기/삭제하기
    private void handleLeaveOrDeleteGroup() {
        if (groupId == -1) return;

        if (groupInfo == null) {
            Toast.makeText(this, "그룹 정보를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("GroupFeedActivity", "groupInfo: " + groupInfo);
        Log.d("GroupFeedActivity", "currentUserId: " + getCurrentUserId());


        GroupController controller = new GroupController(this);

        if (isGroupOwner()) { // 그룹장인 경우 -> 그룹 삭제
            controller.deleteGroup(groupId, new GroupController.OnDeleteGroupCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GroupFeedActivity.this, "그룹 삭제 완료", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(GroupFeedActivity.this, GroupActivity.class));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(GroupFeedActivity.this, "삭제 실패: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else { // 일반 멤버 -> 그룹 탈퇴
            controller.leaveGroup(groupId, new GroupController.OnLeaveGroupCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GroupFeedActivity.this, "그룹 탈퇴 완료", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(GroupFeedActivity.this, GroupActivity.class));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(GroupFeedActivity.this, "탈퇴 실패: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 현재 로그인한 유저가 그룹장인지 확인
    private boolean isGroupOwner() {
        if (groupInfo == null) return false;
        int currentUserId = getCurrentUserId();
        return currentUserId == groupInfo.getOwnerId();
    }

    //userId 가져오기
    private int getCurrentUserId() {
        return getSharedPreferences("auth", MODE_PRIVATE)
                .getInt("userId", -1);
    }



    private void fetchGroupInfo() {
        GroupController groupController = new GroupController(this);

        groupController.getGroupById(groupId, new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    groupInfo = response.body().getData();

                    Log.d("GroupFeedActivity", "서버에서 받은 그룹 정보: "
                            + "id=" + groupInfo.getId()
                            + ", ownerId=" + groupInfo.getOwnerId()
                            + ", name=" + groupInfo.getName());

                    Log.d("GroupFeedActivity", "현재 로그인 유저 ID: " + getCurrentUserId());

                    menuLayout.findViewById(R.id.btn_leave_group).setEnabled(true);
                } else {
                    Toast.makeText(GroupFeedActivity.this,
                            "그룹 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(GroupFeedActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 피드 갱신 런처
    private final ActivityResultLauncher<Intent> feedDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();

                            int deletedPostId = data.getIntExtra("deletedPostId", -1);
                            if (deletedPostId != -1) removeFeedFromList(deletedPostId);

                            GroupFeedResponse updatedFeed = (GroupFeedResponse) data.getSerializableExtra("updatedFeed");
                           // if (updatedFeed != null) updateFeedInList(updatedFeed);
                        }
                    }
            );

    // 피드 삭제 갱신
    private void removeFeedFromList(int postId) {
        for (int i = 0; i < groupFeedList.size(); i++) {
            if (groupFeedList.get(i).getPostId() == postId) { // FeedResponse에 reviewId가 있다고 가정
                groupFeedList.remove(i);
                groupFeedAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

/*
    //피드 수정 갱신
    private void updateFeedInList(GroupFeedResponse updatedFeed) {
        for (int i = 0; i < groupFeedList.size(); i++) {
            GroupFeedItem item = groupFeedList.get(i);
            if (item.getPostId() == updatedFeed.getPId()) { // postId와 reviewId 비교
                // FeedItem 필드 업데이트
                item.setTitle(updatedFeed.getTitle());
                item.setContent(updatedFeed.getContent());

                List<String> images = updatedFeed.getReviewImageUrls();
                if (images != null && !images.isEmpty()) {
                    item.setImageUrl(images.get(0)); // 첫 번째 이미지 사용
                } else {
                    item.setImageUrl(null); // 이미지 없으면 null
                }

                groupFeedAdapter.notifyItemChanged(i);
                break;
            }
        }
    }
*/
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Group;
    }
}
