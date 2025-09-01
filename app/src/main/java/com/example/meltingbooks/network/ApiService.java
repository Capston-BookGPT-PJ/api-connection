// Retrofit 인터페이스
package com.example.meltingbooks.network;

import com.example.meltingbooks.network.UserResponse;
import com.example.meltingbooks.network.UpdateUserRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    // 사용자 프로필 조회 API
    @GET("/api/users/{id}")
    Call<UserResponse> getUserProfile(
            @Header("Authorization") String token, // 헤더에 Bearer 토큰 추가
            @Path("id") int userId               // URL의 {id} 부분

    );

    // 사용자 프로필 수정 API
    @PUT("/api/users/{id}")
    Call<UserResponse> updateUserProfile(
            @Header("Authorization") String token,
            @Path("id") int userId,
            @Body UpdateUserRequest request
    );


}