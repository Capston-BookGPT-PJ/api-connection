package com.example.meltingbooks.network.book;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.meltingbooks.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class BookController {

    private static final String TAG = "BookController";
    private BookApi bookApi;

    public BookController(Context context) {
        // SharedPreferences에서 토큰 가져오기
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt", null);

        if (token == null) {
            Log.e(TAG, "JWT 토큰이 없습니다.");
            return;
        }

        bookApi = ApiClient.getClient(token).create(BookApi.class);
    }

    // 전체 책 리스트 가져오기
    public void fetchBooks(Callback<List<Book>> callback) {
        if (bookApi == null) return;

        Call<List<Book>> call = bookApi.getAllBooks();
        call.enqueue(callback);
    }

    // 책 검색
    public void searchBooks(String keyword, Callback<List<Book>> callback) {
        if (bookApi == null) return;

        Call<List<Book>> call = bookApi.searchBooks(keyword);
        call.enqueue(callback);
    }

    // bookId로 책 상세 조회
    /**public void getBookDetail(String token, int bookId, Callback<Book> callback) {
        if (bookApi == null) return;

        Call<Book> call = bookApi.getBookDetail("Bearer " + token, bookId);
        call.enqueue(callback);
    }*/

    // bookId로 책 상세 조회
    public void getBookDetail(int bookId, Callback<Book> callback) {
        if (bookApi == null) return;

        Call<Book> call = bookApi.getBookDetail(null, bookId);
        call.enqueue(callback);
    }

}