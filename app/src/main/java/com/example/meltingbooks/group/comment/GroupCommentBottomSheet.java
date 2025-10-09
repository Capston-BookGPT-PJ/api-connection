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
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.CommentRequest;
import com.example.meltingbooks.network.feed.CommentResponse;
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

    public static GroupCommentBottomSheet newInstance(int postId, String postType) {
        GroupCommentBottomSheet fragment = new GroupCommentBottomSheet();
        Bundle args = new Bundle();
        args.putInt("postId", postId);
        args.putString("postType", postType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
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

        if ("feed".equals(postType)) {
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

                ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

                CommentRequest request = new CommentRequest(commentText);

                apiService.postComment("Bearer " + token, userId, postId, request)
                        .enqueue(new Callback<ApiResponse<CommentResponse>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<CommentResponse>> call, Response<ApiResponse<CommentResponse>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    CommentResponse newComment = response.body().getData();

                                    // 리스트에 추가 (GroupCommentItem 으로 변환)
                                    commentList.add(new GroupCommentItem(
                                            newComment.getNickname(),
                                            newComment.getContent(),
                                            newComment.getFormattedCreatedAt(),
                                            newComment.getUserProfileImage()
                                    ));
                                    commentAdapter.notifyItemInserted(commentList.size() - 1);

                                    // 입력창 비우기
                                    commentEditText.setText("");

                                    // 콜백 호출 (댓글 수 갱신)
                                    if (onCommentAddedListener != null) {
                                        onCommentAddedListener.onCommentAdded(commentList.size());
                                    }
                                } else {
                                    Log.e("Comment", "댓글 등록 실패: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                                Log.e("Comment", "댓글 등록 에러: " + t.getMessage());
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

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        apiService.getComments("Bearer " + token, postId)
                .enqueue(new Callback<ApiResponse<List<CommentResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<CommentResponse>>> call, Response<ApiResponse<List<CommentResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            commentList.clear();

                            for (CommentResponse comment : response.body().getData()) {
                                // 서버에서 내려주는 authorName, content, createdAt, profileImageUrl 사용
                                GroupCommentItem item = new GroupCommentItem(
                                        comment.getNickname(),
                                        comment.getContent(),
                                        comment.getFormattedCreatedAt(),
                                        comment.getUserProfileImage() // 서버에서 내려주면
                                );
                                commentList.add(item);
                            }

                            commentAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("GroupComment", "댓글 불러오기 실패: " + (response.body() != null ? response.body().getError() : "null"));
                        }

                    }

            @Override
            public void onFailure(Call<ApiResponse<List<CommentResponse>>> call, Throwable t) {
                Log.e("GroupComment", "네트워크 오류: " + t.getMessage());
            }
        });
    }

}
