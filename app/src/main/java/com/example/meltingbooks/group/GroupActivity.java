package com.example.meltingbooks.group;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.group.profile.GroupCreateActivity;
import com.example.meltingbooks.group.profile.GroupProfileActivity;
import com.example.meltingbooks.group.profile.GroupProfileItem;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.group.GroupAllList;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupResponse;
import com.example.meltingbooks.network.group.GroupResponseAdapter;
import com.example.meltingbooks.search.SearchActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupActivity extends BaseActivity {

    private ImageButton createGroupButton, searchGroupButton;
    private RecyclerView popularGroupsRecyclerView, recentGroupsRecyclerView;
    private GroupResponseAdapter popularAdapter, recentAdapter;
    private ImageButton myGroup1, myGroup2, myGroup3;



    // 🔹 그룹 클릭 리스너
    private GroupResponseAdapter.OnItemClickListener listener = group -> {
        String imageUrl = group.getGroupImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = ""; // 기본 이미지
        }

        GroupProfileItem profileItem = new GroupProfileItem(
                group.getName(),
                imageUrl,
                group.getCategory(),
                "그룹 소개",
                group.getDescription()
        );

        Intent intent = new Intent(GroupActivity.this, GroupProfileActivity.class);
        intent.putExtra("groupId", group.getId());
        intent.putExtra("groupProfile", profileItem);
        startActivity(intent);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setupBottomNavigation();



        createGroupButton = findViewById(R.id.createGroupButton);
        searchGroupButton = findViewById(R.id.searchGroupButton);
        popularGroupsRecyclerView = findViewById(R.id.popularGroupsRecyclerView);
        recentGroupsRecyclerView = findViewById(R.id.recentGroupsRecyclerView);

        setupRecyclerViews();

        createGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupCreateActivity.class);
            startActivity(intent);
        });

        searchGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, SearchActivity.class);
            startActivity(intent);
        });


        // 내 그룹 클릭시 -> 각 그룹 피드로 이동
        myGroup1 = findViewById(R.id.myGroup1);
        myGroup2 = findViewById(R.id.myGroup2);
        myGroup3 = findViewById(R.id.myGroup3);

        myGroup1.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupFeedActivity.class);
            intent.putExtra("groupId", 7); // 그룹 ID 전달
            startActivity(intent);
        });

        myGroup2.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupFeedActivity.class);
            intent.putExtra("groupId", 9);
            startActivity(intent);
        });

        myGroup3.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupFeedActivity.class);
            //intent.putExtra("groupId", 3);
            startActivity(intent);
        });

        // 새 그룹이 있으면 Adapter에 추가
        GroupResponse newGroup = (GroupResponse) getIntent().getSerializableExtra("newGroup");
        if (newGroup != null) {
            List<GroupResponse> list = new ArrayList<>();
            list.add(newGroup);
            popularAdapter.addItem(newGroup);
        }
    }

    private void openGroupActivity(int groupId) {
        Intent intent = new Intent(this, GroupFeedActivity.class);
        intent.putExtra("groupId", groupId); // 그룹 ID 전달
        startActivity(intent);
    }
    private void setupRecyclerViews() {
        popularGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recentGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        popularAdapter = new GroupResponseAdapter(this, new ArrayList<>(), listener);
        recentAdapter = new GroupResponseAdapter(this, new ArrayList<>(), listener);

        popularGroupsRecyclerView.setAdapter(popularAdapter);
        recentGroupsRecyclerView.setAdapter(recentAdapter);

        fetchGroupsFromServer(); // 서버에서 그룹 가져오기
    }

    //그룹 전체 조회-> GroupAllList 사용
    private void fetchGroupsFromServer() {
        GroupController groupController = new GroupController(this);

        groupController.searchGroups(null, null, new Callback<GroupAllList>() {
            @Override
            public void onResponse(Call<GroupAllList> call, Response<GroupAllList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupResponse> allGroups = response.body().getData(); // 🔹 리스트 접근

                    // 인기 그룹: 멤버 수 기준 상위 10개
                    allGroups.sort((g1, g2) -> Integer.compare(g2.getMemberCount(), g1.getMemberCount()));
                    List<GroupResponse> popularGroups = new ArrayList<>(allGroups.subList(0, Math.min(10, allGroups.size())));

                    // 최신 그룹: 생성일 기준 상위 10개
                    allGroups.sort((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()));
                    List<GroupResponse> recentGroups = new ArrayList<>(allGroups.subList(0, Math.min(10, allGroups.size())));

                    popularAdapter.updateData(popularGroups);
                    recentAdapter.updateData(recentGroups);
                }
            }

            @Override
            public void onFailure(Call<GroupAllList> call, Throwable t) {
                t.printStackTrace();
            }


        });

    }
    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Group;
    }
}
