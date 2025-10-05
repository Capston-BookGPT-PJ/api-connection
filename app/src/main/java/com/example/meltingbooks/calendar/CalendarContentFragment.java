package com.example.meltingbooks.calendar;

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
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
import com.example.meltingbooks.calendar.view.CircularProgressView;
import com.example.meltingbooks.calendar.view.GoalProgressView;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookApi;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.example.meltingbooks.network.log.LogApi;
import com.example.meltingbooks.network.log.LogController;
import com.example.meltingbooks.network.log.ReadingLogResponse;
import com.example.meltingbooks.network.book.BookController;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class CalendarContentFragment extends Fragment {
    public CalendarContentFragment() { }

    //달력 생성
    private GridLayout calendarGrid;
    private TextView textMonth;
    private Calendar currentCalendar;

    private TextView selectedDayView = null;
    private Calendar selectedDate = Calendar.getInstance();  // 기본: 오늘

    private String token;
    private int userId;

    private GoalController goalController;
    private LogController logController;
    private BookController bookController;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_content, container, false);


        // SharedPreferences에서 토큰, userId 가져오기
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);


        //달력 생성 관련--------------------------
        calendarGrid = view.findViewById(R.id.calendarGrid);
        textMonth = view.findViewById(R.id.textMonth);

        currentCalendar = Calendar.getInstance();
        updateCalendar(view);
        // 오늘 날짜로 초기화
        SimpleDateFormat format = new SimpleDateFormat("M/d (E)", Locale.KOREA);
        TextView goalByDate = view.findViewById(R.id.goal_by_date);
        if (goalByDate != null) {
            goalByDate.setText(format.format(selectedDate.getTime()));
        }


        // 이전 달
        view.findViewById(R.id.btnPrev).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar(view);
        });

        // 다음 달
        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar(view);
        });

        // detail_goal_button 클릭 시 DetailGoalFragment로 전환
        View detailGoalButton = view.findViewById(R.id.detail_goal_button);
        if (detailGoalButton != null) {
            detailGoalButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new DetailGoalFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // btn_monthly_report 클릭 시 MonthlyReportFragment 로 전환
        View MonthlyReportButton = view.findViewById(R.id.btn_monthly_report);
        if (MonthlyReportButton != null) {
            MonthlyReportButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MonthlyReportFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        GoalApi apiService = ApiClient.getClient(token).create(GoalApi.class);
        goalController = new GoalController(apiService);

        // onCreateView()에서
        LogApi logApi = ApiClient.getClient(token).create(LogApi.class);
        logController = new LogController(logApi);

        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        bookController = new BookController(getContext());

        // 월간 불러오기
        loadGoal("MONTHLY");

        // 책 리스트 UI 생성
        setupBooks(view);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String todayStr = sdf.format(selectedDate.getTime());
        loadLogsByDate(todayStr);

        return view;
    }

    //달력 생성 알고리즘
    private void updateCalendar(View view) {
        // 요일 표시
        LinearLayout weekdaysRow = view.findViewById(R.id.weekdaysRow);
        weekdaysRow.removeAllViews();

        String[] weekdays = {"S", "M", "T", "W", "T", "F", "S"};
        for (int i = 0; i < weekdays.length; i++) {
            TextView dayLabel = new TextView(getContext());
            dayLabel.setText(weekdays[i]);
            dayLabel.setTextSize(14);
            dayLabel.setGravity(Gravity.CENTER);
            dayLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            // 색상 지정
            if (i == 0) {
                dayLabel.setTextColor(Color.parseColor("#FC1F8E")); // 일요일
            } else if (i == 6) {
                dayLabel.setTextColor(Color.parseColor("#1D9BF0")); // 토요일
            } else {
                dayLabel.setTextColor(Color.BLACK); // 평일
            }

            weekdaysRow.addView(dayLabel);
        }

        calendarGrid.removeAllViews();

        // 현재 월의 정보
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);

        // 달 이름 표시
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH);
        textMonth.setText(monthFormat.format(currentCalendar.getTime()));

        // 1일이 무슨 요일인지 계산 (0:일 ~ 6:토)
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, 1);
        int startDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;

        int maxDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 빈칸 먼저 채우기
        for (int i = 0; i < startDayOfWeek; i++) {
            TextView emptyView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            emptyView.setLayoutParams(params);
            calendarGrid.addView(emptyView);
        }

        //날짜 채우기
        for (int day = 1; day <= maxDay; day++) {
            TextView dayView = new TextView(getContext());
            dayView.setText(String.valueOf(day));
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(16);
            dayView.setPadding(8, 8, 8, 8);

            // 크기 설정 (정사각형)
            int sizeInDp = 35;
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getResources().getDisplayMetrics()
            );
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = sizeInPx;
            params.height = sizeInPx;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            dayView.setLayoutParams(params);

            Calendar thisDate = Calendar.getInstance();
            thisDate.set(year, month, day);

            // 초기 스타일 적용
            if (thisDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                    thisDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                    thisDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)) {
                dayView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));
                dayView.setTextColor(Color.WHITE);
                selectedDayView = dayView;
            } else {
                dayView.setTextColor(Color.BLACK);
            }

            // 클릭 이벤트 처리
            dayView.setOnClickListener(v -> {
                // 기존 선택 해제
                if (selectedDayView != null) {
                    selectedDayView.setBackground(null);
                    selectedDayView.setTextColor(Color.BLACK);
                }

                // 새로 선택
                selectedDayView = (TextView) v;
                selectedDayView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));
                selectedDayView.setTextColor(Color.WHITE);

                // 날짜 저장
                selectedDate.set(year, month, Integer.parseInt(dayView.getText().toString()));

                // 날짜별 기록 표시
                SimpleDateFormat format = new SimpleDateFormat("M/d (E)", Locale.KOREA);
                TextView goalByDate = getActivity().findViewById(R.id.goal_by_date);
                if (goalByDate != null) {
                    goalByDate.setText(format.format(selectedDate.getTime()));
                }

                // 선택한 날짜의 로그 조회
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                String selectedDateStr = sdf.format(selectedDate.getTime());
                loadLogsByDate(selectedDateStr);

            });

            calendarGrid.addView(dayView);
        }

    }

    private List<BookListHelper.BookItem> bookItems = new ArrayList<>();

    private void setupBooks(View view) {
        LinearLayout container = view.findViewById(R.id.book_list_container);

        // 샘플 데이터
        bookItems.clear();
        bookItems.add(new BookListHelper.BookItem(R.drawable.book_image, false));

        BookListHelper.setupBooks(getContext(), container, bookItems, true);
    }

    private void loadGoal(String goalType) {
        goalController.getGoals(token, new GoalController.GoalCallback<List<GoalResponse>>() {
            @Override
            public void onSuccess(List<GoalResponse> goals) {
                if (goals != null && !goals.isEmpty()) {

                    // 현재 날짜 구하기
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    int currentYear = calendar.get(java.util.Calendar.YEAR);
                    int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1; // 0부터 시작하므로 +1

                    GoalResponse selectedGoal = null;

                    for (GoalResponse goal : goals) {
                        if ("MONTHLY".equalsIgnoreCase(goalType)) {
                            // 월간 목표 → goalType = MONTHLY, 현재 연도/월 일치
                            if ("MONTHLY".equalsIgnoreCase(goal.getGoalType())
                                    && goal.getYear() == currentYear
                                    && goal.getMonth() != null
                                    && goal.getMonth() == currentMonth) {
                                selectedGoal = goal;
                                break;
                            }
                        } else if ("YEARLY".equalsIgnoreCase(goalType)) {
                            // 연간 목표 → goalType = YEARLY, 현재 연도 일치
                            if ("YEARLY".equalsIgnoreCase(goal.getGoalType())
                                    && goal.getYear() == currentYear) {
                                selectedGoal = goal;
                                break;
                            }
                        }
                    }

                    if (selectedGoal != null) {
                        bindGoalData(selectedGoal);
                    } else {
                        Log.w("DetailGoalFragment", "해당하는 목표 없음 (" + goalType + ")");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailGoalFragment", "loadGoal error: " + errorMessage);
            }
        });
    }

    private void bindGoalData(GoalResponse goal) {
        float totalProgress = (float) ((goal.getBookProgress() + goal.getReviewProgress() + goal.getTimeProgress()) / 3.0);

        // 원형 전체 진행률
        CircularProgressView circleProgress = getView().findViewById(R.id.circle_progress);
        ProgressBarUtil.setCircularProgress(circleProgress, totalProgress);

        // 목표 1 (책 권수)
        GoalProgressView goal1 = getView().findViewById(R.id.goal1_view);
        goal1.setUnit("권");
        goal1.setProgressWithGoal(goal.getCompletedBooks(), goal.getTargetBooks(),goal.getBookProgress());

        // 목표 2 (리뷰 개수)
        GoalProgressView goal2 = getView().findViewById(R.id.goal2_view);
        goal2.setUnit("개");
        goal2.setProgressWithGoal(goal.getCompletedReviews(), goal.getTargetReviews(),goal.getReviewProgress());

        // 목표 3 (독서 시간)
        GoalProgressView goal3 = getView().findViewById(R.id.goal3_view);
        goal3.setUnit("시간");
        float completedHours = goal.getCompletedMinutes() / 60f;
        float targetHours = goal.getTargetMinutes() / 60f;
        goal3.setProgressWithGoal(completedHours, targetHours, goal.getTimeProgress());
    }

    private void loadLogsByDate(String dateStr) {
        logController.getLogsByPeriod(token, userId, dateStr, dateStr,
                new retrofit2.Callback<ApiResponse<List<ReadingLogResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReadingLogResponse>>> call,
                                           Response<ApiResponse<List<ReadingLogResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReadingLogResponse> logs = response.body().getData();
                            displayLog(dateStr, logs);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReadingLogResponse>>> call, Throwable t) {
                        Log.e("CalendarContent", "Log fetch failed", t);
                    }
                });
    }

    private void displayLog(String selectedDateStr, List<ReadingLogResponse> logs) {
        View root = getView();
        if (root == null) return;

        // 숫자 표시 뷰 참조
        TextView editPage = root.findViewById(R.id.edit_page);
        TextView editHour = root.findViewById(R.id.edit_hour);
        TextView editMinute = root.findViewById(R.id.edit_minute);

        int totalPages = 0;
        int totalMinutes = 0;

        if (logs != null && !logs.isEmpty()) {
            for (ReadingLogResponse log : logs) {
                totalPages += log.getPagesRead();
                totalMinutes += log.getMinutesRead();
            }
        } else {
            // 기록 없을 때
            Toast.makeText(getContext(), "기록이 없습니다.", Toast.LENGTH_SHORT).show();
        }

        // 값 세팅
        editPage.setText(String.valueOf(totalPages));

        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;
        editHour.setText(String.valueOf(hour));
        editMinute.setText(String.valueOf(minute));


        // 책 표지 처리
        LinearLayout container = root.findViewById(R.id.book_list_container);
        container.removeAllViews();
        bookItems.clear();

        if (logs != null) {
            for (ReadingLogResponse log : logs) {
                int bookId = log.getBookId();

                //bookId 기반으로 책 상세 조회
                bookController.getBookDetail(bookId, new retrofit2.Callback<Book>() {
                    @Override
                    public void onResponse(Call<Book> call, Response<Book> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Book book = response.body();
                            String coverUrl = book.getCover();

                            // 커버 URL을 BookItem에 넣어주도록 BookListHelper 확장
                            BookListHelper.BookItem item = new BookListHelper.BookItem(coverUrl, false);
                            bookItems.add(item);

                            // UI 갱신
                            BookListHelper.setupBooks(getContext(), container, bookItems, true);
                        }
                    }

                    @Override
                    public void onFailure(Call<Book> call, Throwable t) {
                        Log.e("CalendarContent", "책 상세 조회 실패: " + bookId, t);
                    }
                });
            }
        }

    }
}
