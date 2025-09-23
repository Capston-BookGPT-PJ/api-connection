package com.example.meltingbooks.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.Review;

import java.util.List;

public class SearchReviewAdapter extends RecyclerView.Adapter<SearchReviewAdapter.ViewHolder> {

    private List<Review> reviewList;

    public SearchReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView searchItemText;

        public ViewHolder(View itemView) {
            super(itemView);
            searchItemText = itemView.findViewById(R.id.searchItemText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_review_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.searchItemText.setText(review.getTitle());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}
