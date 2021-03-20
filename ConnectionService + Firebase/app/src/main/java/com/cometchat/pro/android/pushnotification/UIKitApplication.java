package com.cometchat.pro.android.pushnotification;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.android.pushnotification.constants.AppConfig;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.listener.CometChatCallListener;
import com.cometchat.pro.uikit.ui_components.calls.callconnection.MyConnectionService;

public class UIKitApplication extends Application implements LifecycleObserver {

    private static final String TAG = "UIKitApplication";

    public static boolean wasInBackground;
    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings appSettings = new AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(AppConfig.AppDetails.REGION).build();
        CometChat.init(this, AppConfig.AppDetails.APP_ID, appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                CometChat.setSource("push-notification","android","java");
                Log.d(TAG, "onSuccess: "+s);
            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(UIKitApplication.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        wasInBackground = !ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
        CometChatCallListener.addCallListener(TAG,this);
        createNotificationChannel();
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("2", name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        Log.e(TAG, "onMoveToForeground: " );
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        Log.e(TAG, "onMoveToBackground: " );
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
