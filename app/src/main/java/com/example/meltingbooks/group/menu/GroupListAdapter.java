package com.example.meltingbooks.group.menu;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private List<GroupListItem> groups;

    public GroupListAdapter(List<GroupListItem> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupListItem group = groups.get(position);
        holder.groupName.setText(group.getName());
        // 예: 이미지 설정 (필요 시)
        // holder.groupImage.setImage... (group.getImage())

        // 첫 번째 아이템만 체크박스 보임
        if (position == 0) {
            holder.groupCheckBoxImage.setVisibility(View.VISIBLE);
        } else {
            holder.groupCheckBoxImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        // 최대 3개까지만 표시
        return Math.min(groups.size(), 3);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView groupImage;
        TextView groupName;
        ImageView groupCheckBoxImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.groupImage);
            groupName = itemView.findViewById(R.id.groupName);
            groupCheckBoxImage = itemView.findViewById(R.id.groupCheckBoxImage);
        }
    }
}
