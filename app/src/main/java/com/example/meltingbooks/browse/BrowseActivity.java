package com.example.meltingbooks.browse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.Browse.HashtagController;
import com.example.meltingbooks.network.Browse.HashtagResponse;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.feed.FeedPageResponse;
import com.example.meltingbooks.network.feed.FeedResponse;

import com.example.meltingbooks.search.SearchActivity;
import com.example.meltingbooks.User;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class BrowseActivity extends BaseActivity {
    private ImageButton search;

    private ViewPager2 bookViewPager;
    private RecyclerView reviewRecyclerView;
    private BrowseBookAdapter bookAdapter;
    private BrowseReviewAdapter reviewAdapter;
    private HashtagReviewsAdapter hashtagAdapter;
    private List<Book> bookList = new ArrayList<>();

    private View hashtagLayout;
    private View usersLayout;
    private FlexboxLayout hashtagFlexbox;
    private RecyclerView hashtagRecyclerView;

    private RecyclerView usersRecyclerView;
    private BrowseUsersAdapter userAdapter;
    private List<User> popularUserList = new ArrayList<>();

    //  변경 후
    private HashMap<Integer, List<FeedResponse>> reviewMapByBookId = new HashMap<>();


    // BrowseActivity 멤버 변수
    // 현재 선택된 태그 저장
    private TextView selectedTagView = null;


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

        // 인기 책
        bookViewPager = findViewById(R.id.bookViewPager);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);

        // 인기 해시태그
        hashtagLayout = findViewById(R.id.hashtagLayout);
        hashtagFlexbox = hashtagLayout.findViewById(R.id.hashtagFlexbox);
        hashtagRecyclerView = findViewById(R.id.hashtagRecyclerView);

        // 인기 사용자
        usersLayout = findViewById(R.id.usersLayout);
        usersRecyclerView = findViewById(R.id.popularUsersRecyclerView);
        usersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 인기 책 ViewPager
        bookAdapter = new BrowseBookAdapter(this, bookList);
        bookViewPager.setAdapter(bookAdapter);


        /// 인기 책 리뷰 RecyclerView
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);
        reviewAdapter = new BrowseReviewAdapter(new ArrayList<>());
        reviewRecyclerView.setAdapter(reviewAdapter);
        reviewRecyclerView.setNestedScrollingEnabled(false);

        // 해시태그 리뷰 RecyclerView
        LinearLayoutManager hashtagLayoutManager = new LinearLayoutManager(this);
        hashtagRecyclerView.setLayoutManager(hashtagLayoutManager);
        hashtagAdapter = new HashtagReviewsAdapter(new ArrayList<>());
        hashtagRecyclerView.setAdapter(hashtagAdapter);
        // hashtagRecyclerView.setNestedScrollingEnabled(false);


        // SharedPreferences에서 토큰과 사용자 ID 가져오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("BrowseActivity", "토큰 또는 사용자 ID가 없습니다.");
        } else {
            // 서버에서 해시태그 불러오기
            setupHashtags(token);
        }

        //인기 책 목록 가져오기
        loadPopularBooks();
        //책 리뷰 갱신
        setupPageChangeListener();

        // 탭 클릭 리스너
        TextView popularBooks = findViewById(R.id.popularBooks);
        TextView popularTags = findViewById(R.id.popularTags);
        TextView popularUsers = findViewById(R.id.popularUsers);

        popularBooks.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.VISIBLE);
            reviewRecyclerView.setVisibility(View.VISIBLE);
            hashtagLayout.setVisibility(View.GONE);
            hashtagRecyclerView.setVisibility(View.GONE);
            usersLayout.setVisibility(View.GONE);
        });

        popularTags.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.GONE);
            reviewRecyclerView.setVisibility(View.GONE);
            hashtagLayout.setVisibility(View.VISIBLE);
            hashtagRecyclerView.setVisibility(View.VISIBLE);
            usersLayout.setVisibility(View.GONE);
        });

        popularUsers.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.GONE);
            reviewRecyclerView.setVisibility(View.GONE);
            hashtagLayout.setVisibility(View.GONE);
            hashtagRecyclerView.setVisibility(View.GONE);
            usersLayout.setVisibility(View.VISIBLE);
        });
    }

    private void loadPopularBooks() {
        BookController bookController = new BookController(this);

        // 1. 인기 책 불러오기
        bookController.fetchPopularBooks(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bookList.clear();
                    bookList.addAll(response.body());
                    bookAdapter.notifyDataSetChanged();

                    // 책별 리뷰 가져오기
                    for (Book book : bookList) {
                        bookController.fetchReviewsByBook(book.getBookId(), new Callback<ApiResponse<List<FeedResponse>>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<List<FeedResponse>>> call, Response<ApiResponse<List<FeedResponse>>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    List<FeedResponse> reviews = response.body().getData();  // 여기서 data 추출
                                    reviewMapByBookId.put(book.getBookId(), reviews);

                                    // 첫 페이지면 바로 리뷰 갱신
                                    if (bookViewPager.getCurrentItem() == bookList.indexOf(book)) {
                                        reviewAdapter.updateReviews(reviews);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<List<FeedResponse>>> call, Throwable t) {
                                Log.e("BrowseActivity", "책별 리뷰 불러오기 실패", t);
                            }
                        });
                    }

                    /*FeedPageResponse 버전
                    // 2. 각 책별 리뷰 불러오기
                    for (Book book : bookList) {
                        bookController.fetchReviewsByBook(book.getBookId(),
                                new Callback<ApiResponse<FeedPageResponse>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<FeedPageResponse>> call,
                                                           Response<ApiResponse<FeedPageResponse>> response) {
                                        if (response.isSuccessful() && response.body() != null
                                                && response.body().isSuccess() && response.body().getData() != null) {

                                            FeedPageResponse pageResponse = response.body().getData();
                                            List<FeedResponse> reviews = pageResponse.getContent(); // FeedPageResponse에서 content 추출
                                            reviewMapByBookId.put(book.getBookId(), reviews);

                                            // 첫 페이지면 바로 리뷰 갱신
                                            if (bookViewPager.getCurrentItem() == bookList.indexOf(book)) {
                                                reviewAdapter.updateReviews(reviews);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                                        Log.e("BrowseActivity", "책별 리뷰 불러오기 실패", t);
                                    }
                                });
                    }*/

                } else {
                    Log.e("BrowseActivity", "인기 책 불러오기 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.e("BrowseActivity", "인기 책 불러오기 실패", t);
            }
        });
    }


    private void setupPageChangeListener() {
        bookViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Book book = bookList.get(position);
                List<FeedResponse> reviews = reviewMapByBookId.get(book.getBookId());
                reviewAdapter.updateReviews(reviews != null ? reviews : new ArrayList<>());
            }
        });
    }

    private void setupHashtags(String token) {
        HashtagController hashtagController = new HashtagController(token);

        hashtagController.fetchPopularHashtags(new Callback<ApiResponse<List<HashtagResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<HashtagResponse>>> call, Response<ApiResponse<List<HashtagResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<HashtagResponse> hashtags = response.body().getData();
                    hashtagFlexbox.removeAllViews();

                    for (HashtagResponse hashtag : hashtags) {
                        String tag = hashtag.getTag();

                        TextView tagView = new TextView(BrowseActivity.this);
                        tagView.setText(tag);
                        tagView.setTextSize(15);
                        tagView.setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.text_blue));
                        tagView.setGravity(Gravity.CENTER);
                        tagView.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag1));
                        tagView.setPadding(8, 8, 8, 8);

                        // **4줄 제한**( 인기 해시태그 20개 정도)
                        tagView.setMaxLines(4);
                        tagView.setEllipsize(TextUtils.TruncateAt.END);

                        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(12, 20, 12, 20);
                        tagView.setLayoutParams(params);

                        tagView.setOnClickListener(v -> {
                            boolean isSelected = v.isSelected();

                            if (selectedTagView != null && selectedTagView != v) {
                                selectedTagView.setSelected(false);
                                selectedTagView.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag1));
                                selectedTagView.setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.text_blue));
                            }

                            if (!isSelected) {
                                v.setSelected(true);
                                v.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag2));
                                ((TextView) v).setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.white));
                                selectedTagView = (TextView) v;

                                Log.d("HashtagClick", "Clicked hashtag: " + tag);

                                hashtagController.fetchReviewsByHashtag(tag, new Callback<ApiResponse<FeedPageResponse>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<FeedPageResponse>> call, Response<ApiResponse<FeedPageResponse>> response) {
                                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                            FeedPageResponse pageResponse = response.body().getData();
                                            List<FeedResponse> reviews = (pageResponse != null && pageResponse.getContent() != null)
                                                    ? pageResponse.getContent() : new ArrayList<>();

                                            Log.d("HashtagClick", "Fetched reviews count: " + reviews.size());

                                            hashtagAdapter.updateReviews(reviews);
                                            if (!reviews.isEmpty()) {
                                                hashtagRecyclerView.setVisibility(View.VISIBLE);
                                            } else {
                                                hashtagRecyclerView.setVisibility(View.GONE);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                                        Log.e("BrowseActivity", "해시태그 리뷰 불러오기 실패", t);
                                    }
                                });

                            } else {
                                v.setSelected(false);
                                v.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag1));
                                ((TextView) v).setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.text_blue));
                                selectedTagView = null;

                                hashtagRecyclerView.setVisibility(View.GONE);

                                hashtagAdapter.updateReviews(new ArrayList<>());
                            }
                        });

                        hashtagFlexbox.addView(tagView);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<HashtagResponse>>> call, Throwable t) {
                Log.e("Hashtag", "불러오기 실패", t);
            }
        });


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
