package com.example.meltingbooks.calendar;

import static android.content.Context.MODE_PRIVATE;

import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.BookListHelper.BookItem;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;

import java.util.List;
import java.util.ArrayList;


import com.example.meltingbooks.calendar.view.CircularProgressView;
import com.example.meltingbooks.calendar.view.GoalProgressView;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailGoalFragment extends Fragment {
    public DetailGoalFragment() { }

    private String token;
    private int userId;
    private GoalController goalController;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail_goal, container, false);

        TextView cheerMessage = view.findViewById(R.id.cheer_message);

        TextView btnMonthly = view.findViewById(R.id.set_goal_monthly);
        TextView btnYearly = view.findViewById(R.id.set_goal_yearly);

        // 초기 상태 설정
        btnMonthly.setSelected(true);
        btnYearly.setSelected(false);

        // SharedPreferences에서 토큰, userId 가져오기
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        GoalApi apiService = ApiClient.getClient(token).create(GoalApi.class);
        goalController = new GoalController(apiService);


        btnMonthly.setOnClickListener(v -> {
            btnMonthly.setSelected(true);
            btnYearly.setSelected(false);
            loadGoal("MONTHLY");
        });

        btnYearly.setOnClickListener(v -> {
            btnMonthly.setSelected(false);
            btnYearly.setSelected(true);
            loadGoal("YEARLY");
        });

        // 초기값: 월간 불러오기
        loadGoal("MONTHLY");

        //그래프 처리
        setupWeeklyGraph(view);

        // btn_set_goal 클릭 시 SetGoalFragment로 전환
        View SetlGoalButton = view.findViewById(R.id.btn_set_goal);

        if (SetlGoalButton != null) {
            SetlGoalButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SetGoalFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }


    //막대그래프 생성 함수
    public void setupWeeklyGraph(View view) {
        CombinedChart combinedChart = view.findViewById(R.id.weekly_combined_chart);

        // 1. 예시 데이터: 최근 7일 읽은 시간 (시간 단위)
        float[] readingHours = new float[]{1.5f, 0.5f, 2f, 0f, 3f, 1f, 2.5f}; // 지난 7일 데이터
        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance(); // 오늘 기준
        SimpleDateFormat sdf = new SimpleDateFormat("d", Locale.KOREA); //날짜

        for (int i = 6; i >= 0; i--) {
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String label = sdf.format(calendar.getTime()); // 요일

            int index = 6 - i;
            barEntries.add(new BarEntry(index, readingHours[index]));
            lineEntries.add(new Entry(index, readingHours[index]));
            xLabels.add(label);

            calendar.add(Calendar.DAY_OF_YEAR, i); // 원래 날짜로 되돌림
        }


        // 2. 막대그래프
        BarDataSet barDataSet = new BarDataSet(barEntries, "Hours");
        barDataSet.setDrawValues(false);
        barDataSet.setColors(getBarColors(readingHours));
        barDataSet.setValueTextSize(5f);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.1f); // 막대 폭 설정


        // 3. 꺾은선 그래프
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Line");
        lineDataSet.setColor(Color.GRAY);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.enableDashedLine(10f, 5f, 0f);
        lineDataSet.setCircleColor(Color.GRAY);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);


        // 4. Combine
        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);


        // 5. X축 설정
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(barEntries.size() - 0.5f);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                } else {
                    return "";
                }
            }
        });

        // 오늘에 하이라이트
        int todayIndex = 6;
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setLabelRotationAngle(0f);


        // 6. Y축 설정
        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setEnabled(false);

        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawGridBackground(false);
        combinedChart.setDrawBorders(false);
        combinedChart.setTouchEnabled(false);
        combinedChart.setDoubleTapToZoomEnabled(false);

        Legend legend = combinedChart.getLegend();
        legend.setEnabled(false);

        combinedChart.setData(combinedData);
        combinedChart.invalidate(); // 갱신
    }

    // 막대그래프 색상 지정 (읽은 시간에 따라 색 다르게)
    private List<Integer> getBarColors(float[] hours) {
        List<Integer> colors = new ArrayList<>();
        for (float h : hours) {
            if (h == 0f) {
                colors.add(Color.parseColor("#DC8686"));
            } else if (h <= 1f) {
                colors.add(Color.parseColor("#86DCA4"));
            } else if (h <= 2f) {
                colors.add(Color.parseColor("#86B4DC"));
            } else {
                colors.add(Color.parseColor("#9A86DC"));
            }
        }
        return colors;
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

        //제목 설정
        TextView title = getView().findViewById(R.id.reading_goal); // fragment_detail_goal.xml 안에 제목 TextView 있어야 함

        if ("MONTHLY".equalsIgnoreCase(goal.getGoalType())) {
            title.setText(goal.getMonth() + "월 독서 목표 설정");
        } else if ("YEARLY".equalsIgnoreCase(goal.getGoalType())) {
            title.setText(goal.getYear() + "년 독서 목표 설정");
        }


        // 응원 메시지
        TextView cheerMessage = getView().findViewById(R.id.cheer_message);
        float totalProgress = (float) ((goal.getBookProgress() + goal.getReviewProgress() + goal.getTimeProgress()) / 3.0);


        String message;
        if (totalProgress >= 100) {
            message = "목표 달성! 너무 멋져요! 🏆";
        } else if (totalProgress >= 70) {
            message = "거의 다 왔어요! 끝까지 힘내요! ✨";
        } else if (totalProgress >= 40) {
            message = "지금까지 잘해왔어요! 남은 절반도 화이팅! 💪";
        } else {
            message = "시작이 반이에요! 천천히 함께 해요! 🌱";
        }
        cheerMessage.setText(message);

        // 원형 전체 진행률
        CircularProgressView circleProgress = getView().findViewById(R.id.circle_progress);
        ProgressBarUtil.setCircularProgress(circleProgress, totalProgress);

        // 목표 1 (책 권수)
        GoalProgressView goal1 = getView().findViewById(R.id.goal1_view);
        goal1.setUnit("권");
        goal1.setProgressWithGoal(goal.getCompletedBooks(), goal.getTargetBooks());

        // 목표 2 (리뷰 개수)
        GoalProgressView goal2 = getView().findViewById(R.id.goal2_view);
        goal2.setUnit("개");
        goal2.setProgressWithGoal(goal.getCompletedReviews(), goal.getTargetReviews());

        // 목표 3 (독서 시간)
        GoalProgressView goal3 = getView().findViewById(R.id.goal3_view);
        goal3.setUnit("시간");
        float completedHours = goal.getCompletedMinutes() / 60f;
        float targetHours = goal.getTargetMinutes() / 60f;
        goal3.setProgressWithGoal(completedHours, targetHours);


        // 20dp 높이, 제목 텍스트 크기 20sp → px 변환
        float titlePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());

        GoalProgressView goal1Detail = getView().findViewById(R.id.goal1_view_detail);
        goal1Detail.setUnit("권");
        goal1Detail.setProgressWithGoal(goal.getCompletedBooks(), goal.getTargetBooks(), 20, titlePx);

        GoalProgressView goal2Detail = getView().findViewById(R.id.goal2_view_detail);
        goal2Detail.setUnit("개");
        goal2Detail.setProgressWithGoal(goal.getCompletedReviews(), goal.getTargetReviews(), 20, titlePx);

        GoalProgressView goal3Detail = getView().findViewById(R.id.goal3_view_detail);
        goal3Detail.setUnit("시간");
        goal3Detail.setProgressWithGoal(completedHours, targetHours, 20, titlePx);

        // 책 이미지 (추후 구조 확장 대응)
        /**LinearLayout bookListContainer = getView().findViewById(R.id.book_list_container);
        bookListContainer.removeAllViews();
        if (goal.getBooks() != null) {
            for (GoalResponse.Book book : goal.getBooks()) {
                ImageView imageView = new ImageView(requireContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 220);
                params.setMargins(8, 0, 8, 0);
                imageView.setLayoutParams(params);

                Glide.with(this)
                        .load(book.getCoverUrl())
                        .into(imageView);

                bookListContainer.addView(imageView);
            }
        }*/
    }

}

