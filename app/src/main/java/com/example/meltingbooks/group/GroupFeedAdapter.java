package com.example.meltingbooks.group;

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
import com.example.meltingbooks.group.comment.GroupCommentActivity;
import com.example.meltingbooks.R;

import java.util.List;

public class GroupFeedAdapter extends RecyclerView.Adapter<GroupFeedAdapter.GroupFeedViewHolder> {

    private final List<GroupFeedItem> feedList;
    private final Context context;

    public GroupFeedAdapter(Context context, List<GroupFeedItem> feedList) {
        this.context = context;
        this.feedList = feedList;
    }

    @NonNull
    @Override
    public GroupFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_feed_item, parent, false);
        return new GroupFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupFeedViewHolder holder, int position) {
        GroupFeedItem item = feedList.get(position);

        holder.userName.setText(item.getUserName());
        holder.writeDate.setText(item.getWriteDate());
        holder.content.setText(item.getContent());

        holder.commentButton.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GroupCommentActivity.class);

            // 게시글 정보 전달 (예: feedId, userName, content 등)
            intent.putExtra("postUserName", holder.userName.getText().toString());
            intent.putExtra("postContentLine1", holder.content.getText().toString());
            // 만약 두 번째 내용 줄이 따로 필요하면 GroupFeedItem에 필드를 추가하거나 적절히 수정 필요
            intent.putExtra("postContentLine2", item.getContent2()); // 현재는 빈 문자열 또는 다른 값 넣기
            // 필요한 경우 postId 등도 추가 가능

            context.startActivity(intent);
        });

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.groupImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(item.getImageUrl()).into(holder.groupImage);
        } else {
            holder.groupImage.setVisibility(View.GONE);
        }

        // 프로필 이미지는 현재 샘플 이미지로 고정, 필요 시 동적으로 바꾸세요.
        holder.profileImage.setImageResource(R.drawable.sample_profile2);
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    static class GroupFeedViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, groupImage, commentButton;
        TextView userName, writeDate, content, commentCount;

        public GroupFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            groupImage = itemView.findViewById(R.id.groupImage);
            userName = itemView.findViewById(R.id.userName);
            writeDate = itemView.findViewById(R.id.groupWriteDate);
            content = itemView.findViewById(R.id.groupWriteContent);
            commentButton = itemView.findViewById(R.id.chat_button);
            commentCount = itemView.findViewById(R.id.chat_count);
        }
    }
}
