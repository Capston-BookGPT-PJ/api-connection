package com.example.meltingbooks.network.group;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroupApi {
    //그룹 생성
    @POST("/api/groups")
    Call<GroupSingleList> createGroup(@Body Group group);

    // 그룹 수정
    @PUT("/api/groups/{groupId}") // 서버가 PUT을 지원하면 PUT으로 바꾸세요
    Call<GroupSingleList> updateGroup(
            @Path("groupId") int groupId,
            @Body Group group
    );

    // 그룹 삭제
    @DELETE("/api/groups/{groupId}")
    Call<Void> deleteGroup(@Path("groupId") int groupId);

    //그룹 검색
    @GET("/api/groups/search")
    Call<GroupAllList> searchGroups(
            @Query("keyword") String keyword,
            @Query("category") String category
    );

    //그룹 id 받기
    @GET("/api/groups/{id}")
    Call<GroupSingleList> getGroupById(@Path("id") int groupId);


    //그룹 가입
    @POST("/api/groups/{groupId}/join")
    Call<Void> joinGroup(@Path("groupId") int groupId);

    //그룹 탈퇴
    @DELETE("/api/groups/{groupId}/leave")
    Call<Void> leaveGroup(@Path("groupId") int groupId);
}
