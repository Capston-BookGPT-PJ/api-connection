package com.example.meltingbooks.search;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.meltingbooks.Book;
import com.example.meltingbooks.Hashtag;
import com.example.meltingbooks.R;
import com.example.meltingbooks.Review;
import com.example.meltingbooks.User;
import com.example.meltingbooks.browse.BrowseUsersAdapter;
import com.example.meltingbooks.group.home.GroupCardItem;
import com.example.meltingbooks.group.home.GroupCardItemAdapter;
import com.example.meltingbooks.group.profile.GroupProfileActivity;
import com.example.meltingbooks.group.profile.GroupProfileItem;

import java.util.ArrayList;
import java.util.List;

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
    private List<Book> fullBookList;
    private List<Book> filteredBookList;
    private View barBook;

    // 감상문 관련 변수
    private List<Review> fullReviewList;
    private List<Review> filteredReviewList;
    private SearchReviewAdapter reviewAdapter;
    private View barReview;

    // 🔹 그룹 관련 변수
    private GroupCardItemAdapter groupAdapter;
    private List<GroupCardItem> fullGroupList;
    private List<GroupCardItem> filteredGroupList;
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

        searchInput = findViewById(R.id.searchInput);
        searchIcon = findViewById(R.id.searchIcon);

        //검색 카테고리 버튼 클릭 탭
        findViewById(R.id.btnBook).setOnClickListener(v -> {
            currentTab = "book";
            performSearch();
        });

        findViewById(R.id.btnReview).setOnClickListener(v -> {
            currentTab = "review";
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
        searchReviewRecyclerView = findViewById(R.id.searchReviewRecyclerView);
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
        bookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filteredBookList = new ArrayList<>();
        fullBookList = createDummyBooks(); // 더미 책 데이터
        bookAdapter = new SearchBookAdapter(this, filteredBookList);
        bookRecyclerView.setAdapter(bookAdapter);

        // 초기 목록 보여주기
        filteredBookList.addAll(fullBookList);
        bookAdapter.notifyDataSetChanged();

        // 감상문  RecyclerView 세팅
        filteredReviewList = new ArrayList<>();
        fullReviewList = createDummyReviews();
        reviewAdapter = new SearchReviewAdapter(filteredReviewList);
        searchReviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchReviewRecyclerView.setAdapter(reviewAdapter);
        filteredReviewList.addAll(fullReviewList);
        reviewAdapter.notifyDataSetChanged();

        // 🔹 사용자 RecyclerView 세팅
        popularUsersRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        filteredUserList = new ArrayList<>();
        fullUserList = createDummyUsers();
        userAdapter = new BrowseUsersAdapter(filteredUserList);
        popularUsersRecyclerView.setAdapter(userAdapter);
        filteredUserList.addAll(fullUserList);

        // 🔹 그룹 RecyclerView 세팅
        popularGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        filteredGroupList = new ArrayList<>();
        fullGroupList = createDummyGroups(); // 그룹 더미 데이터 생성

        groupAdapter = new GroupCardItemAdapter(this, filteredGroupList, item -> {
            String imageUrl = item.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = "";
            }

            GroupProfileItem profileItem = new GroupProfileItem(
                    item.getName(),
                    imageUrl,
                    item.getCategory(),
                    item.getIntroTitle(),
                    item.getIntroDetail()
            );

            Intent intent = new Intent(SearchActivity.this, GroupProfileActivity.class);
            intent.putExtra("groupProfile", profileItem);
            startActivity(intent);
        });

        popularGroupsRecyclerView.setAdapter(groupAdapter);


        // 초기 그룹 목록 보여주기
        filteredGroupList.addAll(fullGroupList);
        groupAdapter.notifyDataSetChanged();

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
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
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

                filteredBookList.clear();
                if (TextUtils.isEmpty(query)) {
                    filteredBookList.addAll(fullBookList);
                } else {
                    for (Book book : fullBookList) {
                        if (book.getTitle().toLowerCase().contains(query.toLowerCase())) {
                            filteredBookList.add(book);
                        }
                    }
                }

                if (filteredBookList.isEmpty()) {
                    Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                bookAdapter.notifyDataSetChanged();
                break;


            case "review":
                bookRecyclerView.setVisibility(View.GONE);
                searchReviewRecyclerView.setVisibility(View.VISIBLE);
                popularUsersRecyclerView.setVisibility(View.GONE);
                popularGroupsRecyclerView.setVisibility(View.GONE);
                searchHashtagRecyclerView.setVisibility(View.GONE);
                barBook.setVisibility(View.GONE);
                barReview.setVisibility(View.VISIBLE);
                barUser.setVisibility(View.GONE);
                barGroup.setVisibility(View.GONE);
                barHashtag.setVisibility(View.GONE);

                filteredReviewList.clear();
                if (TextUtils.isEmpty(query)) {
                    filteredReviewList.addAll(fullReviewList);
                } else {
                    for (Review r : fullReviewList) {
                        if (r.getTitle().toLowerCase().contains(query.toLowerCase())) {
                            filteredReviewList.add(r);
                        }
                    }
                }

                if (filteredReviewList.isEmpty()) {
                    Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }

                reviewAdapter.notifyDataSetChanged();
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


                // 🔹 그룹 검색 필터링
                filteredGroupList.clear();
                if (TextUtils.isEmpty(query)) {
                    filteredGroupList.addAll(fullGroupList);
                } else {
                    for (GroupCardItem item : fullGroupList) {
                        if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredGroupList.add(item);
                        }
                    }
                }

                if (filteredGroupList.isEmpty()) {
                    Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }

                groupAdapter.notifyDataSetChanged();
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

    // 🔹 책 검색용 더미 데이터 생성
    private List<Book> createDummyBooks() {
        List<Book> list = new ArrayList<>();
        list.add(new Book("데미안", "https://example.com/demian.jpg", "헤르만 헤세", "민음사"));
        list.add(new Book("1984", "https://example.com/1984.jpg", "조지 오웰", "문학동네"));
        list.add(new Book("어린왕자", "https://example.com/prince.jpg", "생텍쥐페리", "열린책들"));
        return list;
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

    // 🔹 그룹 더미 데이터 생성
    private List<GroupCardItem> createDummyGroups() {
        List<GroupCardItem> list = new ArrayList<>();

        GroupCardItem item1 = new GroupCardItem("독서모임1", "책과 이야기하는 모임", R.drawable.sample_profile2);
        item1.setCategory("문학 토론 그룹");
        item1.setIntroTitle("그룹 소개");
        item1.setIntroDetail("📌 이 그룹은 다양한 문학 작품을 함께 읽고 토론하는 모임입니다.\n모두 환영합니다!");
        list.add(item1);

        GroupCardItem item2 = new GroupCardItem("영화책모임", "영화와 책을 함께 즐겨요", R.drawable.sample_profile2);
        item2.setCategory("문화 교류 그룹");
        item2.setIntroTitle("그룹 소개");
        item2.setIntroDetail("🎬 영화와 책을 연결하여 깊이 있는 대화를 나누는 그룹입니다.");
        list.add(item2);

        GroupCardItem item3 = new GroupCardItem("소설사랑", "소설 읽기 좋아하는 모임", R.drawable.sample_profile2);
        item3.setCategory("소설 팬클럽");
        item3.setIntroTitle("그룹 소개");
        item3.setIntroDetail("📖 소설을 사랑하는 사람들이 모여 이야기를 나누는 모임입니다.");
        list.add(item3);

        GroupCardItem item4 = new GroupCardItem("시집모임", "시를 함께 나누는 모임", R.drawable.sample_profile2);
        item4.setCategory("시와 문학");
        item4.setIntroTitle("그룹 소개");
        item4.setIntroDetail("🌸 시집을 읽고 감상을 나누는 따뜻한 모임입니다.");
        list.add(item4);

        return list;
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
