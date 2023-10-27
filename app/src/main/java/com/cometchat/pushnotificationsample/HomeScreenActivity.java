package com.cometchat.pushnotificationsample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.shared.resources.theme.Palette;
import com.cometchat.pushnotificationsample.helper.CometChatObjectCallback;
import com.cometchat.pushnotificationsample.helper.CometChatObjectHelper;

public class HomeScreenActivity extends AppCompatActivity {

    private final String TAG = HomeScreenActivity.class.getSimpleName();
    private RelativeLayout conversationParentView;

    private int PERMISSION_REQUEST_CODE = 99;

    CometChatNotification cometChatNotification;

    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ANSWER_PHONE_CALLS,Manifest.permission.CALL_PHONE,
            Manifest.permission.MANAGE_OWN_CALLS,Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: RequestCode"+requestCode);
        Log.e(TAG, "onActivityResult: ResultCode"+resultCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        conversationParentView = findViewById(R.id.conversation_container);
        cometChatNotification = CometChatNotification.getInstance(this);
        setTheme();
        handleIntent(getIntent());
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermissions(){
        //Required Permission
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);

        //For VOIP
        if (!cometChatNotification.checkAccountConnection(this)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("VoIP Permission");
            alertDialog.setMessage("To make VoIP Calling work properly, you need to allow certain permission from your call account settings for this app.");
            alertDialog.setPositiveButton("Open Settings", (dialog, which) -> launchVoIPSetting(HomeScreenActivity.this));
            alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            alertDialog.create().show();
        }
    }

    public void launchVoIPSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
        ComponentName telecomComponent = new ComponentName("com.android.server.telecom", "com.android.server.telecom.settings.EnableAccountPreferenceActivity");
        intent.setComponent(telecomComponent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private void handleIntent(Intent intent){
        CometChatObjectHelper.process(intent, new CometChatObjectCallback() {
            @Override
            public void onUserMessage(User user) {
                Log.e(TAG, "===>>>: onUserMessage: " + user);
                Bundle bundle = new Bundle();
                bundle.putString("notification_payload", user.toJson().toString());
                MessagesFragment messagesFragment = new MessagesFragment();
                messagesFragment.setArguments(bundle);
                loadFragment(messagesFragment);
            }

            @Override
            public void onGroupMessage(Group group) {
                Log.e(TAG, "===>>>: onUserMessage: " + group);
                Bundle bundle = new Bundle();
                bundle.putString("notification_payload", group.toString());
                MessagesFragment messagesFragment = new MessagesFragment();
                messagesFragment.setArguments(bundle);
                loadFragment(messagesFragment);
            }

            @Override
            public void onCallMessage() {

            }

            @Override
            public void onNoMessage() {
                loadFragment(new ConversationFragment());
            }
        });
    }

    private void setTheme(){
        Palette palette = Palette.getInstance(this);
        palette.primary(getResources().getColor(R.color.colorPrimary));
        palette.secondary(getResources().getColor(R.color.colorSecondary));
    }

    private void loadFragment(Fragment fragment) {
        Log.e(TAG, "loadFragment: "+fragment.getClass().getSimpleName());
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.conversation_container, fragment).commit();
        }
    }


}