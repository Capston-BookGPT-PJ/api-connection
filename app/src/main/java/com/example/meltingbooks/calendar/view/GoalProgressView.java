package com.example.meltingbooks.calendar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;

import java.util.Locale;

public class GoalProgressView extends FrameLayout {

    private TextView tvTitle;
    private View vProgressFill;
    private TextView tvSubtext;

    private float currentPercent = 0f;
    private String unit = "권"; // 기본 단위 (권 / 개 / 시간)

    public GoalProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_goal_progress, this, true);

        tvTitle = findViewById(R.id.tv_title);
        vProgressFill = findViewById(R.id.v_progress_fill);
        tvSubtext = findViewById(R.id.tv_subtext);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GoalProgressView);
            String title = ta.getString(R.styleable.GoalProgressView_goalTitle);
            String subText = ta.getString(R.styleable.GoalProgressView_goalSubText);
            int progressDrawableResId = ta.getResourceId(R.styleable.GoalProgressView_progressDrawable, 0);
            String unitFromAttr = ta.getString(R.styleable.GoalProgressView_goalUnit);
            float titleTextSize = ta.getDimension(R.styleable.GoalProgressView_goalTitleTextSize, 15f); //제목 글씨 사이즈 설정

            tvTitle.setText(title);
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
            tvSubtext.setText(subText);

            if (progressDrawableResId != 0) {
                vProgressFill.setBackgroundResource(progressDrawableResId);
            }

            if (unitFromAttr != null) {
                unit = unitFromAttr; // XML에서 지정 가능
            }

            ta.recycle();
        }


    }

    /** 단위 변경 (권 / 개 / 시간) **/
    public void setUnit(String unit) {
        if (unit != null && !unit.trim().isEmpty()) {
            this.unit = unit;
        }
    }

    public void setProgressWithGoal(float current, int goal) {
        if (goal <= 0) return;

        currentPercent = (current / goal) * 100f;

        // 권, 개 → 소수점 없이 / 시간 → 소수점 1자리
        String currentStr;
        if ("권".equals(unit) || "개".equals(unit)) {
            currentStr = String.valueOf((int) current);
        } else {
            currentStr = String.format(Locale.getDefault(), "%.1f", current);
        }

        String text = currentStr + unit + " / " + goal + unit;
        tvSubtext.setText(text);

        post(() -> {
            int totalWidthPx = getWidth();
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);
        });
    }

    public float getPercent() {
        return currentPercent;
    }
}
