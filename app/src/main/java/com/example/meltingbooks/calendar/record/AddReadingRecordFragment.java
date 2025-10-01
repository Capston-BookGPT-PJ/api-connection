package com.example.meltingbooks.calendar.record;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.BookListHelper.BookItem;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookApi;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.book.BookCreateRequest;
import com.example.meltingbooks.search.SearchBookAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddReadingRecordFragment extends Fragment {

    private LocalDate selectedDate = LocalDate.now();
    private View rootView;
    private View feedBookSearch; //책 검색 및 선택 레이아웃(숨김.보임)
    // 🔹 책 관련 변수 매핑
    private SearchBookAdapter bookAdapter;
    private List<Book> filteredBookList;
    private BookController bookController;
    private RecyclerView rvSearchResults; //책 검색
    private LinearLayout bookInfoSelected; //선택한 책 표시
    private ImageView bookCover; //책 이미지
    private TextView bookInfoTitle, bookInfoAuthor, bookInfoPublisher, bookInfoCategory; //제목 저자 출판사 카테고리


    // 선택한 책 bookId
    private int selectedBookId = -1;
    private boolean isBookSearchInitialized = false;

    public AddReadingRecordFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_reading_record, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // 초기 날짜 표시
        updateWeekDates();

        // 이전 주 버튼
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_week);
        btnPrev.setOnClickListener(v -> {
            selectedDate = selectedDate.minusWeeks(1);
            updateWeekDates();
        });

        // 다음 주 버튼
        ImageButton btnNext = view.findViewById(R.id.btn_next_week);
        btnNext.setOnClickListener(v -> {
            selectedDate = selectedDate.plusWeeks(1);
            updateWeekDates();
        });

        initializeBookSearchViews();

        CheckBox checkboxFinished = view.findViewById(R.id.checkboxFinished);
        boolean isFinished = checkboxFinished.isChecked(); // true면 완독, false면 미완독

    }

    private void updateWeekDates() {
        LinearLayout container = rootView.findViewById(R.id.week_date_container);
        container.removeAllViews();

        // 해당 주의 일요일 찾기
        LocalDate sunday = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() % 7);

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            TextView textView = new TextView(getContext());

            textView.setText(String.valueOf(date.getDayOfMonth()));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(24, 16, 24, 16);

            // 정사각형 크기로 설정해서 원으로 보이게
            int sizeInDp = 35;
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    sizeInDp,
                    getResources().getDisplayMetrics()
            );

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            textView.setLayoutParams(layoutParams);

            // 선택된 날짜면 회색 원 + 흰색 글씨
            if (date.equals(selectedDate)) {
                textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setTextColor(Color.BLACK);
            }

            // 날짜 클릭 시 선택 표시 갱신
            textView.setOnClickListener(v -> {
                selectedDate = date;
                updateWeekDates();  // 다시 렌더링
            });

            container.addView(textView);
        }
    }

    //책 검색과 선택 및 별점
    private void initializeBookSearchViews() {
        // 1. Fragment의 rootView에서 뷰 찾기
        feedBookSearch = rootView.findViewById(R.id.feedBookSearch);


        // 2. feedBookSearch가 null이 아닌지 확인합니다.
        if (feedBookSearch != null) {
            // 3. 이제 안전하게 하위 뷰들을 찾습니다.
            // 책 검색 관련 뷰 매핑
            LinearLayout bookSearchInfoContainer = feedBookSearch.findViewById(R.id.bookSearchInfoContainer);
            EditText etBookTitle = feedBookSearch.findViewById(R.id.etBookTitle);
            ImageView searchBook = feedBookSearch.findViewById(R.id.searchBook);
            rvSearchResults = feedBookSearch.findViewById(R.id.rvSearchResults);
            bookInfoSelected = feedBookSearch.findViewById(R.id.bookInfoSelected);

            // bookInfoSelected도 null 체크를 하는 것이 안전합니다.
            if (bookInfoSelected != null) {
                bookInfoSelected.setVisibility(View.GONE);

                bookInfoTitle = bookInfoSelected.findViewById(R.id.bookInfoTitle);
                bookInfoAuthor = bookInfoSelected.findViewById(R.id.bookInfoAuthor);
                bookInfoPublisher = bookInfoSelected.findViewById(R.id.bookInfoPublisher);
                bookCover = bookInfoSelected.findViewById(R.id.bookCover);
                bookInfoCategory = bookInfoSelected.findViewById(R.id.bookInfoCategory);
            }

            // RecyclerView 및 어댑터 초기화 로직
            bookController = new BookController(requireContext());
            filteredBookList = new ArrayList<>();
            bookAdapter = new SearchBookAdapter(requireContext(), filteredBookList);
            rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext())); // 수정
            rvSearchResults.setAdapter(bookAdapter);


            // 책 검색 버튼 클릭 리스너
            searchBook.setOnClickListener(v -> {
                if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.GONE);
                String query = etBookTitle.getText().toString().trim();
                rvSearchResults.setVisibility(View.VISIBLE);

                if (!query.isEmpty()) {
                    bookController.searchBooks(query, new Callback<List<Book>>() {
                        @Override
                        public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                            filteredBookList.clear();
                            if (response.isSuccessful() && response.body() != null) {
                                filteredBookList.addAll(response.body());
                            }
                            bookAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<List<Book>> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            // RecyclerView 아이템 클릭
            bookAdapter.setOnItemClickListener(book -> {
                bookInfoTitle.setText(book.getTitle());
                bookInfoAuthor.setText(book.getAuthor());
                bookInfoPublisher.setText(book.getPublisher());
                bookInfoCategory.setText(book.getCategoryName());
                Glide.with(this).load(book.getCover()).into(bookCover);

                rvSearchResults.setVisibility(View.GONE);
                bookInfoSelected.setVisibility(View.VISIBLE);

                // 2️⃣ 서버에 Book 생성 요청
                createBookOnServer(book);
            });
        }
    }
    // 책 생성
    private void createBookOnServer(Book searchResult) {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        BookCreateRequest request = new BookCreateRequest(
                searchResult.getTitle(),
                searchResult.getAuthor(),
                searchResult.getPublisher(),
                searchResult.getPubDate(),
                searchResult.getIsbn(),
                searchResult.getIsbn13(),
                searchResult.getCover(),
                searchResult.getLink(),
                searchResult.getCategoryName(),
                searchResult.getItemPage()
        );


        //Book 객체에 저장(bookId 저장->selectedBookId)
        // BookApi 사용
        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        bookApi.createBook("Bearer " + token, request).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedBookId = response.body().getBookId();
                } else {
                    Toast.makeText(requireContext(), "책 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}