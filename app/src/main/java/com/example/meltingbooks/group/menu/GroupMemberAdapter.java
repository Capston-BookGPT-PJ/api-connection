package com.example.meltingbooks.group.menu;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.profile.ProfileActivity;
import com.example.meltingbooks.R;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private List<GroupMemberItem> members;

    public GroupMemberAdapter(List<GroupMemberItem> members) {
        this.members = members;
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

        holder.memberName.setText(member.getName());
        // 예: 이미지 설정
        // holder.memberImage.setImage... (member.getImage())

        if (position == 0) {
            // 첫 번째 멤버: myself 보여주고, groupCheckBoxImage 숨김
            holder.myself.setVisibility(View.VISIBLE);
            holder.groupCheckBoxImage.setVisibility(View.GONE);
        } else {
            // 두 번째 이후 멤버: myself 숨기고, groupCheckBoxImage 보여줌
            holder.myself.setVisibility(View.GONE);
            holder.groupCheckBoxImage.setVisibility(View.VISIBLE);
        }

        // ⭐ memberImage 클릭 이벤트 추가
        holder.memberImage.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            // intent.putExtra("memberId", member.getId()); // 필요하면 멤버 ID 전달
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
