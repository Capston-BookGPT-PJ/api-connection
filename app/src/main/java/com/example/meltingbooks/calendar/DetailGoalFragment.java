package com.example.meltingbooks.calendar;

/**import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.BookListHelper.BookItem;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
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


public class DetailGoalFragment extends Fragment {
    public DetailGoalFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail_goal, container, false);

        TextView cheerMessage = view.findViewById(R.id.cheer_message);

        // 예: 목표 달성률을 서버에서 받아왔다고 가정
        int progressRate = 52; // 백에서 받아온 값

        String message;
        if (progressRate >= 100) {
            message = "목표 달성! 너무 멋져요! 🏆";
        } else if (progressRate >= 70) {
            message = "거의 다 왔어요! 끝까지 힘내요! ✨";
        } else if (progressRate >= 40) {
            message = "지금까지 잘해왔어요! 남은 절반도 화이팅! 💪";
        } else {
            message = "시작이 반이에요! 천천히 함께 해요! 🌱";
        }

        cheerMessage.setText(message);

        // goal1에 해당하는 progressbar fill View
        View goal1ProgressFill = view.findViewById(R.id.goal1_progressbar_fill);
        int goal1progress = 80;
        ProgressBarUtil.setProgressBar(requireContext(), goal1ProgressFill, goal1progress, 300);

        // goal2에 해당하는 progressbar fill View
        View goal2ProgressFill = view.findViewById(R.id.goal2_progressbar_fill);
        int goal2progress = 33;
        ProgressBarUtil.setProgressBar(requireContext(), goal2ProgressFill, goal2progress, 300);

        // goal3에 해당하는 progressbar fill View
        View goal3ProgressFill = view.findViewById(R.id.goal3_progressbar_fill);
        int goal3progress = 22;
        ProgressBarUtil.setProgressBar(requireContext(), goal3ProgressFill, goal3progress, 300);


        //책 이미지 처리 리턴
        List<BookItem> books = new ArrayList<>();

        books.add(new BookItem(R.drawable.book_image_1, true));
        books.add(new BookItem(R.drawable.book_image_2, true));
        books.add(new BookItem(R.drawable.book_image_3, false));

        LinearLayout bookContainer = view.findViewById(R.id.book_list_container);
        BookListHelper.setupBooks(getContext(), bookContainer, books, false);

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


}*/

import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.BookListHelper.BookItem;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;

import java.util.List;
import java.util.ArrayList;


import com.example.meltingbooks.calendar.view.CircularProgressView;
import com.example.meltingbooks.calendar.view.GoalProgressView;
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


public class DetailGoalFragment extends Fragment {
    public DetailGoalFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail_goal, container, false);

        TextView cheerMessage = view.findViewById(R.id.cheer_message);

        // 예: 목표 달성률을 서버에서 받아왔다고 가정
        int progressRate = 52; // 백에서 받아온 값

        String message;
        if (progressRate >= 100) {
            message = "목표 달성! 너무 멋져요! 🏆";
        } else if (progressRate >= 70) {
            message = "거의 다 왔어요! 끝까지 힘내요! ✨";
        } else if (progressRate >= 40) {
            message = "지금까지 잘해왔어요! 남은 절반도 화이팅! 💪";
        } else {
            message = "시작이 반이에요! 천천히 함께 해요! 🌱";
        }

        cheerMessage.setText(message);


        GoalProgressView goal1 = view.findViewById(R.id.goal1_view);
        goal1.setUnit("권");
        goal1.setProgressWithGoal(2, 3);

        GoalProgressView goal2 = view.findViewById(R.id.goal2_view);
        goal2.setUnit("개");
        goal2.setProgressWithGoal(1, 3);

        GoalProgressView goal3 = view.findViewById(R.id.goal3_view);
        goal3.setUnit("시간");
        goal3.setProgressWithGoal(4.5f, 15);

        // 전체 목표 평균 progress 계산
        float totalProgress = (goal1.getPercent() + goal2.getPercent() + goal3.getPercent()) / 3f;

        //원형 전체 목표 표시
        CircularProgressView circleProgress = view.findViewById(R.id.circle_progress);
        ProgressBarUtil.setCircularProgress(circleProgress, totalProgress);


        // goal1에 해당하는 progressbar fill View
        View goal1ProgressFill = view.findViewById(R.id.goal1_progressbar_fill);
        float goal1progress = goal1.getPercent();
        ProgressBarUtil.setProgressBar(requireContext(), goal1ProgressFill, goal1progress, 300);

        // goal2에 해당하는 progressbar fill View
        View goal2ProgressFill = view.findViewById(R.id.goal2_progressbar_fill);
        float goal2progress = goal2.getPercent();
        ProgressBarUtil.setProgressBar(requireContext(), goal2ProgressFill, goal2progress, 300);

        // goal3에 해당하는 progressbar fill View
        View goal3ProgressFill = view.findViewById(R.id.goal3_progressbar_fill);
        float goal3progress = goal3.getPercent();
        ProgressBarUtil.setProgressBar(requireContext(), goal3ProgressFill, goal3progress, 300);


        //책 이미지 처리 리턴
        List<BookItem> books = new ArrayList<>();

        books.add(new BookItem(R.drawable.book_image_1, true));
        books.add(new BookItem(R.drawable.book_image_2, true));
        books.add(new BookItem(R.drawable.book_image_3, false));

        LinearLayout bookContainer = view.findViewById(R.id.book_list_container);
        BookListHelper.setupBooks(getContext(), bookContainer, books, false);

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


}

