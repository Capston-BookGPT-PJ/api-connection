package com.example.meltingbooks.group.comment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;

import java.util.List;

public class GroupCommentAdapter extends RecyclerView.Adapter<GroupCommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<GroupCommentItem> commentList;

    public GroupCommentAdapter(Context context, List<GroupCommentItem> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        GroupCommentItem comment = commentList.get(position);

        holder.commentUserName.setText(comment.getUserName());  // ✅ 변수명 정리 (userName → authorName 권장)
        holder.commentContent.setText(comment.getContent());
        holder.commentDate.setText(comment.getCommentDate());

        // ✅ 프로필 이미지 Glide 처리
        if (comment.getProfileImageUrl() != null && !comment.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(comment.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile)
                    .error(R.drawable.sample_profile)
                    .into(holder.commentProfileImage);
        } else {
            holder.commentProfileImage.setImageResource(R.drawable.sample_profile);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentUserName, commentContent, commentDate;
        ImageView commentProfileImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserName = itemView.findViewById(R.id.commentUserName);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentProfileImage = itemView.findViewById(R.id.commentProfileImage);
            commentDate = itemView.findViewById(R.id.commentDate);
        }
    }
}
