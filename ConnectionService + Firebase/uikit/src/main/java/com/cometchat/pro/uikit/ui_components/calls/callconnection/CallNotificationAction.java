package com.cometchat.pro.uikit.ui_components.calls.callconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatCallActivity;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatStartCallActivity;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;

public class CallNotificationAction extends BroadcastReceiver {

    String TAG = "CallNotificationAction";
    @Override
    public void onReceive(Context context, Intent intent) {
        String sessionID = intent.getStringExtra(UIKitConstants.IntentStrings.SESSION_ID);
        Log.e(TAG, "onReceive: " + intent.getStringExtra(UIKitConstants.IntentStrings.SESSION_ID));
        if (intent.getAction().equals("Answer_")) {
            CometChat.acceptCall(sessionID, new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    Intent acceptIntent = new Intent(context, CometChatStartCallActivity.class);
                    acceptIntent.putExtra(UIKitConstants.IntentStrings.SESSION_ID,sessionID);
                    acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(acceptIntent);
                }

                @Override
                public void onError(CometChatException e) {
                    Toast.makeText(context,"Error "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(UIKitConstants.Notification.ID);
        } else if(intent.getAction().equals("Decline_")) {
            CometChat.rejectCall(sessionID, CometChatConstants.CALL_STATUS_REJECTED, new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.cancel(UIKitConstants.Notification.ID);
                    if (CometChatCallActivity.callActivity!=null)
                        CometChatCallActivity.callActivity.finish();
                }

                @Override
                public void onError(CometChatException e) {
                    Toast.makeText(context,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        } else if (intent.getAction().equals("StartCall")) {
            Log.e(TAG, "onReceive: StartCall" );

            Intent acceptIntent = new Intent(context, CometChatStartCallActivity.class);
            acceptIntent.putExtra(UIKitConstants.IntentStrings.SESSION_ID,sessionID);
            acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            acceptIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(acceptIntent);
        }
    }
}
