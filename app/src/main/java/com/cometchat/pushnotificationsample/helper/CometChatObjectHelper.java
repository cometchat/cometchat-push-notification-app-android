package com.cometchat.pushnotificationsample.helper;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.cometchat.chat.helpers.CometChatHelper;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.google.gson.JsonObject;

import org.json.JSONObject;

public class CometChatObjectHelper {

    public static void process(Intent intent, final CometChatObjectCallback listener){
        try {
            Log.e("CometChatObjectHelper", "===>>>: process: 1: " + intent);
            String notificationPayload = intent.getStringExtra("notification_payload");
            if(TextUtils.isEmpty(notificationPayload)){
                listener.onNoMessage();
            }else{
                JSONObject jsonObject = new JSONObject(notificationPayload);
                Log.e("CometChatObjectHelper", "===>>>: process: 2: " + notificationPayload);
                if (jsonObject.has("uid")){
                    User user = User.fromJson(notificationPayload);
                    listener.onUserMessage(user);
                } else if (jsonObject.has("guid")){
                    Group group = Group.fromJson(notificationPayload);
                    listener.onGroupMessage(group);
                }
            }
        }catch (Exception e){
            Log.e("CometChatObjectHelper", "===>>>: Error: " + e);
        }
    }

}
