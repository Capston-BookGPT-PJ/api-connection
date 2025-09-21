package com.example.meltingbooks.group.comment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;

import java.util.List;

public class GroupCommentAdapter extends RecyclerView.Adapter<GroupCommentAdapter.ViewHolder> {

    private Context context;
    private List<GroupCommentItem> commentList;

    public GroupCommentAdapter(Context context, List<GroupCommentItem> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView commentProfileImage;
        TextView commentUserName, commentDate, commentContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentProfileImage = itemView.findViewById(R.id.commentProfileImage);
            commentUserName = itemView.findViewById(R.id.commentUserName);
            commentDate = itemView.findViewById(R.id.commentDate);
            commentContent = itemView.findViewById(R.id.commentContent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupCommentItem item = commentList.get(position);
        holder.commentUserName.setText(item.getUserName());
        holder.commentDate.setText("방금 전"); // 실제 날짜 대신 고정값
        holder.commentContent.setText(item.getContent());
        holder.commentProfileImage.setImageResource(item.getProfileImageResId());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
