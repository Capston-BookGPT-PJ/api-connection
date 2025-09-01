package com.example.meltingbooks.group.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.group.create.CreateGroupActivity;
import com.example.meltingbooks.group.profile.GroupProfileActivity;
import com.example.meltingbooks.group.profile.GroupProfileItem;
import com.example.meltingbooks.R;
import com.example.meltingbooks.search.SearchActivity;

import java.util.ArrayList;
import java.util.List;

public class GroupHomeActivity extends AppCompatActivity {

    private ImageButton createGroupButton, searchGroupButton;
    private RecyclerView popularGroupsRecyclerView, recentGroupsRecyclerView;
    private GroupCardItemAdapter popularAdapter, recentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_home);

        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        createGroupButton = findViewById(R.id.createGroupButton);
        searchGroupButton = findViewById(R.id.searchGroupButton);

        popularGroupsRecyclerView = findViewById(R.id.popularGroupsRecyclerView);
        recentGroupsRecyclerView = findViewById(R.id.recentGroupsRecyclerView);

        setupRecyclerViews();

        createGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupHomeActivity.this, CreateGroupActivity.class);
            startActivity(intent);
        });

        searchGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupHomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        // 예시 데이터 생성 - 실제로는 서버나 DB에서 받아와야 함
        List<GroupCardItem> popularGroups = new ArrayList<>();
        GroupCardItem item1 = new GroupCardItem("베스트 독서회", "베스트셀러를 함께 읽어요!", "https://i.imgur.com/iWf9Yuh.jpeg");
        item1.setCategory("소셜 토론 그룹");
        item1.setIntroTitle("그룹 소개");
        item1.setIntroDetail("\uD83D\uDCCC 인증 방법: 일주일 동안 일부 부분에서 감명 깊은 부분 사진 찍어 올리고 감상문 공유하기.\\n\\n\uD83D\uDCDD 모임 방법: 오프라인으로 진행하며 공지 및 게시글에서 자유롭게 토론\\n\\n❌ 주의 사항: 그룹 내 분위기를 흐리는 글 또는 그룹 질서를 어길 시 강제 조치.");
        popularGroups.add(item1);

        GroupCardItem item2 = new GroupCardItem("고전문학 모임", "고전 읽고 토론하기", "https://i.imgur.com/다른이미지.jpeg");
        item2.setCategory("문학 토론 그룹");
        item2.setIntroTitle("그룹 소개");
        popularGroups.add(item2);


        List<GroupCardItem> recentGroups = new ArrayList<>();
        recentGroups.add(new GroupCardItem("신규 심리책 모임", "심리학 책 첫 모임!", R.drawable.sample_profile2));
        recentGroups.add(new GroupCardItem("환경 독서회", "환경 관련 책 읽고 토론", R.drawable.sample_profile2));

        GroupCardItemAdapter.OnItemClickListener listener = item -> {
            String imageUrl = item.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = ""; // 기본 이미지 URL
            }

            GroupProfileItem profileItem = new GroupProfileItem(
                    item.getName(),
                    imageUrl,
                    item.getCategory(),
                    item.getIntroTitle(),
                    item.getIntroDetail()
            );

            Intent intent = new Intent(GroupHomeActivity.this, GroupProfileActivity.class);
            intent.putExtra("groupProfile", profileItem);
            startActivity(intent);
        };

        popularAdapter = new GroupCardItemAdapter(this, popularGroups, listener);
        recentAdapter = new GroupCardItemAdapter(this, recentGroups, listener);

        popularGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recentGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        popularGroupsRecyclerView.setAdapter(popularAdapter);
        recentGroupsRecyclerView.setAdapter(recentAdapter);
    }
}
