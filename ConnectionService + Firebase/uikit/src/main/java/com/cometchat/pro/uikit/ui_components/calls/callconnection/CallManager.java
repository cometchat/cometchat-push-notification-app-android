package com.cometchat.pro.uikit.ui_components.calls.callconnection;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.BuildConfig;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_settings.UIKitSettings;

import java.util.Arrays;
import java.util.List;

import static android.content.Context.TELECOM_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

public class CallManager {

    Context context;
    TelecomManager telecomManager;
    PhoneAccountHandle phoneAccountHandle;
    public CallManager(Context context) {
        this.context = context;
        telecomManager = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
        ComponentName componentName =  new ComponentName(context, MyConnectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            phoneAccountHandle = new PhoneAccountHandle(componentName, context.getPackageName());
            PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, context.getPackageName())
                        .setCapabilities(UIKitSettings.getConnectionCapability()).build();
            Log.e( "CallManager: ",phoneAccount.toString() );
            telecomManager.registerPhoneAccount(phoneAccount);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startOutgoingCall() {
        Bundle extras = new Bundle();
        extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);

        TelecomManager manager = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
        PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(new ComponentName(context.getPackageName(), MyConnectionService.class.getName()), "estosConnectionServiceId");
        Bundle test = new Bundle();
        test.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        test.putInt(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_BIDIRECTIONAL);
        test.putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras);
        try {
            if (context.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS) == PackageManager.PERMISSION_GRANTED) {
                manager.placeCall(Uri.parse("tel:$number"), test);
            }
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startIncomingCall(Call call){
        if (context.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS) == PackageManager.PERMISSION_GRANTED) {
            Bundle extras = new Bundle();
            Uri uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, call.getSessionId().substring(0,11), null);
            extras.putString(UIKitConstants.IntentStrings.SESSION_ID,call.getSessionId());
            extras.putString(UIKitConstants.IntentStrings.TYPE,call.getReceiverType());
            extras.putString(UIKitConstants.IntentStrings.CALL_TYPE,call.getType());
            extras.putString(UIKitConstants.IntentStrings.ID,call.getReceiverUid());
            if (call.getReceiverType().equalsIgnoreCase(CometChatConstants.RECEIVER_TYPE_GROUP))
                extras.putString(UIKitConstants.IntentStrings.NAME,((Group)call.getReceiver()).getName());
            else
                extras.putString(UIKitConstants.IntentStrings.NAME,((User)call.getCallInitiator()).getName());

            if (call.getType().equalsIgnoreCase(CometChatConstants.CALL_TYPE_VIDEO))
                extras.putInt(TelecomManager.EXTRA_INCOMING_VIDEO_STATE,VideoProfile.STATE_BIDIRECTIONAL);
            else
                extras.putInt(TelecomManager.EXTRA_INCOMING_VIDEO_STATE,VideoProfile.STATE_AUDIO_ONLY);

            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri);
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            boolean isCallPermitted = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isCallPermitted = telecomManager.isIncomingCallPermitted(phoneAccountHandle);
            } else {
                isCallPermitted = true;
            }
            Log.e("CallManager", "is incoming call permited = "+isCallPermitted+"\n"+phoneAccountHandle.toString());
            try {
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras);
            } catch (SecurityException e) {
                Intent intent = new Intent();
                intent.setAction(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
//                ComponentName telecomComponent = new ComponentName("com.android.server.telecom", "com.android.server.telecom.settings.EnableAccountPreferenceActivity");
//                intent.setComponent(telecomComponent);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e( "CallManagerError: ",e.getMessage());
            }
        }
    }
}
