package com.cometchat.pushnotificationsample;

import static android.content.Context.TELECOM_SERVICE;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CallManager;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Action;
import com.cometchat.chat.models.BaseMessage;
import com.cometchat.chat.models.CustomMessage;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.MediaMessage;
import com.cometchat.chat.models.TextMessage;
import com.cometchat.chat.models.User;
import com.cometchat.pushnotificationsample.helper.ConstantFile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CometChatNotification {
    private static final String TAG = CometChatNotification.class.getSimpleName();
    private static Context context;
    private static CometChatNotification cometChatNotification;
    private static NotificationManager notificationManager;
    private static TelecomManager telecomManager;
    private static ComponentName componentName;
    private static PhoneAccountHandle phoneAccountHandle;

    private CometChatNotification() {}

    public static CometChatNotification getInstance(Context c) {
        if(cometChatNotification == null) {
            cometChatNotification = new CometChatNotification();
            context = c;
            notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

            //For VoIP
            telecomManager = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
            ComponentName componentName = new ComponentName(context, CallConnectionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                phoneAccountHandle = new PhoneAccountHandle(componentName, context.getPackageName());
                PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, context.getPackageName())
                        .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER).build();
                telecomManager.registerPhoneAccount(phoneAccount);
            }
        }
        return cometChatNotification;
    }


    public void registerCometChatNotification(final CometChat.CallbackListener<String> listener){
        if(!isFirebaseAppInitialized()){
            listener.onError(new CometChatException("Notifications Not Registered","FireBase App Not Initialized"));
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                Log.e(TAG, "Push Notification Token = "+token);
                CometChat.registerTokenForPushNotification(token, new CometChat.CallbackListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG, "onSuccess:  CometChat Notification Registered : "+s );
                        listener.onSuccess(s);
                    }

                    @Override
                    public void onError(CometChatException e) {
                        Log.e(TAG, "onError: Notification Registration Failed : "+e.getMessage());
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void renderCometChatNotification(RemoteMessage remoteMessage, final CometChat.CallbackListener<String> listener){
        JSONObject data = new JSONObject(remoteMessage.getData());
        Log.e(TAG, "renderCometChatNotification: Data "+data );
        if(!data.has(CometChatConstants.CATEGORY_MESSAGE)){
            listener.onError(new CometChatException("Error","Not a CometChat Notification Data Payload"));
            return;
        }

        try {
            JSONObject rawMessageJson = new JSONObject(data.getString(CometChatConstants.CATEGORY_MESSAGE));
            BaseMessage baseMessage = getBaseMessage(rawMessageJson);
            Log.e(TAG, "renderCometChatNotification: BaseMessageType"+baseMessage.getType());

            switch (Objects.requireNonNull(baseMessage).getType()){
                case CometChatConstants.MESSAGE_TYPE_TEXT:
                    TextMessage textMessage = TextMessage.fromJson(rawMessageJson);
                    renderTextMessageNotification(textMessage);
                    break;

                case CometChatConstants.MESSAGE_TYPE_VIDEO:
                    Log.e(TAG, "renderCometChatNotification: Video Call Received");
                    Call videoCallObject = Call.fromJson(rawMessageJson.toString());
                    handleCallNotification(videoCallObject);
                    break;

                case CometChatConstants.MESSAGE_TYPE_AUDIO:
                    Log.e(TAG, "renderCometChatNotification: Audio Call Received");
                    Call audioCallObject = Call.fromJson(rawMessageJson.toString());
                    handleCallNotification(audioCallObject);
                    break;
                default:
            }


        } catch (JSONException e) {
            Log.e(TAG,"Render Exception : "+e.getMessage());
        }
    }

    public boolean isCometChatNotification(RemoteMessage remoteMessage){
        JSONObject data = new JSONObject(remoteMessage.getData());
        if(data.has(CometChatConstants.CATEGORY_MESSAGE)){
            return true;
        }else{
            return false;
        }
    }

    private void handleCallNotification(Call call){
        Log.e(TAG, "handleCallNotification: Called ::");
        /*String uid = CometChat.getLoggedInUser().getUid();
        if(call.getCallReceiver() instanceof User && ((User) call.getCallReceiver()).getUid().equals(uid)){
            startIncomingCall(call);
        }*/
        startIncomingCall(call);
    }

    private void registerConnectionService(){
        //For VOIP

    }

    public void startIncomingCall(Call call) {
        if (context.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS) == PackageManager.PERMISSION_GRANTED) {
            Bundle extras = new Bundle();
            Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, call.getSessionId().substring(0, 11), null);

            extras.putString(ConstantFile.IntentStrings.SESSION_ID, call.getSessionId());
            extras.putString(ConstantFile.IntentStrings.TYPE, call.getReceiverType());
            extras.putString(ConstantFile.IntentStrings.CALL_TYPE, call.getType());

            if(call.getCallInitiator() instanceof User){
                extras.putString(ConstantFile.IntentStrings.NAME, ((User)call.getReceiver()).getName());
            }else{
                extras.putString(ConstantFile.IntentStrings.NAME, ((Group)call.getReceiver()).getName());
            }

            if(call.getCallReceiver() instanceof User){
                extras.putString(ConstantFile.IntentStrings.ID, ((User)call.getReceiver()).getUid());
            }else{
                extras.putString(ConstantFile.IntentStrings.ID, ((Group)call.getReceiver()).getGuid());
            }

            if (call.getType().equalsIgnoreCase(CometChatConstants.CALL_TYPE_VIDEO)) {
                extras.putInt(TelecomManager.EXTRA_INCOMING_VIDEO_STATE, VideoProfile.STATE_BIDIRECTIONAL);
            } else {
                extras.putInt(TelecomManager.EXTRA_INCOMING_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY);
            }

            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    telecomManager.isIncomingCallPermitted(phoneAccountHandle);
                }
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("CallManagerError: ", e.getMessage());
            }
        }
    }

    private void renderTextMessageNotification(TextMessage message){
        Log.e(TAG, "renderTextMessageNotification: "+message.toString());
        Log.e(TAG, "renderTextMessageNotification: Receiver type = "+message.getReceiverType());
        Log.e(TAG, "renderTextMessageNotification: Receiver type = "+message.getReceiver());

        if(message.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)){
            showNotification(
                    message.getId(),
                    message.getSender().getName(),
                    message.getText(),
                    message.getSender().getAvatar(),
                    message.getSender().toJson()
            );
        }else{
            showNotification(
                    message.getId(),
                    ((Group)message.getReceiver()).getName(),
                    message.getText(),
                    ((Group)message.getReceiver()).getIcon(),
                    groupToJson((Group)(message.getReceiver()))
            );
        }

    }

    public boolean checkAccountConnection(Context context) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && telecomManager!=null) {
                final List<PhoneAccountHandle> enabledAccounts = telecomManager.getCallCapablePhoneAccounts();
                for (PhoneAccountHandle account : enabledAccounts) {
                    if (account.getComponentName().getClassName().equals(CallConnectionService.class.getCanonicalName())) {
                        isConnected = true;
                        break;
                    }
                }
            }
        }
        return isConnected;
    }

    private void showNotification(int nid, String title ,String text ,String largeIconUrl ,JSONObject payload){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Messages", title, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("You messages!!");

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        // Get the notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Create the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Messages");
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

        if(!TextUtils.isEmpty(largeIconUrl)){
            builder.setLargeIcon(getBitmapFromURL(largeIconUrl));
        }else{
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background));
        }

        // Create the pending intent
        Log.e(TAG, "showNotification: Payload = "+payload);
        Intent intent = new Intent(context, HomeScreenActivity.class);
        intent.putExtra("notification_payload",payload.toString());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 501, intent, PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // show the notification
        notificationManager.notify(nid, builder.build());
    }

    private static boolean isFirebaseAppInitialized(){
        if(FirebaseApp.getApps(context).isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    private Bitmap getBitmapFromURL(String strURL) {
        if (strURL != null) {
            try {
                URL url = new URL(strURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private JSONObject groupToJson(Group group){
        JSONObject groupObject = new JSONObject();
        try {
            groupObject.put(Group.COLUMN_GUID,group.getGuid());
            groupObject.put(Group.COLUMN_NAME,group.getName());
            groupObject.put(Group.COLUMN_GROUP_TYPE,group.getGroupType());
            groupObject.put(Group.COLUMN_PASSWORD,group.getPassword());
            groupObject.put(Group.COLUMN_ICON,group.getIcon());
            groupObject.put(Group.COLUMN_DESCRIPTION,group.getDescription());
            groupObject.put(Group.COLUMN_OWNER,group.getOwner());
            groupObject.put(Group.COLUMN_METADATA,group.getMetadata());
            groupObject.put(Group.COLUMN_CREATED_AT,group.getCreatedAt());
            groupObject.put(Group.COLUMN_UPDATED_AT,group.getUpdatedAt());
            groupObject.put(Group.COLUMN_HAS_JOINED,group.getJoinedAt());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return groupObject;
    }
    private BaseMessage getBaseMessage(JSONObject messageObject) throws JSONException {
        if (messageObject.has(CometChatConstants.MessageKeys.KEY_MESSAGE_CATEGORY)) {
            String category = messageObject.getString(CometChatConstants.MessageKeys.KEY_MESSAGE_CATEGORY);
            if (category.equalsIgnoreCase(CometChatConstants.CATEGORY_MESSAGE)) {
                if (messageObject.has(CometChatConstants.MessageKeys.KEY_SEND_MESSAGE_TYPE)) {
                    String type = messageObject.getString(CometChatConstants.MessageKeys.KEY_SEND_MESSAGE_TYPE);
                    if (type.equalsIgnoreCase(CometChatConstants.MESSAGE_TYPE_TEXT)) {
                        return TextMessage.fromJson(messageObject);
                    } else if (type.equalsIgnoreCase(CometChatConstants.MESSAGE_TYPE_CUSTOM)) {
                        return CustomMessage.fromJson(messageObject);
                    } else {
                        return MediaMessage.fromJson(messageObject);
                    }
                }
            } else if (category.equalsIgnoreCase(CometChatConstants.CATEGORY_ACTION)) {
                return Action.fromJson(messageObject);
            } else if (category.equalsIgnoreCase(CometChatConstants.CATEGORY_CALL)) {
                return Call.fromJson(messageObject.toString());
            } else if (category.equalsIgnoreCase(CometChatConstants.CATEGORY_CUSTOM)) {
                return CustomMessage.fromJson(messageObject);
            }
        }
        return null;
    }
}
