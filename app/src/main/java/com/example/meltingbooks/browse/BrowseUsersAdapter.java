package com.example.meltingbooks.browse;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.profile.ProfileActivity;
import com.example.meltingbooks.R;
import com.example.meltingbooks.User;

public class BrowseUsersAdapter extends RecyclerView.Adapter<BrowseUsersAdapter.ViewHolder> {

    private List<User> userList;

    public BrowseUsersAdapter(List<User> userList) {
        this.userList = userList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName, userIntro;

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            userIntro = itemView.findViewById(R.id.userIntro);
        }
    }

    @NonNull
    @Override
    public BrowseUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.browse_users_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrowseUsersAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userIntro.setText(user.getIntro());
        holder.userImage.setImageResource(user.getImageResId());

        // ⭐ userImage 클릭 이벤트 추가
        holder.userImage.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            // intent.putExtra("userId", user.getId()); // 필요하면 유저 ID 전달
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
