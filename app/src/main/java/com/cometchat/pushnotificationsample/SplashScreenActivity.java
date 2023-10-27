package com.cometchat.pushnotificationsample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.helpers.Logger;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings;

public class SplashScreenActivity extends AppCompatActivity {

    private final String TAG = SplashScreenActivity.class.getSimpleName();
    LocalBroadcastManager localBroadcastManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initCometChatUIKit();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(callEventReceiver, new IntentFilter("CometChat_Call_Event"));

    }

    private void initCometChatUIKit(){
        UIKitSettings uiKitSettings = new UIKitSettings.UIKitSettingsBuilder()
                .setRegion(AppConfig.AppDetails.REGION)
                .setAppId(AppConfig.AppDetails.APP_ID)
                .setAuthKey(AppConfig.AppDetails.AUTH_KEY)
                .subscribePresenceForAllUsers().build();

        CometChatUIKit.init(this, uiKitSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successString) {
                Log.e(TAG, "onSuccess: CometChatUIKit init");
                if (CometChatUIKit.getLoggedInUser() != null){
                    startActivity(new Intent(SplashScreenActivity.this, HomeScreenActivity.class));
                    //finish();
                }else {
                    Log.e(TAG, "User not logged in");
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: CometChatUIKit init");
            }
        });
    }

    private BroadcastReceiver callEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: Call Broadcast received");
        }
    };
}