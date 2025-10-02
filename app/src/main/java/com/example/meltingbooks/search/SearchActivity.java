package com.example.meltingbooks.search;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.Hashtag;
import com.example.meltingbooks.R;
import com.example.meltingbooks.browse.BrowseUsersAdapter;
import com.example.meltingbooks.network.book.BookResponse;
import com.example.meltingbooks.network.browse.HashtagController;
import com.example.meltingbooks.network.browse.HashtagResponse;
import com.example.meltingbooks.network.browse.PopularUser;
import com.example.meltingbooks.network.browse.UserController;
import com.example.meltingbooks.network.feed.FeedPageResponse;
import com.example.meltingbooks.network.feed.FeedResponse;
import com.example.meltingbooks.network.group.GroupAllList;
import com.example.meltingbooks.network.group.GroupResponseAdapter;
import com.example.meltingbooks.group.profile.GroupProfileActivity;
import com.example.meltingbooks.group.profile.GroupProfileItem;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton searchIcon;


    // 탭 관련 RecyclerView
    private RecyclerView bookRecyclerView;
    private RecyclerView reviewRecyclerView;
    private RecyclerView popularUsersRecyclerView;
    private RecyclerView popularGroupsRecyclerView;
    private RecyclerView searchHashtagRecyclerView;

    private String currentTab = "book"; // 기본 탭

    // 🔹 책 관련 변수
    private SearchBookAdapter bookAdapter;
    private List<Book> filteredBookList;
    private View barBook;
    private BookController bookController;


    // 🔹 감상문 관련 변수
    private View barReview;

    // 🔹 리뷰 관련 변수
    private SearchReviewAdapter reviewAdapter;
    private List<FeedItem> reviewList = new ArrayList<>();

    // 선택한 책 bookId와 별점(전달용)
    private int selectedBookId = -1;
    private int selectedBookRating = 0;


    // 🔹 그룹 관련 변수
    private GroupResponseAdapter groupAdapter;
    private List<GroupResponse> fullGroupList;
    private List<GroupResponse> filteredGroupList;

    private View barGroup;

    // 🔹 사용자 관련 변수
    private BrowseUsersAdapter userAdapter;
    private List<PopularUser> fullUserList; //인기 유저 모델로 수정
    private List<PopularUser> filteredUserList; //인기 유저 모델로 수정
    private View barUser;

    //해시태그 관련 변수
    private List<Hashtag> fullHashtagList;
    private List<Hashtag> filteredHashtagList;
    private SearchHashtagAdapter hashtagAdapter;
    private View barHashtag;


    // 선택된 해시태그 관련 뷰
    private LinearLayout hashTagInfoSelected;
    private ImageView searchItemIcon;
    private TextView searchItemText;
    private RecyclerView reviewRecyclerView2;

    // 해시태그 리뷰 리스트 & 어댑터
    private List<FeedItem> reviewList2 = new ArrayList<>();
    private SearchHashtagReviewAdapter reviewAdapter2;

    private String token;
    private int userId;
    private ApiService apiService; // 클래스 멤버로 선언

    // 멤버 변수로 RadioGroup 추가
    private RadioGroup layoutBookOptions;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //토큰 받아오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
       token = prefs.getString("jwt", null);
       userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("SearchActivity", "토큰 또는 사용자 ID가 없습니다.");
        } else {
            // 서버에서 해시태그 불러오기
            loadAllHashtags(token);
        }


        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        searchInput = findViewById(R.id.searchInput);
        searchIcon = findViewById(R.id.searchIcon);

        // 기존 findViewById 코드 외에 추가
        layoutBookOptions = findViewById(R.id.layout_book_options);

        layoutBookOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (filteredBookList == null || filteredBookList.isEmpty()) return;

            if (checkedId == R.id.radio_book1) { // 평점순
                sortBooksByRating();
            } else if (checkedId == R.id.radio_book2) { // 출판일
                sortBooksByPubDate();
            } else if (checkedId == R.id.radio_book3) { // 인기순
                sortBooksByPopularity();
            }
        });



        //검색 카테고리 버튼 클릭 탭 추가
        findViewById(R.id.btnBook).setOnClickListener(v -> {
            currentTab = "book";
            performSearch();
            showRadioGroupForTab("book");
        });

        findViewById(R.id.btnReview).setOnClickListener(v -> {
            currentTab = "review";
            performSearch();
            showRadioGroupForTab("review");
        });

        findViewById(R.id.btnUser).setOnClickListener(v -> {
            currentTab = "user";
            performSearch();
            showRadioGroupForTab("user");
        });

        findViewById(R.id.btnGroup).setOnClickListener(v -> {
            currentTab = "group";
            performSearch();
            showRadioGroupForTab("group");
        });

        findViewById(R.id.btnHashtag).setOnClickListener(v -> {
            currentTab = "hashtag";
            performSearch();
            showRadioGroupForTab("hashtag");
        });


        // RecyclerView 초기화
        bookRecyclerView = findViewById(R.id.bookRecyclerView);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);
        popularUsersRecyclerView = findViewById(R.id.popularUsersRecyclerView);
        popularGroupsRecyclerView = findViewById(R.id.popularGroupsRecyclerView);
        searchHashtagRecyclerView = findViewById(R.id.searchHashtagRecyclerView);

        // Bar 초기화
        barBook = findViewById(R.id.barBook);
        barReview = findViewById(R.id.barReview);
        barUser = findViewById(R.id.barUser);
        barGroup = findViewById(R.id.barGroup);
        barHashtag = findViewById(R.id.barHashtag);


        // 🔹 책 RecyclerView 세팅
        bookController = new BookController(this); // context 전달
        filteredBookList = new ArrayList<>();
        bookAdapter = new SearchBookAdapter(this, filteredBookList);
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookRecyclerView.setAdapter(bookAdapter);


        // 책 클릭 리스너 -> 알라딘 링크로 이동
        bookAdapter.setOnItemClickListener(book -> {
            if (book.getLink() != null && !book.getLink().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getLink()));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "책 링크가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });


        // 🔹 감상문 RecyclerView 세팅
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);
        reviewAdapter = new SearchReviewAdapter(this, reviewList);
        reviewRecyclerView.setAdapter(reviewAdapter);
        reviewRecyclerView.setNestedScrollingEnabled(false);


        // 🔹 사용자 RecyclerView 세팅
        popularUsersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        filteredUserList = new ArrayList<>();
        //fullUserList = createDummyUsers(); ⭐ 삭제
        userAdapter = new BrowseUsersAdapter(filteredUserList);
        popularUsersRecyclerView.setAdapter(userAdapter);
        //filteredUserList.addAll(fullUserList); ⭐ 삭제


        // 🔹 그룹 RecyclerView 세팅
        popularGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        filteredGroupList = new ArrayList<>();
        fullGroupList = new ArrayList<>();

        groupAdapter = new GroupResponseAdapter(this, filteredGroupList, groupResponse -> {
            String imageUrl = groupResponse.getGroupImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) imageUrl = "";

            GroupProfileItem profileItem = new GroupProfileItem(
                    groupResponse.getName(),
                    imageUrl,
                    groupResponse.getCategory(),
                    "그룹 소개",          // IntroTitle 고정
                    groupResponse.getDescription()  // IntroDetail
            );

            Intent intent = new Intent(SearchActivity.this, GroupProfileActivity.class);
            intent.putExtra("groupId", groupResponse.getId());
            startActivity(intent);
        });

        popularGroupsRecyclerView.setAdapter(groupAdapter);


        // 선택된 해시태그 관련 뷰
        hashTagInfoSelected = findViewById(R.id.hashTagInfoSelected);
        searchItemIcon = findViewById(R.id.searchItemIcon);
        searchItemText = findViewById(R.id.searchItemText);
        reviewRecyclerView2 = findViewById(R.id.reviewRecyclerView2);

        fullHashtagList = new ArrayList<>();
        filteredHashtagList = new ArrayList<>();


        // 해시태그 검색시 리뷰용 리스트 & 어댑터 초기화
        LinearLayoutManager hashtagLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView2.setLayoutManager(hashtagLayoutManager);
        reviewAdapter2 = new SearchHashtagReviewAdapter(this, reviewList2);
        reviewRecyclerView2.setAdapter(reviewAdapter2);
        //reviewAdapter2 = new SearchHashtagReviewAdapter(this, new ArrayList<>());



        // 해시태그 RecyclerView 세팅
        filteredHashtagList = new ArrayList<>();
        hashtagAdapter = new SearchHashtagAdapter(filteredHashtagList);
        searchHashtagRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchHashtagRecyclerView.setAdapter(hashtagAdapter);

        apiService = ApiClient.getClient(token).create(ApiService.class);

        // 클릭 리스너 연결
        hashtagAdapter.setOnItemClickListener(hashtag -> {
            // 해시태그 목록은 숨기고
            searchHashtagRecyclerView.setVisibility(View.GONE);
            // 선택된 해시태그 영역은 보이게
            hashTagInfoSelected.setVisibility(View.VISIBLE);

            searchItemText.setText(hashtag.getTag());

            // 해시태그 리뷰 불러오기
            HashtagController hashtagController = new HashtagController(token);
            hashtagController.fetchReviewsByHashtag(hashtag.getTag(), new Callback<ApiResponse<FeedPageResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<FeedPageResponse>> call,
                                       Response<ApiResponse<FeedPageResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        FeedPageResponse pageResponse = response.body().getData();
                        List<FeedResponse> hashtagReviews = (pageResponse != null && pageResponse.getContent() != null)
                                ? pageResponse.getContent() : new ArrayList<>();

                        // Feed API 호출 → 유저 정보 포함
                        Call<ApiResponse<FeedPageResponse>> feedCall =
                                apiService.getUserFeeds("Bearer " + token, userId, 0, 50);

                        feedCall.enqueue(new Callback<ApiResponse<FeedPageResponse>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<FeedPageResponse>> call,
                                                   Response<ApiResponse<FeedPageResponse>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                    FeedPageResponse feedPage = response.body().getData();
                                    List<FeedResponse> feeds = feedPage.getContent();
                                    List<FeedItem> mappedFeeds = new ArrayList<>();

                                    // reviewId 기준으로 hashtagReviews와 feed를 매핑
                                    for (FeedResponse feed : feeds) {
                                        for (FeedResponse hashtagReview : hashtagReviews) {
                                            if (feed.getReviewId() == hashtagReview.getReviewId()) {
                                                String firstImage = (feed.getReviewImageUrls() != null && !feed.getReviewImageUrls().isEmpty())
                                                        ? feed.getReviewImageUrls().get(0)
                                                        : null;

                                                FeedItem feedItem = new FeedItem(
                                                        feed.getNickname(),
                                                        feed.getContent(),
                                                        feed.getFormattedCreatedAt(), // formatted 사용
                                                        firstImage,
                                                        feed.getUserProfileImage(),
                                                        feed.getBookId(),
                                                        feed.getCommentCount(),
                                                        feed.getLikeCount(),
                                                        feed.getTagId(),
                                                        feed.getHashtags(),
                                                        feed.getRating()
                                                );
                                                feedItem.setPostId(feed.getReviewId());
                                                feedItem.setPostType("feed");

                                                mappedFeeds.add(feedItem);
                                                break;
                                            }
                                        }
                                    }

                                    // 📌 어댑터 갱신
                                    reviewList2.clear();
                                    reviewList2.addAll(mappedFeeds);
                                    reviewAdapter2.notifyDataSetChanged();

                                    // ✅ 리뷰 RecyclerView 보여주기
                                    reviewRecyclerView2.setVisibility(mappedFeeds.isEmpty() ? View.GONE : View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                                Log.e("BrowseActivity", "Feed API 실패: " + t.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                    Log.e("BrowseActivity", "해시태그 리뷰 불러오기 실패: " + t.getMessage());
                }
            });

        });


        // 검색 아이콘 클릭 시 실행
        searchIcon.setOnClickListener(v -> performSearch());

        // 키보드에서 검색 버튼 눌렀을 때 실행
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }


    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        Log.d("SearchActivity", "Search query: '" + query + "'");

        switch (currentTab) {
            case "book":
                bookRecyclerView.setVisibility(View.VISIBLE);
                reviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.VISIBLE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.GONE);

                //해시태그 관련 뷰 숨기기
                hashTagInfoSelected.setVisibility(View.GONE);

                if (!query.isEmpty()) {
                    // 서버에서 검색
                    bookController.searchBooks(query, new Callback<BookResponse>() {
                        @Override
                        public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                            Log.d("SearchActivity", "서버 응답 성공: " + response.code());

                            if (response.isSuccessful() && response.body() != null) {
                                Log.d("SearchActivity", "받은 책 개수: " + response.body());
                                List<Book> books = response.body().getData(); // data 꺼내기
                                filteredBookList.clear();
                                filteredBookList.addAll(books);
                                bookAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("SearchActivity", "응답은 왔지만 body 없음");
                                filteredBookList.clear();
                                bookAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<BookResponse> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(SearchActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                break;

            case "review":
                bookRecyclerView.setVisibility(View.GONE);
                reviewRecyclerView.setVisibility(View.VISIBLE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.VISIBLE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.GONE);

                //해시태그 관련 뷰 숨기기
                hashTagInfoSelected.setVisibility(View.GONE);
                // 🔹 인기순 리뷰 불러오기
                fetchPopularReviews();
                break;


            //⭐case user 부분 전체 수정
            case "user":
                bookRecyclerView.setVisibility(View.GONE);
                reviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.VISIBLE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.VISIBLE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.GONE);

                filteredUserList.clear();


                //해시태그 관련 뷰 숨기기
                hashTagInfoSelected.setVisibility(View.GONE);


                UserController userController = new UserController(token);

                if (!TextUtils.isEmpty(query)) {
                    // 검색어가 있으면 서버에서 검색
                    userController.searchUsers(query, new Callback<List<PopularUser>>() {
                        @Override
                        public void onResponse(Call<List<PopularUser>> call, Response<List<PopularUser>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                filteredUserList.addAll(response.body());
                                userAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(SearchActivity.this, "검색 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<PopularUser>> call, Throwable t) {
                            Toast.makeText(SearchActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 검색어 없으면 인기 유저 가져오기
                    userController.fetchPopularUsers(new UserController.PopularUsersCallback() {
                        @Override
                        public void onSuccess(List<PopularUser> users) {
                            // 응답 로그 출력
                            Log.d("BrowseActivity", "Fetched popular users: " + users.size());
                            for (PopularUser user : users) {
                                Log.d("BrowseActivity",
                                        "User -> id: " + user.getId() +
                                                ", nickname: " + user.getNickname() +
                                                ", bio: " + user.getBio());
                            }

                            runOnUiThread(() -> {
                                // 어댑터 새로 안만들고 데이터 갱신
                                userAdapter.updateUsers(users);
                            });
                        }
                        @Override
                        public void onError(String errorMessage) {
                            Log.e("BrowseActivity", "Failed to load popular users: " + errorMessage);
                        }
                    });
                }
                break;


            case "group":
                bookRecyclerView.setVisibility(View.GONE);
                reviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.VISIBLE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.VISIBLE);
                barHashtag.setVisibility(View.GONE);


                //해시태그 관련 뷰 숨기기
                hashTagInfoSelected.setVisibility(View.GONE);


                fetchGroupsFromServer(query); // 서버 검색 호출
                break;


            case "hashtag":
                bookRecyclerView.setVisibility(View.GONE);
                reviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.VISIBLE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.VISIBLE);

                //해시태그 관련 뷰 숨기기
                hashTagInfoSelected.setVisibility(View.GONE);

                //재검색
                hashTagInfoSelected.setVisibility(View.GONE);

                filteredHashtagList.clear();
                if (TextUtils.isEmpty(query)) {
                    filteredHashtagList.addAll(fullHashtagList);
                } else {
                    for (Hashtag tag : fullHashtagList) {
                        if (tag.getTag().toLowerCase().contains(query.toLowerCase())) {
                            filteredHashtagList.add(tag);
                        }
                    }
                }

                if (filteredHashtagList.isEmpty()) {
                    Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }

                hashtagAdapter.notifyDataSetChanged();
                break;
        }
    }


// 인기순 리뷰 (책 제한 없음)
private void fetchPopularReviews() {
    Call<ApiResponse<FeedPageResponse>> feedCall =
            apiService.getUserFeeds("Bearer " + token, userId, 0, 10); // 인기순 파라미터 없으면 전체 가져오기

    feedCall.enqueue(new Callback<ApiResponse<FeedPageResponse>>() {
        @Override
        public void onResponse(Call<ApiResponse<FeedPageResponse>> call,
                               Response<ApiResponse<FeedPageResponse>> response) {
            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                FeedPageResponse pageResponse = response.body().getData();
                List<FeedResponse> feeds = pageResponse.getContent();

                if (feeds == null || feeds.isEmpty()) {
                    reviewRecyclerView.setVisibility(View.GONE);
                    return;
                }

                // 📌 likeCount 내림차순 정렬 (인기순)
                Collections.sort(feeds, (a, b) -> Integer.compare(b.getLikeCount(), a.getLikeCount()));

                List<FeedItem> mappedFeeds = new ArrayList<>();
                for (FeedResponse feed : feeds) {
                    String firstImage = (feed.getReviewImageUrls() != null && !feed.getReviewImageUrls().isEmpty())
                            ? feed.getReviewImageUrls().get(0)
                            : null;

                    FeedItem feedItem = new FeedItem(
                            feed.getNickname(),
                            feed.getContent(),
                            feed.getFormattedCreatedAt(),
                            firstImage,
                            feed.getUserProfileImage(),
                            feed.getBookId(),
                            feed.getCommentCount(),
                            feed.getLikeCount(),
                            feed.getTagId(),
                            feed.getHashtags(),
                            feed.getRating()
                    );
                    feedItem.setPostId(feed.getReviewId());
                    feedItem.setPostType("feed");

                    mappedFeeds.add(feedItem);
                }

                // 어댑터 갱신
                reviewList.clear();
                reviewList.addAll(mappedFeeds);
                reviewAdapter.notifyDataSetChanged();

                reviewRecyclerView.setVisibility(mappedFeeds.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                Log.e("Feed", "인기 리뷰 응답 비정상: " + response.message());
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
            Log.e("Feed", "인기 리뷰 불러오기 실패: " + t.getMessage());
        }
    });
}



    //그룹 전체 조회-> GroupAllList 사용
    private void fetchGroupsFromServer(String keyword) {
        GroupController groupController = new GroupController(this);

        groupController.searchGroups(keyword, null, new Callback<GroupAllList>() {
            @Override
            public void onResponse(Call<GroupAllList> call, Response<GroupAllList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullGroupList.clear();
                    filteredGroupList.clear();

                    List<GroupResponse> allGroups = response.body().getData();
                    fullGroupList.addAll(allGroups);
                    filteredGroupList.addAll(allGroups);
                    groupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<GroupAllList> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SearchActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllHashtags(String token) {
        HashtagController hashtagController = new HashtagController(token);

        hashtagController.fetchPopularHashtags(new Callback<ApiResponse<List<HashtagResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<HashtagResponse>>> call,
                                   Response<ApiResponse<List<HashtagResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<HashtagResponse> hashtags = response.body().getData();

                    fullHashtagList.clear();
                    for (HashtagResponse hr : hashtags) {
                        fullHashtagList.add(new Hashtag(hr.getTag()));
                    }

                    // 처음에는 전체 리스트 보여주기
                    filteredHashtagList.clear();
                    filteredHashtagList.addAll(fullHashtagList);
                    hashtagAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<HashtagResponse>>> call, Throwable t) {
                Log.e("SearchActivity", "해시태그 전체 불러오기 실패", t);
            }
        });
    }

    //카테고리 정렬 추가
    private void showRadioGroupForTab(String tab) {
        layoutBookOptions.setVisibility(tab.equals("book") ? View.VISIBLE : View.GONE);
    }

    //도서 평점순 정렬
    private void sortBooksByRating() {
        String query = searchInput.getText().toString().trim();
        if (TextUtils.isEmpty(query)) return;

        bookController.searchBooksWithSort(query, "rating", new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = response.body().getData();
                    filteredBookList.clear();
                    filteredBookList.addAll(books);
                    bookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SearchActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //도서 인기순 정렬
    private void sortBooksByPopularity() {
        String query = searchInput.getText().toString().trim();
        if (TextUtils.isEmpty(query)) return;

        bookController.searchBooksWithSort(query, "popular", new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = response.body().getData();
                    filteredBookList.clear();
                    filteredBookList.addAll(books);
                    bookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SearchActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //도서 출판일 순
    private void sortBooksByPubDate() {
        if (filteredBookList == null || filteredBookList.isEmpty()) return;

        Collections.sort(filteredBookList, (b1, b2) -> {
            // pubDate: "2025-04-10"
            return b2.getPubDate().compareTo(b1.getPubDate()); // 최신순
        });

        bookAdapter.notifyDataSetChanged();
    }


}
