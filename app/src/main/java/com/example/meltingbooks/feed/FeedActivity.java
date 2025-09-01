package com.example.meltingbooks.feed;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.R;

import java.util.ArrayList;
import java.util.List;


public class FeedActivity extends BaseActivity {
    //피드 리사이클
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList;



    private int commentCounter = 0; // 초기 댓글 수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setupBottomNavigation();

        //글 작성 화면으로 이동
        ImageButton goToUpload = findViewById(R.id.goToUpload);

        goToUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, FeedWriteActivity.class);
                startActivity(intent);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            //상태바 디자인 설정--------------------------------------------------------
            // 상태바를 완전히 투명하게 만들어서 배경이 보이도록 설정
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT); // 상태바를 투명하게 설정

            // 레이아웃이 상태바 영역까지 확장되도록 설정
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            // 상태바 아이콘 & 글자를 검정색으로 변경
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }

            //피드 리사이클 뷰---------------------------------------------------
            feedRecyclerView = findViewById(R.id.feedRecyclerView);
            feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // 더미 데이터 추가
            feedList = new ArrayList<>();

            // 나중에 서버 통신으로 바꿀시 Null 처리 어떻게 할 것인지 생각해보기.
            // 이미지 게시글
            feedList.add(new FeedItem("Alice", "이 고양이좀 봐~! 책읽고 있어", "2025-03-10", "https://i.imgur.com/iWf9Yuh.jpeg"));

            // 투표 게시글
            /*List<String> pollOptions = Arrays.asList("재미있다", "그저 그렇다", "별로다");x
            feedList.add(new FeedItem("Bob", "이번 책 어땠어?", "2025-03-09", null, pollOptions));*/

            //추후 서버 연결해서 바꾸는 식으로 조정.
            feedList.add(new FeedItem("Alice", "오늘 읽은 책 너무 재밌었어!------------------------------------------------------------------------------------------------------------------------------------------------------------------------", "2025-03-10", null));
            feedList.add(new FeedItem("Bob", "독서가 정말 힘이 되네.", "2025-03-09", null));
            feedList.add(new FeedItem("Charlie", "이번 책도 완독했다!", "2025-03-08", null));
            feedList.add(new FeedItem("Alice", "오늘 읽은 책 너무 재밌었어!", "2025-03-10", null));
            feedList.add(new FeedItem("Bob", "독서가 정말 힘이 되네.", "2025-03-09", null));
            feedList.add(new FeedItem("Charlie", "이번 책도 완독했다!", "2025-03-08", null));
            feedList.add(new FeedItem("Alice", "오늘 읽은 책 너무 재밌었어!", "2025-03-10", null));
            feedList.add(new FeedItem("Bob", "독서가 정말 힘이 되네.", "2025-03-09", null));
            feedList.add(new FeedItem("Charlie", "이번 책도 완독했다!", "2025-03-08", null));
            feedList.add(new FeedItem("Alice", "오늘 읽은 책 너무 재밌었어!", "2025-03-10", null));
            feedList.add(new FeedItem("Bob", "독서가 정말 힘이 되네.", "2025-03-09", null));
            feedList.add(new FeedItem("Charlie", "이번 책도 완독했다!", "2025-03-08", null));

            feedAdapter = new FeedAdapter(this, feedList);
            feedRecyclerView.setAdapter(feedAdapter);

        }

    }
    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Feed;
    }

}