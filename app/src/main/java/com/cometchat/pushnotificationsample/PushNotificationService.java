package com.cometchat.pushnotificationsample;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Arrays;

public class PushNotificationService extends FirebaseMessagingService {
    CometChatNotification cometChatNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        cometChatNotification = CometChatNotification.getInstance(this);
    }

    final String TAG = PushNotificationService.class.getSimpleName();
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(cometChatNotification.isCometChatNotification(remoteMessage)){
            cometChatNotification.renderCometChatNotification(remoteMessage, new CometChat.CallbackListener<String>() {
                @Override
                public void onSuccess(String s) {

                }

                @Override
                public void onError(CometChatException e) {

                }
            });
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e(TAG,"onNewToken : "+token);
    }
}
