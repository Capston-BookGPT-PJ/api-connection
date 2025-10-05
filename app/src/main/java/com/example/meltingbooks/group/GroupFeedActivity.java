package com.example.meltingbooks.group;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.calendar.view.GoalProgressView;
import com.example.meltingbooks.group.goal.GroupGoalSetting;
import com.example.meltingbooks.group.menu.GroupListAdapter;
import com.example.meltingbooks.group.menu.GroupListItem;
import com.example.meltingbooks.group.menu.GroupMemberAdapter;
import com.example.meltingbooks.group.menu.GroupMemberItem;
import com.example.meltingbooks.group.profile.GroupProfileActivity;
import com.example.meltingbooks.group.write.GroupWriteActivity;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupResponse;
import com.example.meltingbooks.network.group.GroupSingleList;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFeedActivity extends BaseActivity implements GroupGoalSetting.OnGoalSetListener {

    private int groupId;

    private LinearLayout noticeBox;
    private EditText noticeEditText, recommendBookEditText;
    private ImageButton groupWriteButton;
    private RecyclerView groupRecyclerView, groupMenuRecyclerView, groupMemberRecyclerView;

    private ImageButton menuButton, groupJoinBtn;
    private View groupScrollView, menuLayout;
    private GestureDetector gestureDetector;
    private GroupResponse groupInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_feed);
        setupBottomNavigation();

        // 그룹 Id
        groupId = getIntent().getIntExtra("groupId", -1);
        Log.d("GroupFeedActivity", "현재 그룹 ID: " + groupId);
        if (groupId == -1) {
            Toast.makeText(this, "그룹 정보가 없습니다. 일부 기능이 비활성화됩니다.", Toast.LENGTH_SHORT).show();
        }

        // 뷰 초기화
        noticeBox = findViewById(R.id.noticeBox);
        noticeEditText = findViewById(R.id.noticeEditText);
        recommendBookEditText = findViewById(R.id.recommendBookEditText);
        groupWriteButton = findViewById(R.id.groupWrite);
        groupRecyclerView = findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        groupWriteButton.setOnClickListener(v -> startActivity(new Intent(this, GroupWriteActivity.class)));

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




        // 예시 피드 데이터
        List<GroupFeedItem> groupPosts = new ArrayList<>();
        groupPosts.add(new GroupFeedItem("홍길동", "인생 책 추천해주세요.", "이 책 정말 인생책이에요.", "2시간 전", "https://i.imgur.com/iWf9Yuh.jpeg"));
        groupPosts.add(new GroupFeedItem("이몽룡", "추천 도서 감상입니다.", "인상적입니다.", "1일 전", null));
        groupRecyclerView.setAdapter(new GroupFeedAdapter(this, groupPosts));

        // 메뉴 RecyclerView
        groupMenuRecyclerView = menuLayout.findViewById(R.id.myGroupRecyclerView);
        groupMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<GroupListItem> menuGroupList = new ArrayList<>();
        menuGroupList.add(new GroupListItem("작은 책방 모임", R.drawable.sample_profile2));
        menuGroupList.add(new GroupListItem("SF 클럽", R.drawable.sample_profile2));
        menuGroupList.add(new GroupListItem("심리학 책 읽기", R.drawable.sample_profile2));
        groupMenuRecyclerView.setAdapter(new GroupListAdapter(menuGroupList));

        // 멤버 RecyclerView
        groupMemberRecyclerView = menuLayout.findViewById(R.id.groupMemberRecyclerView);
        groupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<GroupMemberItem> groupMemberList = new ArrayList<>();
        groupMemberList.add(new GroupMemberItem("홍길동", R.drawable.sample_profile2));
        groupMemberList.add(new GroupMemberItem("이몽룡", R.drawable.sample_profile2));
        groupMemberList.add(new GroupMemberItem("성춘향", R.drawable.sample_profile2));
        groupMemberRecyclerView.setAdapter(new GroupMemberAdapter(groupMemberList));

        // Fragment 초기화
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.group_goal_fragment, new GroupGoalSetting())
                    .commit();
        }

        // 예시 목표 데이터 불러오기
        loadGroupProgress();


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
        /**int targetBooks = 30, targetReviews = 15, targetTime = 100;
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
        }*/
        // 20dp 높이, 제목 텍스트 크기 20sp → px 변환
        float titlePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());

        GoalProgressView goal1Detail = findViewById(R.id.goal1_view_detail);
        goal1Detail.setUnit("권");
        goal1Detail.setProgressWithGoal(5, 15, 20, titlePx);

        GoalProgressView goal2Detail = findViewById(R.id.goal2_view_detail);
        goal2Detail.setUnit("개");
        goal2Detail.setProgressWithGoal(10,30,0, titlePx);

        GoalProgressView goal3Detail = findViewById(R.id.goal3_view_detail);
        goal3Detail.setUnit("시간");
        goal3Detail.setProgressWithGoal(3, 20, 20, titlePx);

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
