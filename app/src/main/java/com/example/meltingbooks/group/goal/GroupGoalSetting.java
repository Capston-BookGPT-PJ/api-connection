package com.example.meltingbooks.group.goal;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;

public class GroupGoalSetting extends Fragment {

    public interface OnGoalSetListener {
        void onGoalSet(GroupGoal goal);
    }
    private OnGoalSetListener goalSetListener;

    private TextView btnMonthly, btnYearly, setGoal;
    private EditText editBook, editReview, editTime;
    private TextView goalBooks, goalReviews, goalTime;
    private ProgressBar progressBooks, progressReviews, progressTime;

    public GroupGoalSetting() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnGoalSetListener) {
            goalSetListener = (OnGoalSetListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_goal_setting, container, false);

        // 버튼
        btnMonthly = view.findViewById(R.id.set_goal_monthly);
        btnYearly = view.findViewById(R.id.set_goal_yearly);
        setGoal = view.findViewById(R.id.btn_set_goal);

        // 목표 입력 필드
        editBook = view.findViewById(R.id.edit_page);
        editReview = view.findViewById(R.id.edit_review);
        editTime = view.findViewById(R.id.edit_time);

        // 목표/Progress UI
        goalBooks = view.findViewById(R.id.goal_books);
        goalReviews = view.findViewById(R.id.goal_reviews);
        goalTime = view.findViewById(R.id.goal_time);
        progressBooks = view.findViewById(R.id.progress_books);
        progressReviews = view.findViewById(R.id.progress_reviews);
        progressTime = view.findViewById(R.id.progress_time);

        // 초기 상태: 월간 선택
        btnMonthly.setSelected(true);
        btnYearly.setSelected(false);

        btnMonthly.setOnClickListener(v -> {
            btnMonthly.setSelected(true);
            btnYearly.setSelected(false);
        });

        btnYearly.setOnClickListener(v -> {
            btnMonthly.setSelected(false);
            btnYearly.setSelected(true);
        });

        // 목표 저장 버튼
        setGoal.setOnClickListener(v -> {
            if (goalSetListener != null) {
                GroupGoal goal = getGroupGoalFromInput();
                goalSetListener.onGoalSet(goal);
            }
        });

        // groupArrow 클릭 처리
        ImageView groupArrow = view.findViewById(R.id.group_arrow);
        if (groupArrow != null) {
            groupArrow.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.group_goal_fragment, new GroupGoalSetting())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    private GroupGoal getGroupGoalFromInput() {
        int books = parseIntSafe(editBook.getText().toString());
        int reviews = parseIntSafe(editReview.getText().toString());
        int time = parseIntSafe(editTime.getText().toString());
        boolean isMonthly = btnMonthly.isSelected();
        return new GroupGoal(isMonthly ? "monthly" : "yearly", books, reviews, time);
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Activity에서 Progress/Goal UI 업데이트용
    public void updateGoalProgress(int targetBooks, int targetReviews, int targetTime,
                                   int currentBooks, int currentReviews, int currentTime) {
        if (goalBooks == null || progressBooks == null) return;

        goalBooks.setText("책 목표: " + currentBooks + " / " + targetBooks + "권");
        progressBooks.setMax(targetBooks);
        progressBooks.setProgress(currentBooks);

        goalReviews.setText("감상문 목표: " + currentReviews + " / " + targetReviews + "개");
        progressReviews.setMax(targetReviews);
        progressReviews.setProgress(currentReviews);

        goalTime.setText("시간 목표: " + currentTime + " / " + targetTime + "시간");
        progressTime.setMax(targetTime);
        progressTime.setProgress(currentTime);
    }

    public static class GroupGoal {
        public String period;
        public int targetBooks;
        public int targetReviews;
        public int targetTime;

        public GroupGoal(String period, int targetBooks, int targetReviews, int targetTime) {
            this.period = period;
            this.targetBooks = targetBooks;
            this.targetReviews = targetReviews;
            this.targetTime = targetTime;
        }
    }
}
