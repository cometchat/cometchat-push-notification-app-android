package com.cometchat.pushnotificationsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CallEventReceiver extends BroadcastReceiver {

    private final String TAG = CallEventReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: "+"Call Event Broadcast Received");
    }
}
