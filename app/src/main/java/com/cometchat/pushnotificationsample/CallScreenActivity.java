package com.cometchat.pushnotificationsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cometchat.chatuikit.calls.ongoingcall.CometChatOngoingCall;
import com.cometchat.pushnotificationsample.helper.ConstantFile;

public class CallScreenActivity extends AppCompatActivity {

    private String TAG = CallScreenActivity.class.getSimpleName();
    private CometChatOngoingCall ongoingCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_screen);

        ongoingCall = findViewById(R.id.ongoing_call);

        Intent intent = getIntent();
        if(intent.hasExtra(ConstantFile.IntentStrings.SESSION_ID) && intent.hasExtra(ConstantFile.IntentStrings.RECEIVER_TYPE)){

            String sessionID = intent.getStringExtra(ConstantFile.IntentStrings.SESSION_ID);
            String receiverType = intent.getStringExtra(ConstantFile.IntentStrings.RECEIVER_TYPE);

            Log.e(TAG, "onCreate: SessionID ="+sessionID);
            Log.e(TAG, "onCreate: receiverType ="+receiverType);

            ongoingCall.setSessionId(sessionID);
            ongoingCall.setReceiverType(receiverType);
            ongoingCall.startCall();
        }

    }
}