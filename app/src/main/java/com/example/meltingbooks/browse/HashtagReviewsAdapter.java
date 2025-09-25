package com.example.meltingbooks.browse;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedDetailActivity;
import com.example.meltingbooks.network.feed.FeedResponse;

import java.util.List;

public class HashtagReviewsAdapter extends RecyclerView.Adapter<HashtagReviewsAdapter.ReviewViewHolder> {

    private List<FeedResponse> reviewList;

    public HashtagReviewsAdapter(List<FeedResponse> reviewList) {
        this.reviewList = reviewList;
    }

    public void updateReviews(List<FeedResponse> newReviewList) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviewList); // FeedPageResponse.getContent()로 받은 리스트 전달
        notifyDataSetChanged();
    }


    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName, reviewDate, reviewContent;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            reviewContent = itemView.findViewById(R.id.reviewContent);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_hashtag_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        FeedResponse feed = reviewList.get(position);

        // 닉네임
        String nickname = feed.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            nickname = "익명";
        }
        holder.userName.setText(nickname);

        // 리뷰 날짜
        holder.reviewDate.setText(feed.getFormattedCreatedAt());

        // 리뷰 내용
        holder.reviewContent.setText(feed.getContent());

        // 프로필 이미지
        String profileUrl = feed.getUserProfileImage();
        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileUrl)
                    .placeholder(R.drawable.sample_profile)
                    .error(R.drawable.sample_profile)
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.sample_profile);
            holder.profileImage.setVisibility(View.VISIBLE);
        }

        // ⭐ 아이템 전체 클릭 시에도 동일하게
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, FeedDetailActivity.class);
            intent.putExtra("postId", feed.getReviewId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}

