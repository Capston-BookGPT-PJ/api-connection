package com.example.meltingbooks.browse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;

import java.util.List;

public class BrowseReviewAdapter extends RecyclerView.Adapter<BrowseReviewAdapter.ReviewViewHolder> {

    private List<String> reviewList;

    public BrowseReviewAdapter(List<String> reviewList) {
        this.reviewList = reviewList;
    }

    // 책에 따른 감상평
    public void updateReviews(List<String> newReviewList) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviewList);
        notifyDataSetChanged();
    }
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView reviewText;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            reviewText = itemView.findViewById(R.id.reviewContent);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_book_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.reviewText.setText(reviewList.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}

