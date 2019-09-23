package com.inscripts.cometchatpro;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.pushnotifications.core.PNExtension;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import static com.inscripts.cometchatpro.Constant.APP_ID;


public class MyFirebaseService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseService";

    private static final String CHANNEL_ID = "2";

    private JSONObject json;

    private static final int REQUEST_CODE = 12;

    private boolean isCall;


    public static void subscribeUser(String UID) {
        FirebaseMessaging.getInstance().subscribeToTopic(APP_ID + "_"+ CometChatConstants.RECEIVER_TYPE_USER +"_" +
                UID);
    }

    public static void subscribeGroup(String GUID){
        FirebaseMessaging.getInstance().subscribeToTopic(APP_ID+"_"+CometChatConstants.RECEIVER_TYPE_GROUP+"_"+GUID);
    }

    @Override
    public void onNewToken(String s) {
        Log.d(TAG, "onNewToken: "+s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getData());

        try {
            json = new JSONObject(remoteMessage.getData());
            Log.d(TAG, "JSONObject: "+json.toString());
            JSONObject messageData = new JSONObject(json.getString("message"));

//            PNExtension.getMessageFromJson(new JSONObject(remoteMessage.getData().get("message")), new CometChat.CallbackListener<BaseMessage>() {
//                @Override
//                public void onSuccess(BaseMessage baseMessage) {
//
//
//                }
//                @Override
//                public void onError(CometChatException e) {
//                    Log.d(TAG, "onError: "+e.getMessage());
//                }
//            });

            BaseMessage baseMessage=CometChatHelper.processMessage(new JSONObject(remoteMessage.getData().get("message")));
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (baseMessage instanceof Call){
                isCall=true;
            }
            showNotifcation(pendingIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showNotifcation(PendingIntent pendingIntent) {

        try {

            String GROUP_ID = "group_id";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.badge)
                    .setContentTitle(json.getString("title"))
                    .setContentText(json.getString("alert"))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setGroup(GROUP_ID)
                    .setGroupSummary(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setAutoCancel(true);

            if (isCall){
                builder.addAction(0,"Answers",pendingIntent);
                builder.addAction(0,"Decline",pendingIntent);
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(2, builder.build());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
