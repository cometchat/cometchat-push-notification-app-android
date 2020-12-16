package utils.firebaseUtils;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.uikit.R;
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

import constant.StringContract;
import screen.messagelist.CometChatMessageListActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseService";
    private JSONObject json;
    private Intent intent;
    private int count=0;
    private Call call;
    public static String token;
    private static final int REQUEST_CODE = 12;

    private boolean isCall;

    public static void subscribeUserNotification(String UID) {
        FirebaseMessaging.getInstance().subscribeToTopic(StringContract.AppInfo.APP_ID + "_"+ CometChatConstants.RECEIVER_TYPE_USER +"_" +
                UID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, UID+ " Subscribed Success");
            }
        });
    }

    public static void unsubscribeUserNotification(String UID) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(StringContract.AppInfo.APP_ID + "_"+ CometChatConstants.RECEIVER_TYPE_USER +"_" +
                UID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, UID+ " Unsubscribed Success");
            }
        });
    }

    public static void subscribeGroupNotification(String GUID) {
        FirebaseMessaging.getInstance().subscribeToTopic(StringContract.AppInfo.APP_ID + "_"+ CometChatConstants.RECEIVER_TYPE_GROUP +"_" +
                GUID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG, GUID+ " Subscribed Success");
            }
        });
    }

    public static void unsubscribeGroupNotification(String GUID) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(StringContract.AppInfo.APP_ID + "_"+ CometChatConstants.RECEIVER_TYPE_GROUP +"_" +
                GUID);
    }

    @Override
    public void onNewToken(String s) {
        token  = s;
        Log.d(TAG, "onNewToken: "+s);
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            count++;
            json = new JSONObject(remoteMessage.getData());
            Log.d(TAG, "JSONObject: "+json.toString());
            JSONObject messageData = new JSONObject(json.getString("message"));
            BaseMessage baseMessage = CometChatHelper.processMessage(new JSONObject(remoteMessage.getData().get("message")));
            if (baseMessage instanceof Call){
                call = (Call)baseMessage;
                isCall=true;
            }
            showNotifcation(baseMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFromURL(String strURL) {
        if (strURL!=null) {
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

            Intent messageIntent = new Intent(getApplicationContext(), CometChatMessageListActivity.class);
            messageIntent.putExtra(StringContract.IntentStrings.TYPE,baseMessage.getReceiverType());
            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                messageIntent.putExtra(StringContract.IntentStrings.UID,baseMessage.getSender().getUid());
                messageIntent.putExtra(StringContract.IntentStrings.NAME,baseMessage.getSender().getName());
                messageIntent.putExtra(StringContract.IntentStrings.AVATAR,baseMessage.getSender().getAvatar());
                messageIntent.putExtra(StringContract.IntentStrings.STATUS,baseMessage.getSender().getStatus());
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                messageIntent.putExtra(StringContract.IntentStrings.GUID,((Group)baseMessage.getReceiver()).getGuid());
                messageIntent.putExtra(StringContract.IntentStrings.NAME,((Group)baseMessage.getReceiver()).getName());
                messageIntent.putExtra(StringContract.IntentStrings.AVATAR,((Group)baseMessage.getReceiver()).getIcon());
                messageIntent.putExtra(StringContract.IntentStrings.MEMBER_COUNT,((Group)baseMessage.getReceiver()).getMembersCount());
                messageIntent.putExtra(StringContract.IntentStrings.GROUP_DESC,((Group)baseMessage.getReceiver()).getDescription());
                messageIntent.putExtra(StringContract.IntentStrings.GROUP_TYPE,((Group)baseMessage.getReceiver()).getGroupType());
                messageIntent.putExtra(StringContract.IntentStrings.GROUP_OWNER,((Group)baseMessage.getReceiver()).getOwner());
                messageIntent.putExtra(StringContract.IntentStrings.GROUP_PASSWORD,((Group)baseMessage.getReceiver()).getPassword());
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"2")
                    .setSmallIcon(R.drawable.cc)
                    .setContentTitle(json.getString("title"))
                    .setContentText(json.getString("alert"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setLargeIcon(getBitmapFromURL(baseMessage.getSender().getAvatar()))
                    .setGroup(GROUP_ID)
                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(),123,messageIntent,PendingIntent.FLAG_UPDATE_CURRENT))
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(this,"2")
                    .setContentTitle("CometChat")
                    .setContentText(count+" messages")
                    .setSmallIcon(R.drawable.cc)
                    .setGroup(GROUP_ID)
                    .setGroupSummary(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            if (isCall){
                builder.setGroup(GROUP_ID+"Call");
                if (json.getString("alert").equals("Incoming audio call") || json.getString("alert").equals("Incoming video call")) {
                    builder.setOngoing(true);
                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
                    builder.addAction(0, "Answers", PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, getCallIntent("Answers"), PendingIntent.FLAG_UPDATE_CURRENT));
                    builder.addAction(0, "Decline", PendingIntent.getBroadcast(getApplicationContext(), 1, getCallIntent("Decline"), PendingIntent.FLAG_UPDATE_CURRENT));
                }
                notificationManager.notify(05,builder.build());
            }
            else {
                notificationManager.notify(baseMessage.getId(), builder.build());
                notificationManager.notify(0, summaryBuilder.build());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Intent getCallIntent(String title){
        Intent callIntent = new Intent(getApplicationContext(), CallNotificationAction.class);
        callIntent.putExtra(StringContract.IntentStrings.SESSION_ID,call.getSessionId());
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.setAction(title);
        return callIntent;
    }
}
