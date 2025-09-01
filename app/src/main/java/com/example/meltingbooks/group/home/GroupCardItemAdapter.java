package com.example.meltingbooks.group.home;

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

public class GroupCardItemAdapter extends RecyclerView.Adapter<GroupCardItemAdapter.GroupViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(GroupCardItem item);
    }

    private Context context;
    private List<GroupCardItem> groupList;
    private OnItemClickListener listener;

    public GroupCardItemAdapter(Context context, List<GroupCardItem> groupList, OnItemClickListener listener) {
        this.context = context;
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_card_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupCardItem item = groupList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView groupImage;
        TextView groupName, groupIntro;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.groupImage);
            groupName = itemView.findViewById(R.id.groupName);
            groupIntro = itemView.findViewById(R.id.groupIntro);
        }

        public void bind(final GroupCardItem item, final OnItemClickListener listener) {
            groupName.setText(item.getName());
            groupIntro.setText(item.getIntro());

            if (item.getImageResId() != 0) {
                Glide.with(groupImage.getContext())
                        .load(item.getImageResId())
                        .circleCrop()  // drawable 이미지 둥글게
                        .into(groupImage);
            } else if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(groupImage.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.sample_profile2)
                        .error(R.drawable.sample_profile2)
                        .circleCrop()  // URL 이미지 둥글게
                        .into(groupImage);
            } else {
                Glide.with(groupImage.getContext())
                        .load(R.drawable.sample_profile2)
                        .circleCrop()  // 기본 이미지도 둥글게
                        .into(groupImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
