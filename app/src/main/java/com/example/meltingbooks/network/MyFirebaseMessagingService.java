package com.example.meltingbooks.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "Refreshed token: " + token);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit().putString("fcmToken", token).apply();

        int userId = prefs.getInt("userId", -1);
        String jwt = prefs.getString("jwt", null);

        if (userId != -1 && jwt != null) {
            sendTokenToServer(userId, jwt, token);
        } else {
            Log.d("FCM", "No logged-in user to update token on server yet");
        }
    }

    private void sendTokenToServer(int userId, String jwt, String fcmToken) {
        ApiService apiService = ApiClient.getClient(jwt).create(ApiService.class);
        TokenRequestBody body = new TokenRequestBody(fcmToken, Build.MODEL);

        Call<Void> call = apiService.registerToken("Bearer " + jwt, userId, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("FCM", response.isSuccessful() ? "Token updated on server" :
                        "Token update failed: " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w("FCM", "Token update failed", t);
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM", "Message received: " + remoteMessage.getData().toString());

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if (title != null && body != null) {
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "meltingbooks_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "MeltingBooks 알림",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
