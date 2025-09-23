package com.example.meltingbooks.search;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.Hashtag;
import com.example.meltingbooks.R;
import com.example.meltingbooks.Review;
import com.example.meltingbooks.User;
import com.example.meltingbooks.browse.BrowseUsersAdapter;
import com.example.meltingbooks.network.group.GroupAllList;
import com.example.meltingbooks.network.group.GroupResponseAdapter;
import com.example.meltingbooks.group.profile.GroupProfileActivity;
import com.example.meltingbooks.group.profile.GroupProfileItem;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupSingleList;
import com.example.meltingbooks.network.group.GroupResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton searchIcon;


    // 탭 관련 RecyclerView
    private RecyclerView bookRecyclerView;
    private RecyclerView searchReviewRecyclerView;
    private RecyclerView popularUsersRecyclerView;
    private RecyclerView popularGroupsRecyclerView;
    private RecyclerView searchHashtagRecyclerView;

    private String currentTab = "book"; // 기본 탭

    // 🔹 책 관련 변수
    private SearchBookAdapter bookAdapter;
    private List<Book> filteredBookList;
    private View barBook;
    private BookController bookController;

    // 감상문 관련 변수
    private List<Review> fullReviewList;
    private List<Review> filteredReviewList;
    private SearchReviewAdapter reviewAdapter;
    private View barReview;

    // 🔹 그룹 관련 변수
    private GroupResponseAdapter groupAdapter;
    private List<GroupResponse> fullGroupList;
    private List<GroupResponse> filteredGroupList;

    private View barGroup;

    // 🔹 사용자 관련 변수
    private BrowseUsersAdapter userAdapter;
    private List<User> fullUserList;
    private List<User> filteredUserList;
    private View barUser;

    //해시태그 관련 변수
    private List<Hashtag> fullHashtagList;
    private List<Hashtag> filteredHashtagList;
    private SearchHashtagAdapter hashtagAdapter;
    private View barHashtag;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //토큰 받아오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", "");


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

        //검색 카테고리 버튼 클릭 탭
        findViewById(R.id.btnBook).setOnClickListener(v -> {
            currentTab = "book";
            performSearch();
        });

        findViewById(R.id.btnUser).setOnClickListener(v -> {
            currentTab = "user";
            performSearch();
        });

        findViewById(R.id.btnGroup).setOnClickListener(v -> {
            currentTab = "group";
            performSearch();
        });

        findViewById(R.id.btnHashtag).setOnClickListener(v -> {
            currentTab = "hashtag";
            performSearch();
        });

        // RecyclerView 초기화
        bookRecyclerView = findViewById(R.id.bookRecyclerView);
        popularUsersRecyclerView = findViewById(R.id.popularUsersRecyclerView);
        popularGroupsRecyclerView = findViewById(R.id.popularGroupsRecyclerView);
        searchHashtagRecyclerView = findViewById(R.id.searchHashtagRecyclerView);

        // Bar 초기화
        barBook = findViewById(R.id.barBook);
        barUser = findViewById(R.id.barUser);
        barGroup = findViewById(R.id.barGroup);
        barHashtag = findViewById(R.id.barHashtag);


        // 🔹 책 RecyclerView 세팅
        bookController = new BookController(this); // context 전달
        filteredBookList = new ArrayList<>();
        bookAdapter = new SearchBookAdapter(this, filteredBookList);

        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookRecyclerView.setAdapter(bookAdapter);


        // 서버에서 초기 책 목록 가져오기
        bookController.fetchBooks(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filteredBookList.clear();
                    filteredBookList.addAll(response.body());
                    bookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SearchActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 사용자 RecyclerView 세팅
        popularUsersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        filteredUserList = new ArrayList<>();
        fullUserList = createDummyUsers();
        userAdapter = new BrowseUsersAdapter(filteredUserList);
        popularUsersRecyclerView.setAdapter(userAdapter);
        filteredUserList.addAll(fullUserList);


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


        // 해시태그 RecyclerView 세팅
        searchHashtagRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filteredHashtagList = new ArrayList<>();
        fullHashtagList = createDummyHashtags(); // 더미 데이터 생성
        hashtagAdapter = new SearchHashtagAdapter(filteredHashtagList);
        searchHashtagRecyclerView.setAdapter(hashtagAdapter);
        filteredHashtagList.addAll(fullHashtagList);
        hashtagAdapter.notifyDataSetChanged();

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


        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        Log.d("SearchActivity", "Search query: '" + query + "'");

        switch (currentTab) {
            case "book":
                bookRecyclerView.setVisibility(View.VISIBLE);
                searchReviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.VISIBLE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.GONE);

                if (!query.isEmpty()) {
                    // 서버에서 검색
                    bookController.searchBooks(query, new Callback<List<Book>>() {
                        @Override
                        public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                            Log.d("SearchActivity", "서버 응답 성공: " + response.code());

                            if (response.isSuccessful() && response.body() != null) {
                                Log.d("SearchActivity", "받은 책 개수: " + response.body().size());
                                filteredBookList.clear();
                                filteredBookList.addAll(response.body());
                                bookAdapter.notifyDataSetChanged();
                            } else {
                                Log.d("SearchActivity", "응답은 왔지만 body 없음");
                                filteredBookList.clear();
                                bookAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Book>> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(SearchActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 검색어가 비었으면 초기 전체 목록 다시 표시
                    bookController.fetchBooks(new Callback<List<Book>>() {
                        @Override
                        public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                filteredBookList.clear();
                                filteredBookList.addAll(response.body());
                                bookAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Book>> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }

                break;

            case "user":
                bookRecyclerView.setVisibility(View.GONE);
                searchReviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.VISIBLE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.VISIBLE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.GONE);

                // 🔹 유저 검색 필터링
                filteredUserList.clear();
                if (TextUtils.isEmpty(query)) {
                    filteredUserList.addAll(fullUserList);
                } else {
                    for (User user : fullUserList) {
                        if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredUserList.add(user);
                        }
                    }
                }
                if (filteredUserList.isEmpty()) {
                    Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                userAdapter.notifyDataSetChanged();
                break;


            case "group":
                bookRecyclerView.setVisibility(View.GONE);
                searchReviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.VISIBLE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.VISIBLE);
                barHashtag.setVisibility(View.GONE);


                fetchGroupsFromServer(query); // 서버 검색 호출
                break;


            case "hashtag":
                bookRecyclerView.setVisibility(View.GONE);
                searchReviewRecyclerView.setVisibility(View.GONE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.VISIBLE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.GONE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.VISIBLE);

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

    // 감상문 검색용 더미 데이터 생성
    private List<Review> createDummyReviews() {
        List<Review> list = new ArrayList<>();
        list.add(new Review("데미안 감상문"));
        list.add(new Review("1984 리뷰"));
        list.add(new Review("어린왕자 독후감"));
        list.add(new Review("소설사랑 후기"));
        return list;
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




    // 🔹 사용자 더미 데이터
    private List<User> createDummyUsers() {
        List<User> list = new ArrayList<>();
        list.add(new User("Alice", "책 덕후입니다 📚", R.drawable.sample_profile2));
        list.add(new User("Bob", "영화와 책을 좋아해요 🎬", R.drawable.sample_profile2));
        list.add(new User("Charlie", "소설을 사랑하는 사람 ✨", R.drawable.sample_profile2));
        return list;
    }

    // 해시태그 더미 데이터
    private List<Hashtag> createDummyHashtags() {
        List<Hashtag> list = new ArrayList<>();
        list.add(new Hashtag("#독서"));
        list.add(new Hashtag("#영화책"));
        list.add(new Hashtag("#소설"));
        list.add(new Hashtag("#감상문"));
        list.add(new Hashtag("#추천도서"));
        return list;
    }
}
