package com.example.meltingbooks.feed;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.feed.comment.CommentBottomSheet;
import com.example.meltingbooks.R;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private final List<FeedItem> feedList;
    private final Context context;

    public FeedAdapter(Context context, List<FeedItem> feedList) {
        this.context = context;
        this.feedList = feedList;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = feedList.get(position);
        holder.userName.setText(item.getUserName());
        holder.reviewContent.setText(item.getReviewContent());
        holder.reviewDate.setText(item.getReviewDate());

        //댓글 버튼 클릭 리스너
        holder.commentButton.setOnClickListener(v -> {
            //나중에 feed_id를 통해 서버와 통신할 수 있도록 수정
            CommentBottomSheet commentBottomSheet = new CommentBottomSheet();
            // 댓글 수 업데이트 리스너 설정
            commentBottomSheet.setOnCommentAddedListener(commentCount -> {
                holder.commentCount.setText(String.valueOf(commentCount));
            });

            commentBottomSheet.show(((AppCompatActivity)v.getContext()).getSupportFragmentManager(), "CommentBottomSheet");
        });


        //공유 버튼 클릭 리스너
        holder.shareButton.setOnClickListener(v -> {

            String shareUrl = "https://yourapp.com/post/12345"; //+ item.getPostId();  공유할 URL 예시 나중에 실제 URL 공유 기능 추가

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
            v.getContext().startActivity(Intent.createChooser(shareIntent, "공유하기"));

            /*
                Intent intent = new Intent(context, FeedItemActivity.class);
                intent.putExtra("postId", item.getPostId()); // 게시물 ID 전달
                context.startActivity(intent);
             */
        });

        // 이미지 표시
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.feedImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(item.getImageUrl()).into(holder.feedImage);
        } else {
            holder.feedImage.setVisibility(View.GONE);
        }

        // 투표 기능
        /*if (item.hasVote()) {
            holder.voteLayout.setVisibility(View.VISIBLE);
        } else {
            holder.voteLayout.setVisibility(View.GONE);
        }
        holder.voteOption1.setOnClickListener(v -> {
            Toast.makeText(context, "예 선택!", Toast.LENGTH_SHORT).show();
        });

        holder.voteOption2.setOnClickListener(v -> {
            Toast.makeText(context, "아니오 선택!", Toast.LENGTH_SHORT).show();
        });*/


    }


    @Override
    public int getItemCount() {
        return feedList.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView userName, reviewContent, reviewDate, commentCount;
        ImageView commentButton, shareButton, feedImage;
        //LinearLayout voteLayout;
        //Button voteOption1, voteOption2;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            reviewContent = itemView.findViewById(R.id.reviewContent);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            commentButton = itemView.findViewById(R.id.comment_button); // 댓글 버튼
            commentCount = itemView.findViewById(R.id.comment_count); // 댓글 수 표시 텍스트뷰
            shareButton = itemView.findViewById(R.id.share_Button); // 공유 버튼
            feedImage = itemView.findViewById(R.id.feedImage); // 피드에 이미지가 있을경우 사용.
            /*voteLayout = itemView.findViewById(R.id.voteLayout);
            voteOption1 = itemView.findViewById(R.id.voteOption1);
            voteOption2 = itemView.findViewById(R.id.voteOption2);*/
        }
    }
}
