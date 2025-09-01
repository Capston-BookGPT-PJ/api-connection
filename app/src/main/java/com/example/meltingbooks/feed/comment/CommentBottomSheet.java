package com.example.meltingbooks.feed.comment;

import android.app.Dialog;
import android.os.Bundle;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private CommentAdapter commentAdapter;
    private List<CommentItem> commentList;
    private OnCommentAddedListener onCommentAddedListener;

    //feed와 group 구분 코드

    private String postId;
    private String postType;
    public interface OnCommentAddedListener {
        void onCommentAdded(int commentCount);
    }

    public void setOnCommentAddedListener(OnCommentAddedListener listener) {
        this.onCommentAddedListener = listener;
    }


    @Override // 댓글 화면 초기 설정을 위함
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // 다이얼로그가 풀스크린으로 보이도록 설정
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // 높이를 부모 뷰의 전체 높이로 설정하여 상단 고정
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());
            }
        });

        return dialog;
    }

    //GroupAdapter 부분
    public static CommentBottomSheet newInstance(String postId, String postType) {
        CommentBottomSheet fragment = new CommentBottomSheet();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("postType", postType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            postType = getArguments().getString("postType");
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            // 배경을 투명하게 설정
            getDialog().getWindow().setDimAmount(0f);
        }

        View view = inflater.inflate(R.layout.comment_bottom_sheet, container, false);

        RecyclerView commentRecyclerView = view.findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), commentList);
        commentRecyclerView.setAdapter(commentAdapter);

        // 테스트용 데이터 추가 나중에 서버 연결 필요.
        commentList.add(new CommentItem("User1", "멋진 리뷰네요!---------------------------------------------------------------------------------------------------------------", R.drawable.sample_profile));
        commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User1", "멋진 리뷰네요!", R.drawable.sample_profile));
        /*commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User1", "멋진 리뷰네요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User1", "멋진 리뷰네요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User1", "멋진 리뷰네요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User1", "멋진 리뷰네요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User1", "멋진 리뷰네요!", R.drawable.sample_profile));
        commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));*/
        commentAdapter.notifyDataSetChanged();

        // 댓글 입력 부분 설정
        EditText commentEditText = view.findViewById(R.id.commentEditText);
        ImageView postCommentButton = view.findViewById(R.id.postCommentButton);

        postCommentButton.setOnClickListener(v -> {
            String comment = commentEditText.getText().toString().trim();
            if (!comment.isEmpty()) {
                // 새 댓글을 리스트에 추가
                commentList.add(new CommentItem("CurrentUser", comment, R.drawable.sample_profile));
                commentAdapter.notifyDataSetChanged();
                commentEditText.setText("");

                // 콜백 호출하여 댓글 수 업데이트 나중에 서버와 통신하도록 수정
                if (onCommentAddedListener != null) {
                    onCommentAddedListener.onCommentAdded(commentList.size());
                }
            }
        });

        return view;
    }


}

