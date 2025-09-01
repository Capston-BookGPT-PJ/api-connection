package com.example.meltingbooks.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meltingbooks.R;
import com.example.meltingbooks.browse.BrowseActivity;
import com.example.meltingbooks.calendar.CalendarActivity;
import com.example.meltingbooks.feed.FeedActivity;
import com.example.meltingbooks.group.GroupActivity;
import com.example.meltingbooks.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    //하단 메뉴
    protected BottomNavigationView bottomNavigationView;
    protected ImageView gradientCircle;
    protected int[] iconPositions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureTransparentStatusBar(); //BaseActivity를 상속받는 모든 액티비티에 적용
    }

    protected void setupBottomNavigation() {
        //하단 메뉴 애니메이션-------
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        gradientCircle = findViewById(R.id.gradientCircle);

        // 기본 애니메이션 제거 (Shift Mode 제거)
        bottomNavigationView.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_UNLABELED);

        // 아이템 클릭 시 애니메이션 방지
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 클릭된 아이템의 강조 효과 제거 (예: Ripple Effect 무시)
                View view = bottomNavigationView.findViewById(item.getItemId());
                view.setPressed(false);
                view.setSelected(false);

                // 원하는 작업 수행
                return true;
            }
        });

        /*BottomNavigationView.setOnItemSelectedListener(item -> {
            int position = getSelectedPosition(item.getItemId());
            if (position >= 0 && iconPositions != null) {
                gradientCircle.animate().x(iconPositions[position]).setDuration(300).start();
            }

            int itemId = item.getItemId();
            if (itemId == R.id.Feed) {
                if (!(this instanceof FeedActivity)) {
                    startActivity(new Intent(this, FeedActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.Calendar) {
                if (!(this instanceof CalendarActivity)) {
                    startActivity(new Intent(this, CalendarActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.Profile) {
                if (!(this instanceof ProfileActivity)) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            }

            // 다른 메뉴들도 여기에 추가
            return false;
        });*/

        // 애니메이션 대신 즉시 위치 수정
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int position = getSelectedPosition(item.getItemId());
            if (position >= 0 && iconPositions != null) {
                // 애니메이션 없이 즉시 위치 변경
                gradientCircle.setX(iconPositions[position]);
            }

            int itemId = item.getItemId();
            if (itemId == R.id.Feed) {
                if (!(this instanceof FeedActivity)) {
                    startActivity(new Intent(this, FeedActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.Browser) {
                if (!(this instanceof BrowseActivity)) {
                    startActivity(new Intent(this, BrowseActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.Calendar) {
                if (!(this instanceof CalendarActivity)) {
                    startActivity(new Intent(this, CalendarActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.Group) {
                if (!(this instanceof GroupActivity)) {
                    startActivity(new Intent(this, GroupActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.Profile) {
                if (!(this instanceof ProfileActivity)) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            }
            return false;
        });

        // 아이콘의 위치를 구하기 위해 뷰가 그려진 후 위치 계산
        bottomNavigationView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int count = bottomNavigationView.getMenu().size();
            iconPositions = new int[count];

            for (int i = 0; i < count; i++) {
                View itemView = bottomNavigationView.findViewById(bottomNavigationView.getMenu().getItem(i).getItemId());
                iconPositions[i] = (int) itemView.getX() + (itemView.getWidth() / 2) - (gradientCircle.getWidth() / 2);
            }

            // 처음 앱이 실행될 때 원을 첫 번째 아이콘 아래로 이동시키기
                /*if (iconPositions.length > 0) {
                    gradientCircle.setX(iconPositions[0]);  // 첫 번째 아이콘 (Feed) 위치로 설정
                }*/

            // 현재 선택된 메뉴 위치로 gradient 원 이동
            int selectedItemId = bottomNavigationView.getSelectedItemId();
            int pos = getSelectedPosition(selectedItemId);
            if (pos >= 0) {
                gradientCircle.setX(iconPositions[pos]);
            }
        });

        // 네비게이터 위치 자동 선택
        bottomNavigationView.setSelectedItemId(getCurrentNavItemId());
    }

    //하단 메뉴 위치 설정
    private int getSelectedPosition(int itemId) {
        if (itemId == R.id.Feed) return 0;
        else if (itemId == R.id.Browser) return 1;
        else if (itemId == R.id.Calendar) return 2;
        else if (itemId == R.id.Group) return 3;
        else if (itemId == R.id.Profile) return 4;
        else return -1;
    }

    //상태바 디자인 설정
    protected void configureTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // 상태바를 완전히 투명하게 만들어서 배경이 보이도록 설정
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

            // 레이아웃이 상태바 영역까지 확장되도록 설정
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

            // 상태바 아이콘 & 글자를 검정색으로 변경
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    //뒤로 가기 시 하단 메뉴 위치 다시 계산
    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null && gradientCircle != null) {
            if (iconPositions == null || iconPositions.length == 0) {
                int count = bottomNavigationView.getMenu().size();
                iconPositions = new int[count];
                for (int i = 0; i < count; i++) {
                    View itemView = bottomNavigationView.findViewById(bottomNavigationView.getMenu().getItem(i).getItemId());
                    iconPositions[i] = (int) itemView.getX() + (itemView.getWidth() / 2) - (gradientCircle.getWidth() / 2);
                }
            }

            int pos = getSelectedPosition(getCurrentNavItemId());
            if (pos >= 0 && pos < iconPositions.length) {
                gradientCircle.setX(iconPositions[pos]);
                bottomNavigationView.setSelectedItemId(getCurrentNavItemId());
            }
        }
    }

    //뒤로가기 시 화면 전환 모션 삭제
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    protected abstract int getCurrentNavItemId();
}
