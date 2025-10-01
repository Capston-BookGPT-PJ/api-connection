package com.example.meltingbooks.group.comment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;

import java.util.ArrayList;
import java.util.List;

public class GroupCommentActivity extends AppCompatActivity {

    private RecyclerView commentRecyclerView;
    private GroupCommentAdapter commentAdapter;
    private List<GroupCommentItem> commentList;
    private EditText commentEditText;
    private ImageView postCommentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_comment_view);

        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        TextView postUserName = findViewById(R.id.postUserName);
        TextView postContentLine1 = findViewById(R.id.postContentLine1);
        TextView postContentLine2 = findViewById(R.id.postContentLine2);
        TextView groupPostTitle = findViewById(R.id.groupPostTitle);

        // 🔸 Intent로 받은 데이터 설정
        Intent intent = getIntent();
        String userName = intent.getStringExtra("postUserName");
        String content1 = intent.getStringExtra("postContentLine1");
        String content2 = getIntent().getStringExtra("postContentLine2");

        postUserName.setText(userName);
        postContentLine1.setText(content1);
        postContentLine2.setText(content2);
        groupPostTitle.setText("그룹 게시글");

        // 🔸 댓글 입력
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);

        // 🔸 댓글 리사이클러뷰 설정
        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentList = new ArrayList<>();
        commentAdapter = new GroupCommentAdapter(this, commentList);
        commentRecyclerView.setAdapter(commentAdapter);

        // 🔸 더미 댓글 추가
        commentList.add(new GroupCommentItem("User1", "멋진 책 추천 감사합니다.", R.drawable.sample_profile));
        commentList.add(new GroupCommentItem("User2", "저도 읽어볼게요!", R.drawable.sample_profile));
        commentAdapter.notifyDataSetChanged();

        // 🔸 댓글 추가 이벤트
        postCommentButton.setOnClickListener(v -> {
            String comment = commentEditText.getText().toString().trim();
            if (!comment.isEmpty()) {
                commentList.add(new GroupCommentItem("현재 사용자", comment, R.drawable.sample_profile));
                commentAdapter.notifyItemInserted(commentList.size() - 1);
                commentRecyclerView.scrollToPosition(commentList.size() - 1);
                commentEditText.setText("");
            }
        });
    }
}
