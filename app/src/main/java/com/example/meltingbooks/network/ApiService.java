// Retrofit 인터페이스
package com.example.meltingbooks.network;

import com.example.meltingbooks.network.feed.CommentRequest;
import com.example.meltingbooks.network.feed.CommentResponse;
import com.example.meltingbooks.network.feed.FeedPageResponse;
import com.example.meltingbooks.network.feed.ReviewRequest;
import com.example.meltingbooks.network.feed.ReviewResponse;
import com.example.meltingbooks.network.feed.UserReviewResponse;
import com.example.meltingbooks.network.profile.UpdateUserRequest;
import com.example.meltingbooks.network.profile.UserResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // 사용자 프로필 조회 API
    @GET("/api/users/{id}")
    Call<UserResponse> getUser(
            @Header("Authorization") String token, // 헤더에 Bearer 토큰 추가
            @Path("id") int userId               // URL의 {id} 부분

    );

    // 사용자 프로필 조회 API
    @GET("/api/profile/{userId}")
    Call<ApiResponse<UserResponse>> getUserProfile(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );

    // 사용자 프로필 수정 API
    @PUT("/api/profile/{userId}")
    Call<UserResponse> updateUserProfile(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Body UpdateUserRequest request
    );

    //사용자 별 리뷰 조회 API
    @GET("/api/reviews/user/{userId}")
    Call<ApiResponse<List<UserReviewResponse>>> getUserReviews(
            @Path("userId") int userId,
            @Header("Authorization") String token
    );

    //프로필 이미지 업로드
    @Multipart
    @POST("api/users/{userId}/profile-image")
    Call<ApiResponse<String>> uploadProfileImage(
            @Header("Authorization") String token,
            @Path("userId") int userId,
            @Part MultipartBody.Part file
    );

    //피드 조회 (페이징 방식)
    @GET("/api/feeds")
    Call<ApiResponse<FeedPageResponse>> getUserFeeds(
            @Header("Authorization") String token,
            @Query("userId") int userId,
            @Query("page") int page,
            @Query("size") int size
    );

    // 댓글 조회
    @GET("/api/comments/review/{reviewId}")
    Call<ApiResponse<List<CommentResponse>>> getComments(
            @Header("Authorization") String token,
            @Path("reviewId") int reviewId
    );

    @POST("/api/comments")
    Call<ApiResponse<CommentResponse>> postComment(
            @Header("Authorization") String token,
            @Query("userId") int userId,
            @Query("reviewId") int reviewId,
            @Body CommentRequest commentRequest
    );

    // 리뷰 작성
    @POST("/api/reviews")
    Call<ApiResponse<ReviewResponse>> createReview(
            @Header("Authorization") String token,
            @Query("userId") int userId,
            @Body ReviewRequest reviewRequest
    );

    // 리뷰 이미지 업로드
    @Multipart
    @POST("/api/reviews/{reviewId}/review-images")
    Call<ApiResponse<List<String>>> uploadReviewImage(
            @Header("Authorization") String token,
            @Path("reviewId") int reviewId,
            @Part MultipartBody.Part file
    );

    //리뷰 좋아요
    @POST("/api/likes/reviews/{reviewId}")
    Call<ApiResponse<Void>> likeReview(
            @Header("Authorization") String token,
            @Path("reviewId") int reviewId
    );


    // 좋아요 취소
    @DELETE("/api/likes/reviews/{reviewId}")
    Call<ApiResponse<Void>> unlikeReview(
            @Header("Authorization") String token,
            @Path("reviewId") int reviewId
    );
}