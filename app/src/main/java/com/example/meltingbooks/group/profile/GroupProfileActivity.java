package com.example.meltingbooks.group.profile;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;

public class GroupProfileActivity extends AppCompatActivity {

    private TextView groupName;
    private ImageView groupIntroImage;
    private TextView groupCategory;
    private TextView groupIntroTile;
    private TextView groupIntroDetail;
    private ImageButton joinGroupButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_profile);
        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        groupName = findViewById(R.id.groupName);
        groupIntroImage = findViewById(R.id.groupIntroImage);
        groupCategory = findViewById(R.id.groupCategory);
        groupIntroTile = findViewById(R.id.groupIntroTile);
        groupIntroDetail = findViewById(R.id.groupIntroDetail);
        joinGroupButton = findViewById(R.id.joinGroupButton);

        // Intent로부터 GroupProfileItem 객체 받아오기
        GroupProfileItem groupProfileItem = (GroupProfileItem) getIntent().getSerializableExtra("groupProfile");


        groupName.setText(groupProfileItem.getGroupName());
        groupCategory.setText(groupProfileItem.getGroupCategory());
        groupIntroTile.setText(groupProfileItem.getGroupIntroTitle());
        groupIntroDetail.setText(groupProfileItem.getGroupIntroDetail());

        String imageUrl = groupProfileItem.getGroupIntroImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            groupIntroImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .into(groupIntroImage);
        } else {
            groupIntroImage.setVisibility(View.GONE);
        }

        joinGroupButton.setOnClickListener(v -> {
            // 가입 신청 처리
        });
    }
}
