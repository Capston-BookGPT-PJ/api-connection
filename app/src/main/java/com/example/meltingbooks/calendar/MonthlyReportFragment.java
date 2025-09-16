package com.example.meltingbooks.calendar;

/**import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;

import java.util.ArrayList;
import java.util.List;

public class MonthlyReportFragment extends Fragment {
    public MonthlyReportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);

        // FrameLayout 참조 (부모 뷰)

        FrameLayout goal1ProgressFrame = view.findViewById(R.id.m_goal_1_progressbar_frame);
        View goal1ProgressFill = view.findViewById(R.id.m_goal1_progressbar_fill);
        int goal1progress = 80;

        goal1ProgressFrame.post(() -> {
            int totalWidthInPx = goal1ProgressFrame.getWidth();
            ProgressBarUtil.setProgressBarWithPx(goal1ProgressFill, goal1progress, totalWidthInPx);
        });


        // goal2에 해당하는 progressbar fill View
        FrameLayout goal2ProgressFrame = view.findViewById(R.id.m_goal_2_progressbar_frame);
        View goal2ProgressFill = view.findViewById(R.id.m_goal2_progressbar_fill);
        int goal2progress = 33;
        goal1ProgressFrame.post(() -> {
            int totalWidthInPx = goal2ProgressFrame.getWidth();
            ProgressBarUtil.setProgressBarWithPx(goal2ProgressFill, goal2progress, totalWidthInPx);
        });

        // goal3에 해당하는 progressbar fill View
        FrameLayout goal3ProgressFrame = view.findViewById(R.id.m_goal_3_progressbar_frame);
        View goal3ProgressFill = view.findViewById(R.id.m_goal3_progressbar_fill);
        int goal3progress = 33;
        goal1ProgressFrame.post(() -> {
            int totalWidthInPx = goal3ProgressFrame.getWidth();
            ProgressBarUtil.setProgressBarWithPx(goal3ProgressFill, goal3progress, totalWidthInPx);
        });

        // 전체 목표 평균 progress 계산
        int totalProgress = (goal1progress + goal2progress + goal3progress) / 3;

        // 전체 progress 적용
        View goalTotalProgressFill = view.findViewById(R.id.goal_total_progressbar_fill);
        ProgressBarUtil.setProgressBar(requireContext(), goalTotalProgressFill, totalProgress, 300);

        // TextView에 표시
        TextView totalPercentageTextView = view.findViewById(R.id.total_percentage);
        totalPercentageTextView.setText(totalProgress + "%");


        //책 이미지 처리 리턴
        List<BookListHelper.BookItem> books = new ArrayList<>();

        books.add(new BookListHelper.BookItem(R.drawable.book_image_1, true));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_2, true));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_3, true));

        LinearLayout bookContainer = view.findViewById(R.id.book_list_container);
        BookListHelper.setupBooks(getContext(), bookContainer, books, false);

        return view;
    }
}*/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
import com.example.meltingbooks.calendar.view.GoalProgressView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonthlyReportFragment extends Fragment {
    public MonthlyReportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);

        // FrameLayout 참조 (부모 뷰)

        // goal1에 해당하는 progressbar fill View
        /**FrameLayout goal1ProgressFrame = view.findViewById(R.id.m_goal_1_progressbar_frame);
         View goal1ProgressFill = view.findViewById(R.id.m_goal1_progressbar_fill);
         int goal1progress = 80;

         goal1ProgressFrame.post(() -> {
         int totalWidthInPx = goal1ProgressFrame.getWidth();
         ProgressBarUtil.setProgressBarWithPx(goal1ProgressFill, goal1progress, totalWidthInPx);
         });
         //GoalProgressView goal1 = view.findViewById(R.id.goal1_view);

         // goal2에 해당하는 progressbar fill View
         FrameLayout goal2ProgressFrame = view.findViewById(R.id.m_goal_2_progressbar_frame);
         View goal2ProgressFill = view.findViewById(R.id.m_goal2_progressbar_fill);
         int goal2progress = 33;
         goal2ProgressFrame.post(() -> {
         int totalWidthInPx = goal2ProgressFrame.getWidth();
         ProgressBarUtil.setProgressBarWithPx(goal2ProgressFill, goal2progress, totalWidthInPx);
         });

         // goal3에 해당하는 progressbar fill View
         FrameLayout goal3ProgressFrame = view.findViewById(R.id.m_goal_3_progressbar_frame);
         View goal3ProgressFill = view.findViewById(R.id.m_goal3_progressbar_fill);
         int goal3progress = 33;
         goal3ProgressFrame.post(() -> {
         int totalWidthInPx = goal3ProgressFrame.getWidth();
         ProgressBarUtil.setProgressBarWithPx(goal3ProgressFill, goal3progress, totalWidthInPx);
         });

         */


        //재 사용성 있도록 수정한 코드
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

        // 전체 progress 적용
        View goalTotalProgressFill = view.findViewById(R.id.goal_total_progressbar_fill);
        ProgressBarUtil.setProgressBar(requireContext(), goalTotalProgressFill, totalProgress, 300);

        // TextView에 표시 (소수점 1자리 반올림)
        TextView totalPercentageTextView = view.findViewById(R.id.total_percentage);
        totalPercentageTextView.setText(String.format(Locale.getDefault(), "%.1f%%", totalProgress));


        //책 이미지 처리 리턴
        List<BookListHelper.BookItem> books = new ArrayList<>();

        books.add(new BookListHelper.BookItem(R.drawable.book_image_1, true));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_2, true));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_3, true));

        LinearLayout bookContainer = view.findViewById(R.id.book_list_container);
        BookListHelper.setupBooks(getContext(), bookContainer, books, false);

        return view;
    }
}

