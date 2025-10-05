package com.example.meltingbooks.calendar.record;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookApi;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.book.BookCreateRequest;
import com.example.meltingbooks.network.log.LogApi;
import com.example.meltingbooks.network.log.LogController;
import com.example.meltingbooks.network.log.ReadingLogRequest;
import com.example.meltingbooks.network.log.ReadingLogResponse;
import com.example.meltingbooks.search.SearchBookAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
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
    private ImageView bookCover, searchBook; //책 이미지
    private TextView bookInfoTitle, bookInfoAuthor, bookInfoPublisher, bookInfoCategory; //제목 저자 출판사 카테고리

    private EditText editPage, editHours, editMinutes, etBookTitle;
    private TextView btnSave, btnDelete;

    // 선택한 책 bookId
    private int selectedBookId = -1;
    private boolean isBookSearchInitialized = false;
    private int currentLogId = -1; // 수정/삭제 시 필요


    private String token;
    private int userId;

    private LogController logController;

    public AddReadingRecordFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_reading_record, container, false);

        editPage = rootView.findViewById(R.id.edit_page);
        editMinutes = rootView.findViewById(R.id.edit_minute);
        editHours= rootView.findViewById(R.id.edit_hour);
        btnSave = rootView.findViewById(R.id.btn_save_log);
        btnDelete = rootView.findViewById(R.id.btn_delete_log);

        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        LogApi logApi = ApiClient.getClient(token).create(LogApi.class);
        logController = new LogController(logApi);


        // 저장 버튼
        btnSave.setOnClickListener(v -> saveOrUpdateLog());

        // 삭제 버튼
        btnDelete.setOnClickListener(v -> deleteLog());

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
            clearInputs();
            loadLogForSelectedDate(); // 이전 주 첫 진입 시 자동 로드
        });

        // 다음 주 버튼
        ImageButton btnNext = view.findViewById(R.id.btn_next_week);
        btnNext.setOnClickListener(v -> {
            selectedDate = selectedDate.plusWeeks(1);
            updateWeekDates();
            clearInputs();
            loadLogForSelectedDate(); // 다음 주 첫 진입 시 자동 로드
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
                clearInputs();
                loadLogForSelectedDate(); // 선택한 날짜 기록 불러오기
            });

            container.addView(textView);
        }

        //주차 UI 갱신 후 현재 선택 날짜 기록도 자동 불러오기
        loadLogForSelectedDate();
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
            //EditText etBookTitle = feedBookSearch.findViewById(R.id.etBookTitle);
            //ImageView searchBook = feedBookSearch.findViewById(R.id.searchBook);
            //rvSearchResults = feedBookSearch.findViewById(R.id.rvSearchResults);
            etBookTitle = feedBookSearch.findViewById(R.id.etBookTitle);
            searchBook = feedBookSearch.findViewById(R.id.searchBook);
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
                    bookController.searchBooks(query, new Callback<ApiResponse<List<Book>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<List<Book>>> call, Response<ApiResponse<List<Book>>> response) {
                            filteredBookList.clear();
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                filteredBookList.addAll(response.body().getData());
                            } else {
                                Log.e("BookSearch", "검색 실패: " + response.code() + " / " + response.message());
                            }
                            bookAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) {
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

                // 서버에 Book 생성 요청
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

    /** 특정 날짜의 기록 불러오기 */
    private void loadLogForSelectedDate() {
        if (token == null || userId == -1) return;

        String from = selectedDate.toString();
        String to = selectedDate.toString();

        logController.getLogsByPeriod(token, userId, from, to,
                new Callback<ApiResponse<List<ReadingLogResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReadingLogResponse>>> call,
                                           Response<ApiResponse<List<ReadingLogResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReadingLogResponse> logs = response.body().getData();
                            if (logs != null && !logs.isEmpty()) {
                                ReadingLogResponse log = logs.get(0);
                                currentLogId = log.getId();
                                fillInputsWithLog(log);
                            } else {
                                Toast.makeText(requireContext(),
                                        "해당 날짜 기록이 없습니다. 새로 입력하세요.", Toast.LENGTH_SHORT).show();
                                clearInputs();
                                currentLogId = -1;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReadingLogResponse>>> call, Throwable t) {
                        Toast.makeText(requireContext(), "서버 오류", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** 입력 값 → 서버 저장 or 수정 */
    private void saveOrUpdateLog() {
        if (selectedBookId == -1) {
            Toast.makeText(requireContext(), "책을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int pages = 0;
        int hours = 0;
        int minutes = 0;

        try {
            String pageStr = editPage.getText().toString();
            String hourStr = editHours.getText().toString();
            String minuteStr = editMinutes.getText().toString();

            pages = pageStr.isEmpty() ? 0 : Integer.parseInt(pageStr);
            hours = hourStr.isEmpty() ? 0 : Integer.parseInt(hourStr);
            minutes = minuteStr.isEmpty() ? 0 : Integer.parseInt(minuteStr);

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "숫자를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalMinutes = hours * 60 + minutes; // API용 분 환산

        String readAt = selectedDate.atStartOfDay().toString();

        ReadingLogRequest request = new ReadingLogRequest(pages, totalMinutes, readAt);

        if (currentLogId == -1) {
            // 새로 생성
            logController.createLog(token, userId, selectedBookId, request,
                    new Callback<ApiResponse<ReadingLogResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<ReadingLogResponse>> call,
                                               Response<ApiResponse<ReadingLogResponse>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "기록 저장 완료", Toast.LENGTH_SHORT).show();
                                loadLogForSelectedDate();
                            } else {
                                Toast.makeText(requireContext(), "기록 저장 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<ReadingLogResponse>> call, Throwable t) {
                            Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 수정
            logController.updateLog(token, userId, currentLogId, request,
                    new Callback<ApiResponse<ReadingLogResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<ReadingLogResponse>> call,
                                               Response<ApiResponse<ReadingLogResponse>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "기록 수정 완료", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "기록 수정 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<ReadingLogResponse>> call, Throwable t) {
                            Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /** 삭제 */
    private void deleteLog() {
        if (currentLogId == -1) {
            Toast.makeText(requireContext(), "삭제할 기록이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        logController.deleteLog(token, userId, currentLogId, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    currentLogId = -1;
                    loadLogForSelectedDate(); // 선택한 날짜 갱신
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private List<BookListHelper.BookItem> bookItems = new ArrayList<>();

    /**private void fillInputsWithLog(ReadingLogResponse log) {
        // 페이지/시간 세팅
        editPage.setText(String.valueOf(log.getPagesRead()));
        int totalMinutes = log.getMinutesRead();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        editHours.setText(String.valueOf(hours));
        editMinutes.setText(String.valueOf(minutes));

        // 선택된 책 ID
        selectedBookId = log.getBookId();

        // 검색창 숨기고 책 정보만 표시
        if (feedBookSearch != null) feedBookSearch.setVisibility(View.GONE);
        if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.VISIBLE);

        // 서버에서 책 상세 정보 가져오기
        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        BookController tempController = new BookController(getContext());

        tempController.getBookDetail(selectedBookId, new retrofit2.Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Book book = response.body();
                    bookInfoTitle.setText(book.getTitle());
                    bookInfoAuthor.setText(book.getAuthor());
                    bookInfoPublisher.setText(book.getPublisher());
                    bookInfoCategory.setText(book.getCategoryName());
                    Glide.with(requireContext()).load(book.getCover()).into(bookCover);
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Log.e("AddReadingRecord", "책 상세 조회 실패", t);
            }
        });
    }

    private void clearInputs() {
        editPage.setText("");
        editHours.setText("");
        editMinutes.setText("");
        selectedBookId = -1;


        // 기록이 없으면 검색창 다시 보이게
        if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.GONE);
        if (feedBookSearch != null) feedBookSearch.setVisibility(View.VISIBLE);
    }*/
    private void fillInputsWithLog(ReadingLogResponse log) {
        // 페이지/시간 세팅
        editPage.setText(String.valueOf(log.getPagesRead()));
        int totalMinutes = log.getMinutesRead();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        editHours.setText(String.valueOf(hours));
        editMinutes.setText(String.valueOf(minutes));

        // 선택된 책 ID
        selectedBookId = log.getBookId();

        // ✅ 검색 영역 숨기고 책 정보 영역만 보여주기
        if (etBookTitle != null) etBookTitle.setVisibility(View.GONE);
        if (searchBook != null) searchBook.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);

        if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.VISIBLE);

        // ✅ 서버에서 책 상세 정보 가져오기
        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        BookController tempController = new BookController(getContext());
        tempController.getBookDetail(selectedBookId, new retrofit2.Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Book book = response.body();
                    bookInfoTitle.setText(book.getTitle());
                    bookInfoAuthor.setText(book.getAuthor());
                    bookInfoPublisher.setText(book.getPublisher());
                    bookInfoCategory.setText(book.getCategoryName());
                    Glide.with(requireContext()).load(book.getCover()).into(bookCover);
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Log.e("AddReadingRecord", "책 상세 조회 실패", t);
            }
        });
    }

    private void clearInputs() {
        editPage.setText("");
        editHours.setText("");
        editMinutes.setText("");
        selectedBookId = -1;

        // ✅ 검색 영역 다시 보이게
        if (etBookTitle != null) etBookTitle.setVisibility(View.VISIBLE);
        if (searchBook != null) searchBook.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);

        // ✅ 책 정보 영역은 숨기기
        if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.GONE);
    }

}