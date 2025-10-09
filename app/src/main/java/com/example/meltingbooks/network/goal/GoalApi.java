package com.example.meltingbooks.network.goal;

import com.example.meltingbooks.network.ApiResponse;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;
public interface GoalApi{
    @POST("/api/goals")
    Call<GoalResponse> createGoal(@Body GoalRequest request);

    @GET("/api/goals/me")
    Call<ApiResponse<List<GoalResponse>>> getGoals();

    @PUT("/api/goals/{goalId}")
    Call<GoalResponse> updateGoal(@Path("goalId") int goalId, @Body GoalRequest request);

    @DELETE("/api/goals/{goalId}")
    Call<Void> deleteGoal(@Path("goalId") int goalId);
}