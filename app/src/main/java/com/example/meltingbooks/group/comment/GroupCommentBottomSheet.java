package com.example.meltingbooks.group.comment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.comment.GroupCommentPageResponse;
import com.example.meltingbooks.network.group.comment.GroupCommentRequest;
import com.example.meltingbooks.network.group.comment.GroupCommentResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupCommentBottomSheet extends BottomSheetDialogFragment {

    private GroupCommentAdapter commentAdapter;
    private List<GroupCommentItem> commentList;
    private OnCommentAddedListener onCommentAddedListener;

    private int postId;
    private int groupId;
    private String postType;

    public interface OnCommentAddedListener {
        void onCommentAdded(int commentCount);
    }

    public void setOnCommentAddedListener(OnCommentAddedListener listener) {
        this.onCommentAddedListener = listener;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());
            }
        });

        return dialog;
    }

    public static GroupCommentBottomSheet newInstance(int groupId, int postId, String postType) {
        GroupCommentBottomSheet fragment = new GroupCommentBottomSheet();
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        args.putInt("postId", postId);
        args.putString("postType", postType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getInt("groupId");
            postId = getArguments().getInt("postId");
            postType = getArguments().getString("postType");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setDimAmount(0f);
        }

        View view = inflater.inflate(R.layout.comment_bottom_sheet, container, false);

        RecyclerView commentRecyclerView = view.findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        commentList = new ArrayList<>();
        commentAdapter = new GroupCommentAdapter(getContext(), commentList);
        commentRecyclerView.setAdapter(commentAdapter);

        if ("group".equals(postType)) {
            loadCommentsFromServer();
        } else {}
        // 댓글 입력 부분 설정
        EditText commentEditText = view.findViewById(R.id.commentEditText);
        ImageView postCommentButton = view.findViewById(R.id.postCommentButton);

        // ✅ 댓글 등록 처리
        postCommentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                SharedPreferences prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
                String token = prefs.getString("jwt", null);
                int userId = prefs.getInt("userId", -1); // 기본값 -1
                if (token == null) return;

                //ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
                GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

                GroupCommentRequest request = new GroupCommentRequest(commentText);

                groupApi.createGroupComment("Bearer " + token, groupId, postId, request)
                        .enqueue(new Callback<ApiResponse<GroupCommentResponse>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<GroupCommentResponse>> call, Response<ApiResponse<GroupCommentResponse>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    GroupCommentResponse newComment = response.body().getData();

                                    commentList.add(new GroupCommentItem(
                                            newComment.getNickname(),
                                            newComment.getContent(),
                                            newComment.getProfileImageUrl(),
                                            newComment.getFormattedCreatedAt()
                                    ));
                                    commentAdapter.notifyItemInserted(commentList.size() - 1);
                                    commentRecyclerView.scrollToPosition(commentList.size() - 1); // ✅ 맨 아래로 스크롤
                                    commentEditText.setText("");

                                    if (onCommentAddedListener != null) {
                                        onCommentAddedListener.onCommentAdded(commentList.size());
                                    }
                                } else {
                                    Log.e("GroupComment", "댓글 등록 실패: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<GroupCommentResponse>> call, Throwable t) {
                                Log.e("GroupComment", "댓글 등록 에러: " + t.getMessage());
                            }
                        });
            }
        });

        commentAdapter.notifyDataSetChanged();
        return view;
    }

    private void loadCommentsFromServer() {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        //ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

        groupApi.getGroupComments("Bearer " + token, groupId, postId, 0, 20)
                .enqueue(new Callback<ApiResponse<GroupCommentPageResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupCommentPageResponse>> call, Response<ApiResponse<GroupCommentPageResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            commentList.clear();

                            for (GroupCommentResponse comment : response.body().getData().getContent()) {
                                GroupCommentItem item = new GroupCommentItem(
                                        comment.getNickname(),
                                        comment.getContent(),
                                        comment.getProfileImageUrl(),
                                        comment.getFormattedCreatedAt() // 서버에서 내려주는 필드 사용
                                );
                                commentList.add(item);

                            }

                            commentAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("GroupComment", "댓글 불러오기 실패: " + (response.body() != null ? response.body().getError() : "null"));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GroupCommentPageResponse>> call, Throwable t) {
                        Log.e("GroupComment", "네트워크 오류: " + t.getMessage());
                    }
                });
    }


}
