package com.cometchat.pro.uikit.ui_components.calls.callconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.uikit.ui_components.calls.call_manager.CometChatStartCallActivity;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;

import static android.telecom.TelecomManager.PRESENTATION_ALLOWED;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MyConnectionService extends ConnectionService {

    public static CallConnection conn;
    public MyConnectionService() {
        super();
    }


    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Bundle bundle = request.getExtras();
        String sessionID = bundle.getString(UIKitConstants.IntentStrings.SESSION_ID);
        String name = bundle.getString(UIKitConstants.IntentStrings.NAME);
        String type = bundle.getString(UIKitConstants.IntentStrings.TYPE);
        String callType = bundle.getString(UIKitConstants.IntentStrings.CALL_TYPE);
        String receiverUID = bundle.getString(UIKitConstants.IntentStrings.ID);
        Call call = new Call(receiverUID,type,callType);
        call.setSessionId(sessionID);
        Log.i("CallConnectionService", "onCreateIncomingConnectionCall:"+call.toString());
        conn = new CallConnection(this,call);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            conn.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        }
        conn.setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED);
        conn.setAddress(request.getAddress(), PRESENTATION_ALLOWED);
        conn.setInitializing();
        conn.setVideoProvider(new Connection.VideoProvider() {
            @Override
            public void onSetCamera(String cameraId) {

            }

            @Override
            public void onSetPreviewSurface(Surface surface) {

            }

            @Override
            public void onSetDisplaySurface(Surface surface) {

            }

            @Override
            public void onSetDeviceOrientation(int rotation) {

            }

            @Override
            public void onSetZoom(float value) {

            }

            @Override
            public void onSendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) {

            }

            @Override
            public void onSendSessionModifyResponse(VideoProfile responseProfile) {

            }

            @Override
            public void onRequestCameraCapabilities() {

            }

            @Override
            public void onRequestConnectionDataUsage() {

            }

            @Override
            public void onSetPauseImage(Uri uri) {

            }
        });
        conn.setActive();
        return conn;
//        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
        Log.e("onIncomingFailed:",connectionManagerPhoneAccount.toString() );
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
//        Log.i("CallConnectionService", "onCreateOutgoingConnection");
//        CallConnection conn = new CallConnection(getApplicationContext());
//        conn.setAddress(request.getAddress(), PRESENTATION_ALLOWED);
//        conn.setInitializing();
//        conn.setVideoProvider(new Connection.VideoProvider() {
//            @Override
//            public void onSetCamera(String cameraId) {
//
//            }
//
//            @Override
//            public void onSetPreviewSurface(Surface surface) {
//
//            }
//
//            @Override
//            public void onSetDisplaySurface(Surface surface) {
//
//            }
//
//            @Override
//            public void onSetDeviceOrientation(int rotation) {
//
//            }
//
//            @Override
//            public void onSetZoom(float value) {
//
//            }
//
//            @Override
//            public void onSendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) {
//
//            }
//
//            @Override
//            public void onSendSessionModifyResponse(VideoProfile responseProfile) {
//
//            }
//
//            @Override
//            public void onRequestCameraCapabilities() {
//
//            }
//
//            @Override
//            public void onRequestConnectionDataUsage() {
//
//            }
//
//            @Override
//            public void onSetPauseImage(Uri uri) {
//
//            }
//        });
//        conn.setActive();
//        return conn;
        return super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request);
    }
}
