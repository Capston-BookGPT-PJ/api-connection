package com.example.meltingbooks.group.menu;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.profile.ProfileActivity;
import com.example.meltingbooks.R;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private List<GroupMemberItem> members;
    private int feedGroupId; // 현재 피드 그룹 ID

    public GroupMemberAdapter(List<GroupMemberItem> members) {
        this.members = members;
    }

    public GroupMemberAdapter(List<GroupMemberItem> members, int feedGroupId) {
        this.members = members;
        this.feedGroupId = feedGroupId;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_member_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMemberItem member = members.get(position);

        holder.memberName.setText(member.getNickname());


        // 이미지 Glide 로딩 가능
        Glide.with(holder.itemView.getContext())
                .load(member.getProfileImageUrl())
                .circleCrop() // 🔹 원형으로 만들기
                .placeholder(R.drawable.sample_profile2)
                .into(holder.memberImage);


        // ✅ 현재 피드 그룹과 일치하면 체크박스 보임
        if (member.getGroupId() == feedGroupId) {
            holder.myself.setVisibility(View.VISIBLE);
            holder.groupCheckBoxImage.setVisibility(View.GONE);
        } else {
            holder.myself.setVisibility(View.GONE);
            holder.groupCheckBoxImage.setVisibility(View.VISIBLE);
        }

        // ⭐ memberImage 클릭 이벤트 추가
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", member.getUserId()); // 필요하면 멤버 ID 전달
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView memberImage;
        ImageView myself;
        TextView memberName;
        ImageView groupCheckBoxImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImage = itemView.findViewById(R.id.memberImage);
            myself = itemView.findViewById(R.id.myself);
            memberName = itemView.findViewById(R.id.memberName);
            groupCheckBoxImage = itemView.findViewById(R.id.groupCheckBoxImage);
        }
    }
}
