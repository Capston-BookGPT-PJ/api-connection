package com.example.meltingbooks.network.book;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookApi {
    @GET("/api/books")
    Call<List<Book>> getAllBooks();

    @GET("/api/books/search")
    Call<List<Book>> searchBooks(@Query("keyword") String keyword);

    //bookId로 책 정보 조회
    @GET("/api/books/{bookId}")
    Call<Book> getBookDetail(@Header("Authorization") String token,
                             @Path("bookId") int bookId);

    //책 생성
    @POST("/api/books")
    Call<Book> createBook(@Header("Authorization") String token,
                          @Body BookCreateRequest bookRequest);
}
