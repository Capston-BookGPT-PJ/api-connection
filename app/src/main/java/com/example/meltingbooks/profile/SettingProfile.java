package com.example.meltingbooks.profile;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide; // 이미지 로딩용 (Glide 추가 필요)
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.UpdateUserRequest;
import com.example.meltingbooks.network.UserResponse;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingProfile extends AppCompatActivity {

    private EditText etNickname, etUserId, etBio;
    private ImageView ivProfile;
    private TextView btnSave;
    private ImageButton btnProfileImage;
    private Uri selectedImageUri;  // 갤러리에서 선택한 이미지 Uri

    private UserResponse currentUser; // 기존 프로필 정보 저장용

    private ApiService apiService;
    private String token;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);

        // 상태바 색상 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // SharedPreferences에서 token, userId 가져오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("ProfileEdit", "토큰 또는 사용자 ID 없음");
            return;
        }

        // 뷰 초기화
        etNickname = findViewById(R.id.NicknameEditText);
        etUserId = findViewById(R.id.UserIDEditText);
        etBio = findViewById(R.id.BioEditText);
        ivProfile = findViewById(R.id.profile_imageView);
        btnSave = findViewById(R.id.bnt_setting_save);
        btnProfileImage = findViewById(R.id.bnt_ProfileImage);

        apiService = ApiClient.getClient(token).create(ApiService.class);

        // 기존 프로필 불러오기
        loadUserProfile();

        // 프로필 이미지 선택 버튼
        btnProfileImage.setOnClickListener(v -> {
            // TODO: 갤러리에서 이미지 가져오기 (startActivityForResult 등)
        });

        // 저장 버튼 클릭 이벤트
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserProfile() {
        Call<UserResponse> getCall = apiService.getUserProfile("Bearer " + token, userId);
        getCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();

                    // UI에 기존 값 반영
                    etNickname.setText(currentUser.getNickname());
                    etUserId.setText(String.valueOf(currentUser.getTagId()));
                    etBio.setText(currentUser.getBio());

                    // Glide로 프로필 이미지 표시
                    if (currentUser.getProfileImage() != null) {
                        Glide.with(SettingProfile.this)
                                .load(currentUser.getProfileImage())
                                .placeholder(R.drawable.sample_profile) // 기본 이미지
                                .into(ivProfile);
                    }
                } else {
                    Toast.makeText(SettingProfile.this, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("ProfileEdit", "프로필 불러오기 실패: " + t.getMessage());
            }
        });
    }
    private void saveProfileChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "기존 프로필 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 입력값 가져오기
        String nickname = etNickname.getText().toString().trim();
        String tagIdStr = etUserId.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // 비어있으면 기존 값 유지
        if (nickname.isEmpty()) nickname = currentUser.getNickname();
        if (tagIdStr.isEmpty()) tagIdStr = String.valueOf(currentUser.getTagId());
        if (bio.isEmpty()) bio = currentUser.getBio();

        String tagId = tagIdStr;

        // email(변경 금지), username, profileImage 는 기존 값 유지
        String email = currentUser.getEmail();
        String username = currentUser.getUsername();
        String profileImage = currentUser.getProfileImage();

        // JSON DTO 생성
        UpdateUserRequest request = new UpdateUserRequest(email ,nickname, username, bio, tagId, profileImage);

        // 여기서 JSON 찍기
        Gson gson = new Gson();
        Log.d("UpdateUserRequest", gson.toJson(request));


        // API 호출
        Call<UserResponse> call = apiService.updateUserProfile(
                "Bearer " + token,
                userId,
                request
        );

        //수정된 부분 다시 띄움
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SettingProfile.this, "프로필 수정 성공!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SettingProfile.this, "수정 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("ProfileEdit", "Response error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(SettingProfile.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}