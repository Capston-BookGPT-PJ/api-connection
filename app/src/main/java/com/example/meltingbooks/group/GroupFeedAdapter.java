package com.example.meltingbooks.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.group.LikedUsersBottomSheet;
import com.example.meltingbooks.group.comment.GroupCommentBottomSheet;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.feed.GroupFeedResponse;
import com.example.meltingbooks.profile.ProfileActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFeedAdapter extends RecyclerView.Adapter<GroupFeedAdapter.GroupFeedViewHolder> {

    private final List<GroupFeedItem> feedList;
    private final Context context;
    private final int groupId;
    private final ActivityResultLauncher<Intent> detailLauncher;


    public GroupFeedAdapter(Context context, List<GroupFeedItem> feedList, int groupId, ActivityResultLauncher<Intent> detailLauncher) {
        this.context = context;
        this.feedList = feedList;
        this.groupId = groupId;
        this.detailLauncher = detailLauncher;
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

        // 작성자 이름, 작성일, 본문 표시
        holder.userName.setText(item.getUserName());
        holder.writeDate.setText(item.getCreatedAt());
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());


        // 댓글 수 표시
        holder.commentCount.setText(String.valueOf(item.getCommentCount()));
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));

        // 좋아요 수 클릭 시
        holder.likeCount.setOnClickListener(v -> {
            // item은 GroupFeedResponse.Post 타입
            List<GroupFeedResponse.Post.LikedUser> likedUsers = item.getLikedUsers();

            if (likedUsers == null || likedUsers.isEmpty()) {
                Toast.makeText(context, "아직 좋아요한 사람이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // LikedUsersBottomSheet 호출 (likedUsers 전달)
            LikedUsersBottomSheet likedSheet = LikedUsersBottomSheet.newInstance(likedUsers);
            likedSheet.show(
                    ((AppCompatActivity) v.getContext()).getSupportFragmentManager(),
                    "LikedUsersBottomSheet"
            );
        });


        // 댓글 버튼(현재 feed용 나중에 group용으로 수정 필요)
        holder.commentButton.setOnClickListener(v -> {

            GroupCommentBottomSheet groupCommentBottomSheet =
                    GroupCommentBottomSheet.newInstance(groupId, item.getPostId(), "group");

            groupCommentBottomSheet.setOnCommentAddedListener(commentCount -> {
                holder.commentCount.setText(String.valueOf(commentCount));
            });

            groupCommentBottomSheet.show(
                    ((AppCompatActivity)v.getContext()).getSupportFragmentManager(),
                    "GroupCommentBottomSheet"
            );
        });

        // 초기 상태 (좋아요 여부에 따라 이미지 설정)
        holder.likeButton.setImageResource(
                item.isLikedByMe() ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));


        holder.likeButton.setOnClickListener(v -> {
            toggleLike(item, holder);
        });


        // 이미지 표시
        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            holder.groupImage.setVisibility(View.VISIBLE);
            List<String> images = item.getImageUrls();
            String latestImage = images.get(images.size() - 1); // ✅ 마지막 이미지 선택
            Glide.with(context)
                    .load(latestImage)
                    .centerCrop()
                    .into(holder.groupImage);
        } else {
            holder.groupImage.setVisibility(View.GONE);
        }



        // 프로필 표시
        if (item.getUserProfileImage() != null && !item.getUserProfileImage().isEmpty()) {
            holder.profileImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getUserProfileImage())
                    .placeholder(R.drawable.sample_profile) // 로딩 중 기본 이미지
                    .error(R.drawable.sample_profile)       // 실패 시 기본 이미지
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setVisibility(View.VISIBLE); // GONE 대신 보이게
            holder.profileImage.setImageResource(R.drawable.sample_profile2); // 기본 이미지 적용
        }

        // ⭐ 사용자 프로필 이동 클릭 리스너 추가
        View.OnClickListener profileClickListener = v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", item.getUserId());
            v.getContext().startActivity(intent);
        };

        // 프로필 이미지와 이름에 클릭 적용
        holder.profileImage.setOnClickListener(profileClickListener);
        holder.userName.setOnClickListener(profileClickListener);


        // 더보기 클릭
        holder.readMore.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GroupDetailActivity.class);
            intent.putExtra("groupFeedItem", item); // groupFeedItem 전달
            intent.putExtra("groupId", item.getGroupId());  // groupId 전달
            intent.putExtra("postId", item.getPostId());    // ✅ postId 추가
            context.startActivity(intent);
            // 로그로 확인
            Log.d("ReadMoreClick", "groupFeedItem postId=" + item.getPostId());
            // 로그 확인
            Log.d("ReadMoreClick", "groupFeedItem: " + item.toString());
            Log.d("ReadMoreClick", "groupId: " + item.getGroupId());
            Log.d("ReadMoreClick", "postId: " + item.getPostId()); // ✅ 로그 추가
        });


    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    static class GroupFeedViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, groupImage, commentButton, likeButton;
        TextView userName, writeDate, title, content, commentCount, likeCount;
        TextView readMore;

        public GroupFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            groupImage = itemView.findViewById(R.id.groupImage);
            userName = itemView.findViewById(R.id.userName);
            writeDate = itemView.findViewById(R.id.groupWriteDate);
            title = itemView.findViewById(R.id.groupWriteTitle);
            content = itemView.findViewById(R.id.groupWriteContent);
            commentButton = itemView.findViewById(R.id.chat_button);
            commentCount = itemView.findViewById(R.id.chat_count);

            likeButton = itemView.findViewById(R.id.like_button);
            likeCount = itemView.findViewById(R.id.like_count);

            //더보기
            readMore = itemView.findViewById(R.id.readMore);//더보기

        }
    }

    private void toggleLike(GroupFeedItem item, GroupFeedAdapter.GroupFeedViewHolder holder) {
        // SharedPreferences에서 토큰 가져오기
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // 서버 요청
        int groupId = item.getGroupId();   // ✅ 그룹 ID 가져오기
        int postId = item.getPostId();

        boolean oldState = item.isLikedByMe();   // ✅ 기존 상태
        int oldCount = item.getLikeCount();

        boolean newState = !oldState;
        item.setLikedByMe(newState);             // ✅ likedByMe 갱신


        // UI 즉시 반영 (optimistic update)
        int newCount = oldCount + (newState ? 1 : -1); // ✅ oldCount 기준
        item.setLikeCount(newCount);

        holder.likeButton.setImageResource(newState ? R.drawable.feed_like_full : R.drawable.feed_like_button);
        holder.likeCount.setText(String.valueOf(newCount));

        Log.d("LikeClick", "postId=" + postId + ", oldState=" + oldState + ", oldCount=" + oldCount + ", newState=" + newState + ", newCount=" + newCount);

        Call<ApiResponse<Void>> call = newState
                ? groupApi.likePost("Bearer " + token, groupId, postId)   // 좋아요
                : groupApi.unlikePost("Bearer " + token, groupId, postId); // 좋아요 취소

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Log.d("LikeResponse", "postId=" + postId + ", response=" + response);
                if (response.isSuccessful()) {
                    // HTTP 2xx → 성공, rollback 필요 없음
                    Log.d("LikeResponse", "postId=" + postId + ", responseCode=" + response.code());
                } else {
                    // 실패 → rollback
                    rollbackLike(holder, item, oldState, oldCount);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // 네트워크/통신 실패 → rollback
                rollbackLike(holder, item, oldState, oldCount);
                Log.e("LikeFail", "postId=" + postId + ", error=" + t.getMessage());
            }
        });

    }

    private void rollbackLike(GroupFeedAdapter.GroupFeedViewHolder holder, GroupFeedItem item, boolean oldState, int oldCount) {
        // 상태 복원
        item.setLikedByMe(oldState);
        item.setLikeCount(oldCount);

        // UI 복원
        holder.likeButton.setImageResource(oldState ? R.drawable.feed_like_full : R.drawable.feed_like_button);
        holder.likeCount.setText(String.valueOf(oldCount));

        Log.d("LikeRollback", "Restored postId=" + item.getPostId() + ", state=" + oldState + ", count=" + oldCount);
    }


}
