package com.cometchat.pushnotificationsample;

import android.content.Intent;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;
import android.widget.Toast;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.Call;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings;
import com.cometchat.pushnotificationsample.helper.ConstantFile;


public class CallConnection extends Connection {

    CallConnectionService service;
    Call call;
    String TAG = "CallConnection";

    public CallConnection(CallConnectionService service, Call call) {
        this.service = service;
        this.call = call;
        Log.e(TAG, "CallConnection: "+call.toString());
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        Log.i(TAG, "onCallAudioStateChanged"+state);
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        destroyConnection();
        Log.e(TAG,"onDisconnect");
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL, "Missed"));
        if (CometChat.getActiveCall()!=null)
            onDisconnect(CometChat.getActiveCall());
    }

    void onDisconnect(Call call) {
        Log.e(TAG,"onDisconnect Call:"+call);
        CometChat.rejectCall(call.getSessionId(), CometChatConstants.CALL_STATUS_CANCELLED, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Log.e(TAG, "onSuccess: reject");
            }
            @Override
            public void onError(CometChatException e) {
                Toast.makeText(service,"Unable to end call due to ${p0?.code}", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void destroyConnection() {
        setDisconnected(new DisconnectCause(DisconnectCause.REMOTE, "Rejected"));
        Log.e(TAG, "destroyConnection" );
        super.destroy();
    }

    @Override
    public void onAnswer(int videoState) {
        Log.e(TAG, "onAnswerVideo: " );
        if (call.getSessionId()!=null) {

            if(!CometChat.isInitialized()){
                initializeCometChat();
            }
            CometChat.acceptCall(call.getSessionId(), new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    Log.e(TAG, "onSuccess: accept "+service);
                   // service.sendBroadcast(getCallIntent("CometChat_Call_Event"));
                    Intent intent = new Intent(service,CallScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(ConstantFile.IntentStrings.SESSION_ID,call.getSessionId());
                    intent.putExtra(ConstantFile.IntentStrings.RECEIVER_TYPE,call.getReceiverType());
                    service.startActivity(intent);
                    destroyConnection();
                }

                @Override
                public void onError(CometChatException e) {
                    destroyConnection();
                    Toast.makeText(service, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onShowIncomingCallUi() {
        Log.e(TAG, "onShowIncomingCallUi: " );
    }

    @Override
    public void onAnswer() {
        Log.i(TAG, "onAnswer"+call.getSessionId());
        if (call.getSessionId()!=null) {

            if(!CometChat.isInitialized()){
                initializeCometChat();
            }
            CometChat.acceptCall(call.getSessionId(), new CometChat.CallbackListener<Call>() {
                @Override
                public void onSuccess(Call call) {
                    Log.e(TAG, "onSuccess: accept");
                    service.sendBroadcast(getCallIntent("CometChat_Call_Event"));
                    Intent intent = new Intent(service,CallScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(ConstantFile.IntentStrings.SESSION_ID,call.getSessionId());
                    intent.putExtra(ConstantFile.IntentStrings.RECEIVER_TYPE,call.getReceiverType());
                    service.startActivity(intent);
                    destroyConnection();
                }

                @Override
                public void onError(CometChatException e) {
                    destroyConnection();
                    e.printStackTrace();
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

            if(!CometChat.isInitialized()){
                initializeCometChat();
            }
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
                }
            });
        }
    }

    public void onOutgoingReject() {
        Log.e(TAG,"onDisconnect");
        destroyConnection();
        setDisconnected(new DisconnectCause(DisconnectCause.REMOTE, "REJECTED"));
    }

    private Intent getCallIntent(String title){
        Intent callIntent = new Intent(title);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.putExtra(ConstantFile.IntentStrings.SESSION_ID,call.getSessionId());
        return callIntent;
    }

    private void initializeCometChat(){
        UIKitSettings uiKitSettings = new UIKitSettings.UIKitSettingsBuilder()
                .setRegion(AppConfig.AppDetails.REGION)
                .setAppId(AppConfig.AppDetails.APP_ID)
                .setAuthKey(AppConfig.AppDetails.AUTH_KEY)
                .subscribePresenceForAllUsers().build();

        CometChatUIKit.init(service, uiKitSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successString) {
                Log.e(TAG, "onSuccess: CometChatUIKit init");
                if (CometChatUIKit.getLoggedInUser() != null){
                }else {
                    Log.e(TAG, "User not logged in");
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: CometChatUIKit init");
            }
        });
    }
}
