package com.example.meltingbooks.calendar.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.meltingbooks.R;

import java.util.List;

public class BookListHelper {

    public static class BookItem {
        public int imageResId;
        public boolean isRead;
        public boolean isSelected = false;

        public BookItem(int imageResId, boolean isRead) {
            this.imageResId = imageResId;
            this.isRead = isRead;
        }
    }

    public static void setupBooks(Context context, ViewGroup container, List<BookItem> bookItems, boolean showOverlayAndSelection) {
        container.removeAllViews();

        for (BookItem book : bookItems) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_book, container, false);

            ImageView bookImage = itemView.findViewById(R.id.book_image);
            View selectionBorder = itemView.findViewById(R.id.selection_border);
            View overlayRead = itemView.findViewById(R.id.overlay_read);
            ImageView awardIcon = itemView.findViewById(R.id.award_icon);

            bookImage.setImageResource(book.imageResId);

            if (book.isRead) {
                awardIcon.setVisibility(View.VISIBLE);
                overlayRead.setVisibility(showOverlayAndSelection ? View.VISIBLE : View.GONE);
            } else {
                awardIcon.setVisibility(View.GONE);
                overlayRead.setVisibility(View.GONE);
            }

            if (showOverlayAndSelection) {
                itemView.setOnClickListener(v -> {
                    book.isSelected = !book.isSelected;
                    selectionBorder.setVisibility(book.isSelected ? View.VISIBLE : View.GONE);
                });
            } else {
                selectionBorder.setVisibility(View.GONE);
            }

            container.addView(itemView);
        }
    }
}