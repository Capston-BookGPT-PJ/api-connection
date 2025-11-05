package com.example.meltingbooks.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TokenManager {

    private final Context context;
    private static final String BASE_URL = "http://meltingbooks.o-r.kr:8080/"; // ← 실제 API URL로 변경

    public TokenManager(Context context) {
        this.context = context;
    }

    // ✅ 토큰 만료 여부 확인
    public static boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);
            long exp = json.getLong("exp");
            long now = System.currentTimeMillis() / 1000;
            return exp < now;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void refreshAccessToken(String token, String refreshToken, Context context, TokenRefreshCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/auth/token/refresh");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("refreshToken", refreshToken);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);

                    JSONObject res = new JSONObject(sb.toString());
                    String newAccess = res.getString("accessToken");
                    String newRefresh = res.getString("refreshToken");

                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt", newAccess)            // 기존 코드와 호환
                            .putString("refreshToken", newRefresh)
                            .apply();

                    callback.onResult(true);
                } else {
                    callback.onResult(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(false);
            }
        }).start();
    }

    // ✅ 콜백 인터페이스
    public interface TokenRefreshCallback {
        void onResult(boolean success);
    }
}
