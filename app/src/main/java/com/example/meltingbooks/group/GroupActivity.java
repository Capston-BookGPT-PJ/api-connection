package com.example.meltingbooks.group;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.group.goal.GroupGoalActivity;
import com.example.meltingbooks.group.home.GroupHomeActivity;
import com.example.meltingbooks.group.menu.GroupListAdapter;
import com.example.meltingbooks.group.menu.GroupListItem;
import com.example.meltingbooks.group.menu.GroupMemberAdapter;
import com.example.meltingbooks.group.menu.GroupMemberItem;
import com.example.meltingbooks.R;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends BaseActivity {

    private LinearLayout noticeBox;
    private EditText noticeEditText, recommendBookEditText;
    private ImageButton groupWriteButton;
    private ImageView groupArrow;
    private RecyclerView groupRecyclerView;
    private RecyclerView groupMenuRecyclerView, groupMemberRecyclerView;

    private ImageButton menuButton, groupJoinBtn; // 메뉴 버튼
    private View groupScrollView; // 메인 그룹화면의 스크롤뷰 (공지, 게시글 등)
    private View menuLayout; // 아래에서 올라오는 그룹 정보 레이아웃
    private GestureDetector gestureDetector; // 스와이프 감지용


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setupBottomNavigation();

        // 상태바 색상 조정
        /**if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }*/

        // 뷰 연결
        noticeBox = findViewById(R.id.noticeBox);
        noticeEditText = findViewById(R.id.noticeEditText);
        recommendBookEditText = findViewById(R.id.recommendBookEditText);
        groupArrow = findViewById(R.id.group_arrow);
        groupWriteButton = findViewById(R.id.groupWrite);
        groupRecyclerView = findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupScrollView = findViewById(R.id.groupScrollView);

        // 아래에서 올라오는 그룹 정보 레이아웃 (group_menu_button_layout)
        menuLayout = findViewById(R.id.group_menu_button_layout);
        menuLayout.setVisibility(View.GONE); // 처음엔 숨김
        menuLayout.setClickable(true);
        menuLayout.setFocusable(true);
        menuLayout.setFocusableInTouchMode(true);

        groupJoinBtn = findViewById(R.id.group_create_join);


        menuButton = findViewById(R.id.menuButton); // 메뉴 버튼

        // 메뉴 버튼 클릭 시 그룹 정보 레이아웃 보여주기
        menuButton.setOnClickListener(v -> showGroupInfo());

        // 스와이프 감지 설정 (아래에서 올라오는 레이아웃에만 적용)
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        menuLayout.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });


        // 공지 작성
        noticeBox.setOnClickListener(v -> {
            String notice = noticeEditText.getText().toString();
            String book = recommendBookEditText.getText().toString();
            Toast.makeText(this, "공지: " + notice + "\n추천도서: " + book, Toast.LENGTH_SHORT).show();
        });

        // 감상문 작성 버튼 → GroupWriteActivity 이동
        groupWriteButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupWriteActivity.class);
            startActivity(intent);
        });

        // 공통 독서 기록 추가 페이지 이동
        groupArrow.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupGoalActivity.class);
            startActivity(intent);
        });


        //그룹 피드 <-여기에 작성
        List<GroupFeedItem> groupPosts = new ArrayList<>();
        groupPosts.add(new GroupFeedItem("홍길동", "인생 책 추천해주세요.", "이 책 정말 인생책이에요. 꼭 읽어보세요!", "2시간 전", "https://i.imgur.com/iWf9Yuh.jpeg"));
        groupPosts.add(new GroupFeedItem("이몽룡", "추천 도서 감상입니다.", "인상적입니다.", "1일 전", null));

        GroupFeedAdapter adapter = new GroupFeedAdapter(this, groupPosts);
        groupRecyclerView.setAdapter(adapter);


        //그룹 관리 메뉴에 있는 그룹 생성 및 가입 버튼 동작
        groupJoinBtn.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupHomeActivity.class);
            startActivity(intent);
        });

        // 그룹 메뉴 RecyclerView (그룹 목록)
        groupMenuRecyclerView = menuLayout.findViewById(R.id.myGroupRecyclerView);
        groupMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<GroupListItem> menuGroupList = new ArrayList<>();
        menuGroupList.add(new GroupListItem("작은 책방 모임", R.drawable.sample_profile2));
        menuGroupList.add(new GroupListItem("SF 클럽", R.drawable.sample_profile2));
        menuGroupList.add(new GroupListItem("심리학 책 읽기", R.drawable.sample_profile2));
        GroupListAdapter groupMenuAdapter = new GroupListAdapter(menuGroupList);
        groupMenuRecyclerView.setAdapter(groupMenuAdapter);


        // 그룹 매뉴 멤버 RecyclerView
        groupMemberRecyclerView = menuLayout.findViewById(R.id.groupMemberRecyclerView);
        groupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<GroupMemberItem> groupMemberList = new ArrayList<>();
        groupMemberList.add(new GroupMemberItem("홍길동", R.drawable.sample_profile2));
        groupMemberList.add(new GroupMemberItem("이몽룡", R.drawable.sample_profile2));
        groupMemberList.add(new GroupMemberItem("성춘향", R.drawable.sample_profile2));
        GroupMemberAdapter groupMemberAdapter = new GroupMemberAdapter(groupMemberList);
        groupMemberRecyclerView.setAdapter(groupMemberAdapter);
    }



    // 그룹 메뉴 레이아웃 보여주기 (메뉴 버튼 클릭 시)
    private void showGroupInfo() {
        menuLayout.setVisibility(View.VISIBLE);
        groupScrollView.setVisibility(View.GONE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        menuLayout.startAnimation(slideUp);
    }

    // 그룹 메뉴 레이아웃 숨기기 (아래로 스와이프 시)
    private void hideGroupInfoAndFinish() {
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        menuLayout.startAnimation(slideDown);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                menuLayout.setVisibility(View.GONE);
                groupScrollView.setVisibility(View.VISIBLE);  // 여기로 이동
            }

            @Override public void onAnimationRepeat(Animation animation) {}
        });
    }


    // 스와이프 감지 리스너 (아래로 스와이프 시 그룹 메뉴 레이아웃 숨기기)
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffY = e2.getY() - e1.getY();
            Log.d("SwipeGesture", "diffY=" + diffY + ", velocityY=" + velocityY);
            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    Log.d("SwipeGesture", "Swipe down detected");
                    hideGroupInfoAndFinish();
                    return true;
                }
            }
            return false;
        }
    }
    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Group;
    }

}
