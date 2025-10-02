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

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.group.goal.GroupGoalSetting;
import com.example.meltingbooks.group.menu.GroupListAdapter;
import com.example.meltingbooks.group.menu.GroupListItem;
import com.example.meltingbooks.group.menu.GroupMemberAdapter;
import com.example.meltingbooks.group.menu.GroupMemberItem;
import com.example.meltingbooks.group.write.GroupWriteActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.FeedPageResponse;
import com.example.meltingbooks.network.feed.FeedResponse;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupFeedResponse;
import com.example.meltingbooks.network.group.GroupResponse;
import com.example.meltingbooks.network.group.GroupSingleList;
import com.example.meltingbooks.network.group.MyGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private GroupResponse groupInfo;
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
        groupFeedAdapter = new GroupFeedAdapter(this, groupFeedList, groupId);;
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
                        groupApi.getGroupById(g.getGroupId()).enqueue(new Callback<GroupSingleList>() {
                            @Override
                            public void onResponse(Call<GroupSingleList> call, Response<GroupSingleList> res) {
                                if (res.isSuccessful() && res.body() != null) {
                                    GroupSingleList groupDetail = res.body();

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
                            public void onFailure(Call<GroupSingleList> call, Throwable t) {
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
        groupApi.getGroupById(groupId).enqueue(new Callback<GroupSingleList>() {
            @Override
            public void onResponse(Call<GroupSingleList> call, Response<GroupSingleList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GroupResponse groupData = response.body().getData();
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
            public void onFailure(Call<GroupSingleList> call, Throwable t) {
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

        Call<GroupFeedResponse> call = groupApi.getGroupFeed(groupId, 0, 20);
        call.enqueue(new Callback<GroupFeedResponse>() {
            @Override
            public void onResponse(Call<GroupFeedResponse> call, Response<GroupFeedResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("GroupFeed", "GroupFeed 응답 비정상: " + (response.body() != null ? response.body().getError() : response.message()));
                    return;
                }

                GroupFeedResponse.GroupFeedData data = response.body().getData();
                if (data == null) return;

                groupFeedList.clear();

                // 모든 카테고리별 리스트를 합치기
                List<GroupFeedResponse.Post> allPosts = new ArrayList<>();
                if (data.getGoals() != null) allPosts.addAll(data.getGoals());
                if (data.getNotices() != null) allPosts.addAll(data.getNotices());
                if (data.getRecommendedBooks() != null) allPosts.addAll(data.getRecommendedBooks());
                if (data.getPosts() != null && data.getPosts().getContent() != null) allPosts.addAll(data.getPosts().getContent());

                for (GroupFeedResponse.Post post : allPosts) {
                    fetchUserProfileAndAddPost(post);
                }
            }

            @Override
            public void onFailure(Call<GroupFeedResponse> call, Throwable t) {
                Log.e("GroupFeed", "GroupFeed API 실패: " + t.getMessage());
            }
        });
    }

    // Feed API 호출해서 프로필 가져오고 그룹 피드 리스트에 추가
    private void fetchUserProfileAndAddPost(GroupFeedResponse.Post post) {
        int userId = post.getAuthorId();

        Call<ApiResponse<FeedPageResponse>> feedCall = apiService.getUserFeeds("Bearer " + token, userId, 0, 10);
        feedCall.enqueue(new Callback<ApiResponse<FeedPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FeedPageResponse>> call, Response<ApiResponse<FeedPageResponse>> response) {
                String profileImage = null;

                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null &&
                        response.body().getData().getContent() != null) {

                    for (FeedResponse feed : response.body().getData().getContent()) {
                        if (feed.getUserId() == post.getAuthorId()) {
                            profileImage = feed.getUserProfileImage();
                            break; // 찾으면 바로 중단
                        }
                    }
                }
                // GroupFeedItem 생성
                GroupFeedItem item = new GroupFeedItem(
                        post.getPostId(),
                        post.getType(),
                        post.getTitle(),
                        post.getContent(),
                        post.getImageUrl(),
                        post.getAuthorId(),
                        post.getAuthorName(),
                        post.getCreatedAt(),
                        post.getCommentCount(),
                        post.getLikeCount(),
                        profileImage,// Feed API에서 가져온 프로필,
                        groupId
                );

                groupFeedList.add(item);
                groupFeedAdapter.notifyItemInserted(groupFeedList.size() - 1);
            }

            @Override
            public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                Log.e("Feed", "Feed API 실패: " + t.getMessage());
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

        groupController.getGroupById(groupId, new Callback<GroupSingleList>() {
            @Override
            public void onResponse(Call<GroupSingleList> call, Response<GroupSingleList> response) {
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
            public void onFailure(Call<GroupSingleList> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(GroupFeedActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Group;
    }
}
