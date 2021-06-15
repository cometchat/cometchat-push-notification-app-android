package com.cometchat.pro.uikit.ui_components.calls.callconnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatCallActivity;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatStartCallActivity;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CallConnection extends Connection {

    Context context;
    Call call;
    String TAG = "CallConnection";

    public CallConnection(Context context,Call call) {
        this.context = context;
        this.call = call;
        Log.e(TAG, "CallConnection: "+call.toString());
    }


    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        Log.i(TAG, "onCallAudioStateChanged"+state);
    }


    @Override
    public void onDisconnect() {
        Log.i(TAG, "onDisconnect");
        super.onDisconnect();
        destroyConnection();
        Log.e(TAG,"onDisconnect");
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL, "Missed"));
        if (CometChat.getActiveCall()!=null)
            onDisconnect(CometChat.getActiveCall());
    }

    void onDisconnect(Call call) {
        Log.e(TAG,"onDisconnect Call: $call");
        CometChat.rejectCall(call.getSessionId(), CometChatConstants.CALL_STATUS_CANCELLED,
                new CometChat.CallbackListener<Call>() {
                    @Override
                    public void onSuccess(Call call) {
                        Log.e(TAG, "onSuccess: reject");
                    }

                    @Override
                    public void onError(CometChatException e) {
                        Toast.makeText(context,"Unable to end call due to ${p0?.code}",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void destroyConnection() {
        setDisconnected(new DisconnectCause(DisconnectCause.REMOTE, "Rejected"));
        Log.e(TAG, "destroyConnection" );
        super.destroy();
    }

    @Override
    public void onAnswer() {
        Log.i(TAG, "onAnswer"+call.getSessionId());
        if (call.getSessionId()!=null) {
            CometChat.acceptCall(call.getSessionId(), new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    Log.e(TAG, "onSuccess: accept");
                    destroyConnection();
                    Intent acceptIntent = new Intent(context, CometChatStartCallActivity.class);
                    acceptIntent.putExtra(UIKitConstants.IntentStrings.SESSION_ID, call.getSessionId());
                    acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(acceptIntent);
                }

                @Override
                public void onError(CometChatException e) {
                    destroyConnection();
                    Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public void onHold() {
        Log.i(TAG, "onHold");
    }

    @Override
    public void onUnhold() {
        Log.i(TAG, "onUnhold");
    }

    @Override
    public void onReject() {
        Log.i(TAG, "onReject"+call.getSessionId());
        if (call.getSessionId()!=null) {
            CometChat.rejectCall(call.getSessionId(), CometChatConstants.CALL_STATUS_REJECTED, new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    Log.e(TAG, "onSuccess: reject" );
                    destroyConnection();
                    setDisconnected(new DisconnectCause(DisconnectCause.REJECTED,"Rejected"));
                }

                @Override
                public void onError(CometChatException e) {
                    destroyConnection();
                    Log.e(TAG, "onErrorReject: "+e.getMessage());
                    Toast.makeText(context,"ErrorReject: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void onOutgoingReject() {
        Log.e(TAG,"onDisconnect");
        destroyConnection();
        setDisconnected(new DisconnectCause(DisconnectCause.REMOTE, "REJECTED"));
    }
}
