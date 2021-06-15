package com.cometchat.pro.android.pushnotification;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cometchat.pro.android.pushnotification.utils.MyFirebaseMessagingService;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_resources.utils.CallUtils;
import com.cometchat.pro.uikit.ui_resources.utils.MediaUtils;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.File;


public class PushNotificationActivity extends AppCompatActivity {

    private String TAG = "PushNotification";
    private ImageView moreBtn;
    private TextInputEditText uid;
    private TextView titleTv;
    private TextView messageTv;
    private RadioGroup recieverType;
    private RadioButton user, group;
    private EditText message;
    private MaterialButton textMessage, mediaMessage, audioCall, videoCall,customMessage;
    private String receiver = CometChatConstants.RECEIVER_TYPE_USER;
    private TextInputLayout uidLayout;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.please_wait));
        progressDialog.setMessage(getResources().getString(R.string.media_uploading));
        progressDialog.setCancelable(false);
        moreBtn = findViewById(R.id.more_btn);
        titleTv = findViewById(R.id.title_tv);
        messageTv = findViewById(R.id.message_tv);
        uid = findViewById(R.id.uid);
        uidLayout = findViewById(R.id.uid_layout);
        recieverType = findViewById(R.id.reciever_type);
        user = findViewById(R.id.users);
        group = findViewById(R.id.groups);
        message = findViewById(R.id.message);
        textMessage = findViewById(R.id.text_message);
        mediaMessage = findViewById(R.id.media_message);
        audioCall = findViewById(R.id.audio_call);
        videoCall = findViewById(R.id.video_call);
        customMessage = findViewById(R.id.custom_message);
        recieverType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup g, int checkedId) {
                if (user.isChecked()) {
                    uidLayout.setHint(getResources().getString(R.string.enter_uid));
                    receiver = CometChatConstants.RECEIVER_TYPE_USER;
                }
                if (group.isChecked()) {
                    uidLayout.setHint(getResources().getString(R.string.enter_guid));
                    receiver = CometChatConstants.RECEIVER_TYPE_GROUP;
                }
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreActionFragment moreActionFragment = new MoreActionFragment();
                moreActionFragment.show(getSupportFragmentManager(),moreActionFragment.getTag());
            }
        });
        textMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uid.getText().toString().trim().isEmpty()) {
                    uid.setError(getResources().getString(R.string.fill_this_field));
                } else if (message.getText().toString().isEmpty()) {
                    message.setError(getResources().getString(R.string.fill_this_field));
                } else {
//                    subscribePushNotification();
                    TextMessage textMessage = new TextMessage(uid.getText().toString(), message.getText().toString(), receiver);
                    sendMessage(textMessage);
                    message.setText("");
                }
            }
        });
        mediaMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uid.getText().toString().isEmpty())
                    uid.setError(getResources().getString(R.string.fill_this_field));
                else {
                    if (Utils.hasPermissions(PushNotificationActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})) {
                        startActivityForResult(MediaUtils.openGallery(PushNotificationActivity.this), UIKitConstants.RequestCode.GALLERY);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, UIKitConstants.RequestCode.GALLERY);
                    }
                }
            }
        });

        customMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uid.getText().toString().isEmpty())
                    uid.setError(getResources().getString(R.string.fill_this_field));
                else {
                    try {
//                        subscribePushNotification();
                        JSONObject customData = new JSONObject();
                        customData.put("latitude","19.0760");
                        customData.put("longitude","72.8777");
                        CustomMessage customMessage = new CustomMessage(uid.getText().toString(), receiver, "location",customData);
                        JSONObject metaData = new JSONObject();
                        metaData.put("pushNotification","You Received Custom Message");
                        customMessage.setMetadata(metaData);
                        sendMessage(customMessage);
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: "+e.getMessage());
                    }
                }
            }
        });
        audioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uid.getText().toString().isEmpty()) {
                    uid.setError(getResources().getString(R.string.fill_this_field));
                } else {
                    Call call = new Call(uid.getText().toString(), receiver, CometChatConstants.CALL_TYPE_AUDIO);
                    initiateCall(call);
                }
            }
        });
        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uid.getText().toString().isEmpty()) {
                    uid.setError(getResources().getString(R.string.fill_this_field));
                } else {
                    Call call = new Call(uid.getText().toString(), receiver, CometChatConstants.CALL_TYPE_VIDEO);
                    initiateCall(call);
                }
            }
        });
        checkDarkMode();
    }

    private void checkDarkMode() {
        if (Utils.isDarkMode(PushNotificationActivity.this)) {
            titleTv.setTextColor(getResources().getColor(R.color.textColorWhite));
            messageTv.setTextColor(getResources().getColor(R.color.textColorWhite));
        }
    }

    private void initiateCall(Call call) {
        CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                if (call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP))
                    CallUtils.startGroupCallIntent(PushNotificationActivity.this,
                            ((Group)call.getCallReceiver()),call.getType(),true,
                            call.getSessionId());
                else if (call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER))
                    CallUtils.startCallIntent(PushNotificationActivity.this,
                            ((User)call.getCallReceiver()),call.getType(),true,
                            call.getSessionId());
                Toast.makeText(PushNotificationActivity.this,getResources().getString(R.string.call_initated_success),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onCallError: "+e.getMessage());
                Toast.makeText(PushNotificationActivity.this,getResources().getString(R.string.call_initated_error),Toast.LENGTH_LONG).show();
            }
        });
    }

//    private void subscribePushNotification() {
//        if (receiver.equals(CometChatConstants.RECEIVER_TYPE_GROUP))
//            MyFirebaseMessagingService.subscribeGroupNotification(uid.getText().toString());
//    }

    private void sendMessage(BaseMessage baseMessage) {
        if (baseMessage instanceof TextMessage) {
            CometChat.sendMessage((TextMessage) baseMessage, new CometChat.CallbackListener<TextMessage>() {
                @Override
                public void onSuccess(TextMessage textMessage) {
                    Toast.makeText(PushNotificationActivity.this, getResources().getString(R.string.text_message_sent), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(CometChatException e) {
                    Toast.makeText(PushNotificationActivity.this, getResources().getString(R.string.text_message_failed), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onError: "+e.getMessage());
                }
            });
        } else if (baseMessage instanceof MediaMessage) {
            CometChat.sendMediaMessage((MediaMessage) baseMessage, new CometChat.CallbackListener<MediaMessage>() {
                @Override
                public void onSuccess(MediaMessage mediaMessage) {
                    progressDialog.dismiss();
                    Toast.makeText(PushNotificationActivity.this, getResources().getString(R.string.media_message_sent), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(CometChatException e) {
                    progressDialog.dismiss();
                    Log.e(TAG, "onError: "+e.getMessage());
                    Toast.makeText(PushNotificationActivity.this, getResources().getString(R.string.media_message_failed), Toast.LENGTH_LONG).show();

                }
            });
        } else if (baseMessage instanceof CustomMessage) {
            CometChat.sendCustomMessage((CustomMessage) baseMessage, new CometChat.CallbackListener<CustomMessage>() {
                @Override
                public void onSuccess(CustomMessage customMessage) {
                    Toast.makeText(PushNotificationActivity.this, getResources().getString(R.string.custom_message_sent), Toast.LENGTH_LONG).show();

                }

                @Override
                public void onError(CometChatException e) {
                    Log.e(TAG, "onError: "+e.getMessage());
                    Toast.makeText(PushNotificationActivity.this, getResources().getString(R.string.custom_message_failed), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");

        switch (requestCode) {
            case UIKitConstants.RequestCode.GALLERY:
                if (data != null) {

                    File file = MediaUtils.getRealPath(PushNotificationActivity.this, data.getData(),false);
                    ContentResolver cr = getContentResolver();
                    String mimeType = cr.getType(data.getData());
                    if (mimeType != null && mimeType.contains("image")) {
                        if (file.exists())
                            sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_IMAGE);
                        else
                            Toast.makeText(PushNotificationActivity.this, "File does not exist", Toast.LENGTH_LONG).show();
                    } else {
                        if (file.exists())
                            sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_VIDEO);
                        else
                            Toast.makeText(PushNotificationActivity.this, "File does not exist", Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }
    }

    private void sendMediaMessage(File file, String messageType) {
//        subscribePushNotification();
        progressDialog.show();
        MediaMessage mediaMessage = new MediaMessage(uid.getText().toString(),file,messageType,receiver);
        sendMessage(mediaMessage);
    }
}
