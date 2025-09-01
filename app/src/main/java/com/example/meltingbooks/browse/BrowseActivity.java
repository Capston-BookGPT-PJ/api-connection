package com.example.meltingbooks.browse;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.Book;
import com.example.meltingbooks.R;
import com.example.meltingbooks.search.SearchActivity;
import com.example.meltingbooks.User;
import com.google.android.flexbox.FlexboxLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrowseActivity extends BaseActivity {
    private ImageButton search;

    private ViewPager2 bookViewPager;
    private RecyclerView reviewRecyclerView;
    private BrowseBookAdapter bookAdapter;
    private BrowseReviewAdapter reviewAdapter;
    private List<Book> bookList = new ArrayList<>();
    private List<List<String>> reviewListByBook = new ArrayList<>();

    private View hashtagLayout;
    private View usersLayout;
    private FlexboxLayout hashtagFlexbox;

    private RecyclerView usersRecyclerView;
    private BrowseUsersAdapter userAdapter;
    private List<User> popularUserList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        setupBottomNavigation();

        // 상태바 색상 설정
        /**if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }*/

        // UI 연결
        search = findViewById(R.id.search);

        bookViewPager = findViewById(R.id.bookViewPager);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);

        hashtagLayout = findViewById(R.id.hashtagLayout);
        hashtagFlexbox = hashtagLayout.findViewById(R.id.hashtagFlexbox);

        usersLayout = findViewById(R.id.usersLayout);
        usersRecyclerView = findViewById(R.id.popularUsersRecyclerView);
        usersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 인기 책 ViewPager
        bookAdapter = new BrowseBookAdapter(this, bookList);
        bookViewPager.setAdapter(bookAdapter);

        //감상평 RecyclerView
        reviewAdapter = new BrowseReviewAdapter(new ArrayList<>());
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewRecyclerView.setAdapter(reviewAdapter);

        // 인기 책 크롤링 및 감상평 리스트 구성
        loadPopularBooks();

        // ViewPager 페이지 변경 시 감상평 갱신
        bookViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position < reviewListByBook.size()) {
                    reviewAdapter.updateReviews(reviewListByBook.get(position));
                }
            }
        });

        // 해시태그 목록 구성
        setupHashtags();

        // 탭 클릭 리스너
        TextView popularBooks = findViewById(R.id.popularBooks);
        TextView popularTags = findViewById(R.id.popularTags);
        TextView popularUsers = findViewById(R.id.popularUsers);

        popularBooks.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.VISIBLE);
            reviewRecyclerView.setVisibility(View.VISIBLE);
            hashtagLayout.setVisibility(View.GONE);
            usersLayout.setVisibility(View.GONE);
        });

        popularTags.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.GONE);
            reviewRecyclerView.setVisibility(View.GONE);
            hashtagLayout.setVisibility(View.VISIBLE);
            usersLayout.setVisibility(View.GONE);
        });

        popularUsers.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.GONE);
            reviewRecyclerView.setVisibility(View.GONE);
            hashtagLayout.setVisibility(View.GONE);
            usersLayout.setVisibility(View.VISIBLE);
        });
    }

    //인기 책 크롤링(이미지+제목) (알라딘 사이트 참고)
    private void loadPopularBooks() {
        new Thread(() -> {
            try {
                Document doc = Jsoup.connect("https://www.aladin.co.kr/shop/common/wbest.aspx?BranchType=1&bestType=Bestseller").get();
                Elements bookBoxes = doc.select(".ss_book_box");
                Set<String> imageUrlSet = new HashSet<>();

                for (Element bookBox : bookBoxes) {
                    Element titleElement = bookBox.selectFirst("a.bo3");
                    String title = (titleElement != null) ? titleElement.text().trim() : "제목없음";

                    Element imgElement = bookBox.selectFirst("img.front_cover");
                    String imageUrl = (imgElement != null) ? imgElement.attr("src") : "";

                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "https://www.aladin.co.kr" + imageUrl;
                    }

                    if ((imageUrl.endsWith(".jpg") || imageUrl.endsWith(".png")) && imageUrl.contains("cover") && !imageUrlSet.contains(imageUrl)) {
                        imageUrlSet.add(imageUrl);
                        bookList.add(new Book(title, imageUrl));

                        List<String> reviews = new ArrayList<>();
                        reviews.add(title + "에 대한 감상평 1");
                        reviews.add(title + "에 대한 감상평 2");
                        reviewListByBook.add(reviews);
                    }
                }

                runOnUiThread(() -> {
                    bookAdapter.notifyDataSetChanged();
                    if (!reviewListByBook.isEmpty()) {
                        reviewAdapter.updateReviews(reviewListByBook.get(0));
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //해시태그 예시
    private void setupHashtags() {
        String[] hashtags = {
                "#채식주의자", "#손글씨감상문", "#장편소설", "#책추천", "#노벨문학상수상",
                "#자기계발", "#독서마라톤", "#외국도서", "#인문학", "#오늘독서완료", "#한강"
        };

        //해시태그 편집
        for (String tag : hashtags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            // 글자 수에 맞게 사이즈 조절되도록
            tagView.setMinWidth(0);
            tagView.setMinEms(0);
            tagView.setMaxWidth(Integer.MAX_VALUE);
            //텍스트 디자인
            tagView.setTextSize(15);
            tagView.setTextColor(ContextCompat.getColor(this, R.color.text_blue));
            tagView.setGravity(Gravity.CENTER);
            //해시태그 디자인
            tagView.setBackground(ContextCompat.getDrawable(this, R.drawable.hashtag1));
            tagView.setPadding(8, 8, 8, 8); //해시태그 간격
            //레이아웃
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(12, 20, 12, 20); //해시태그 간 간격
            tagView.setLayoutParams(params);


            tagView.setOnClickListener(v -> {
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                if (!selected) {
                    // 선택 상태로 변경
                    tagView.setBackground(ContextCompat.getDrawable(this, R.drawable.hashtag2));
                    tagView.setTextColor(ContextCompat.getColor(this, R.color.white));
                } else {
                    // 선택 해제 상태로 변경
                    tagView.setBackground(ContextCompat.getDrawable(this, R.drawable.hashtag1));
                    tagView.setTextColor(ContextCompat.getColor(this, R.color.text_blue));
                }
            });

            hashtagFlexbox.addView(tagView);

        }

        // 예시 사용자 데이터
        popularUserList.add(new User("김감성", "시를 사랑해요", R.drawable.sample_profile2));
        popularUserList.add(new User("북마스터", "매일 3권 독서", R.drawable.sample_profile2));
        popularUserList.add(new User("나무늘보", "천천히 읽어요", R.drawable.sample_profile2));
        popularUserList.add(new User("책벌레", "모든 책은 친구", R.drawable.sample_profile2));

        userAdapter = new BrowseUsersAdapter(popularUserList);
        usersRecyclerView.setAdapter(userAdapter);

        //검색 실행
        search.setOnClickListener(v -> {
            Intent intent = new Intent(BrowseActivity.this, SearchActivity.class);
            startActivity(intent);
        });


    }

    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Browser;
    }
}
