package com.cometchat.pro.android.pushnotification.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cometchat.pro.android.pushnotification.R;
import com.cometchat.pro.android.pushnotification.UIKitApplication;
import com.cometchat.pro.android.pushnotification.constants.AppConfig;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatCallActivity;
import com.cometchat.pro.uikit.ui_components.calls.callconnection.CallManager;
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseService";
    private JSONObject json;
    private Intent intent;
    private int count = 0;
    private Call call;
    public static String token;
    private static final int REQUEST_CODE = 12;

    private boolean isCall;
    CallManager callManager;


    public static void subscribeUserNotification(String UID) {
        FirebaseMessaging.getInstance().subscribeToTopic(AppConfig.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_USER + "_" + UID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, UID + " Subscribed Success");
            }
        });
    }

    public static void unsubscribeUserNotification(String UID) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(AppConfig.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_USER + "_" + UID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, UID + " Unsubscribed Success");
            }
        });
    }

    public static void subscribeGroupNotification(String GUID) {
        FirebaseMessaging.getInstance().subscribeToTopic(AppConfig.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_GROUP + "_" + GUID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, GUID + " Subscribed Success");
            }
        });
    }

    public static void unsubscribeGroupNotification(String GUID) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(AppConfig.AppDetails.APP_ID + "_" + CometChatConstants.RECEIVER_TYPE_GROUP + "_" + GUID);
    }

    @Override
    public void onNewToken(String s) {
        token = s;
        Log.d(TAG, "onNewToken: " + s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            count++;
            json = new JSONObject(remoteMessage.getData());
            Log.d(TAG, "JSONObject: " + json.toString());
            JSONObject messageData = new JSONObject(json.getString("message"));
            BaseMessage baseMessage = CometChatHelper.processMessage(new JSONObject(remoteMessage.getData().get("message")));
            if (baseMessage instanceof Call) {
                call = (Call) baseMessage;
                isCall = true;
            }

            showNotifcation(baseMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFromURL(String strURL) {
        if (strURL != null) {
            try {
                URL url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private void showNotifcation(BaseMessage baseMessage) {

        try {
            int m = (int) ((new Date().getTime()));
            String GROUP_ID = "group_id";
            if (!CometChat.isInitialized()) {
                AppSettings appSettings = new AppSettings.AppSettingsBuilder().autoEstablishSocketConnection(false).setRegion(AppConfig.AppDetails.REGION).build();
                CometChat.init(this, AppConfig.AppDetails.APP_ID, appSettings, new CometChat.CallbackListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        CometChat.setSource("push-notification", "android", "java");
                        Log.d(TAG, "onSuccess: " + s);
                    }

                    @Override
                    public void onError(CometChatException e) {
                        e.getMessage();
                    }
                });
            }
            Intent messageIntent = new Intent(getApplicationContext(), CometChatMessageListActivity.class);
            messageIntent.putExtra(UIKitConstants.IntentStrings.TYPE, baseMessage.getReceiverType());
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                messageIntent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage.getSender().getName());
                messageIntent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage.getSender().getUid());
                messageIntent.putExtra(UIKitConstants.IntentStrings.AVATAR, baseMessage.getSender().getAvatar());
                messageIntent.putExtra(UIKitConstants.IntentStrings.STATUS, baseMessage.getSender().getStatus());
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                messageIntent.putExtra(UIKitConstants.IntentStrings.GUID, ((Group) baseMessage.getReceiver()).getGuid());
                messageIntent.putExtra(UIKitConstants.IntentStrings.NAME, ((Group) baseMessage.getReceiver()).getName());
                messageIntent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, ((Group) baseMessage.getReceiver()).getDescription());
                messageIntent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, ((Group) baseMessage.getReceiver()).getGroupType());
                messageIntent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, ((Group) baseMessage.getReceiver()).getOwner());
                messageIntent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, ((Group) baseMessage.getReceiver()).getMembersCount());
            }
            PendingIntent messagePendingIntent = PendingIntent.getActivity(getApplicationContext(), 0123, messageIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "2").setSmallIcon(R.drawable.cc).setContentTitle(json.getString("title")).setContentText(json.getString("alert")).setColor(getResources().getColor(R.color.colorPrimary)).setLargeIcon(getBitmapFromURL(baseMessage.getSender().getAvatar())).setGroup(GROUP_ID).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            if (baseMessage.getType().equals(CometChatConstants.MESSAGE_TYPE_IMAGE)) {
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(((MediaMessage) baseMessage).getAttachment().getFileUrl())));
            }
            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(this, "2").setContentTitle("CometChat").setContentText(count + " messages").setSmallIcon(R.drawable.cc).setGroup(GROUP_ID).setGroupSummary(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            if (isCall && json.getString("alert").contains("Incoming")) {
                if (!UIKitApplication.isForeground) {
                    if (((System.currentTimeMillis() / 1000) - call.getInitiatedAt()) < 45)
                        initiateCallService(call);
                }
            } else if (isCall && json.getString("alert").contains("Missed")) {
                if (!UIKitApplication.isForeground) endCallService();
                else {
                    if (CometChatCallActivity.callActivity != null) {
                        CometChatCallActivity.callActivity.finish();
                    }
                }
                showNotification(builder, summaryBuilder, messagePendingIntent, notificationManager, baseMessage);
            } else {
//                Person person = createPerson(baseMessage);
//                builder.setStyle(new NotificationCompat.MessagingStyle(person)
//                        .setGroupConversation(true)
//                        .setConversationTitle(json.getString("title"))
//                        .addMessage(json.getString("alert"),
//                                currentTimeMillis(), person));
                showNotification(builder, summaryBuilder, messagePendingIntent, notificationManager, baseMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showNotification(NotificationCompat.Builder builder, NotificationCompat.Builder summaryBuilder, PendingIntent messagePendingIntent, NotificationManagerCompat notificationManager, BaseMessage baseMessage) {
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentIntent(messagePendingIntent);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
//                Uri notification = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.incoming_message);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        notificationManager.notify(baseMessage.getId(), builder.build());
        notificationManager.notify(0, summaryBuilder.build());
    }

    private void endCallService() {
        if (callManager == null) callManager = new CallManager(getApplicationContext());
        callManager.endCall();
    }

    private void initiateCallService(Call call) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("initiateCallService: ", call.toString());
                callManager = new CallManager(getApplicationContext());
                callManager.startIncomingCall(call);
            }
        } catch (Exception e) {
            Log.e("initiateCallError:", "${e.message}");
            Toast.makeText(getApplicationContext(), "Unable to receive call due to " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }
}
