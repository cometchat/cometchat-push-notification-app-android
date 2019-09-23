package com.inscripts.cometchatpro;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import static com.inscripts.cometchatpro.Constant.APP_ID;
import static com.inscripts.cometchatpro.Constant.CHANNEL_ID;

public class CometChatPro extends Application {

    private static final String TAG = "CometChatPro";

    @Override
    public void onCreate() {
        super.onCreate();

        AppSettings appSettings= new AppSettings.AppSettingsBuilder().setRegion(Constant.REGION).build();

        CometChat.init(this, APP_ID,appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(CometChatPro.this, s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: "+e.getMessage());

            }
        });

        FirebaseOptions.Builder builder = new FirebaseOptions.Builder()
                .setApplicationId(Constant.FCM_APPLICATION_ID)
                .setApiKey(Constant.FCM_WEB_API_KEY);
        FirebaseApp.initializeApp(this, builder.build());

        createNotificationChannel();

    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
