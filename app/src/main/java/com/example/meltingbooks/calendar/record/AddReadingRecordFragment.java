package com.example.meltingbooks.calendar.record;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.BookListHelper.BookItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class AddReadingRecordFragment extends Fragment {

    private LocalDate selectedDate = LocalDate.now();
    private View rootView;

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

        // 책 리스트 UI 생성
        setupBooks(view);

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

    private List<BookItem> bookItems = new ArrayList<>();

    private void setupBooks(View view) {
        LinearLayout container = view.findViewById(R.id.book_list_container);

        // 샘플 데이터
        bookItems.clear();
        bookItems.add(new BookItem(R.drawable.book_image_1, false));
        bookItems.add(new BookItem(R.drawable.book_image_2, true));
        bookItems.add(new BookItem(R.drawable.book_image_3, false));

        BookListHelper.setupBooks(getContext(), container, bookItems, true);
    }

    /*
    // DB연결 후 선택된 책의 페이지 수 가져오는 로직
    TextView totalPageText = rootView.findViewById(R.id.text_total_page);
    int totalPage = getSelectedBookTotalPages(); // DB나 객체에서 가져오기
    totalPageText.setText("p / " + totalPage + "p");*/

}