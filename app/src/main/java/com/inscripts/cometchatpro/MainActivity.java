package com.inscripts.cometchatpro;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.TextMessage;

import org.json.JSONException;
import org.json.JSONObject;

import static com.inscripts.cometchatpro.Constant.GUID;
import static com.inscripts.cometchatpro.Constant.UID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText etMessage;

    private String receiverId=UID;

    private String receiverType=CometChatConstants.RECEIVER_TYPE_USER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        etMessage = findViewById(R.id.et_sendMessage);
        Button btnSendButton = findViewById(R.id.sendbutton);
        Button btnCall=findViewById(R.id.call_button);
        Button btnCustom=findViewById(R.id.custom_button);

        RadioButton radioOne = findViewById(R.id.radio_one);
        RadioButton radioGroup = findViewById(R.id.radio_group);

        radioOne.setTextColor(getColorList());
        radioGroup.setTextColor(getColorList());

        btnSendButton.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            sendMessage(message);
        });

        btnCall.setOnClickListener(v -> initiateCall());

        btnCustom.setOnClickListener(v -> {
             sendCustom();
        });
    }

    private void initiateCall() {

        Call call=new Call(receiverId,receiverType,CometChatConstants.CALL_TYPE_AUDIO);
        CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
            @Override
            public void onSuccess(Call call) {
                Log.d(TAG, "onSuccess: "+call.toString());
                Toast.makeText(MainActivity.this, "Call initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "onError: "+e.getMessage());
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendCustom(){
        try {

            JSONObject jsonObject=new JSONObject();
            JSONObject metaData=new JSONObject();

            metaData.put("pushNotification",etMessage.getText().toString());

            CustomMessage customMessage=new CustomMessage(receiverId, receiverType,"message",jsonObject );

            customMessage.setMetadata(metaData);

            CometChat.sendCustomMessage(customMessage, new CometChat.CallbackListener<CustomMessage>() {
                @Override
                public void onSuccess(CustomMessage customMessage) {
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    etMessage.setText("");
                }

                @Override
                public void onError(CometChatException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {

            case R.id.radio_one:
                if (checked) {
                    receiverId = UID;
                    receiverType = CometChatConstants.RECEIVER_TYPE_USER;
                }
                break;
            case R.id.radio_group:
                if (checked) {
                    receiverId = GUID;
                    receiverType = CometChatConstants.RECEIVER_TYPE_GROUP;
                }
                break;
        }
    }

    private ColorStateList getColorList() {

        int state[][] = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };

        int color[] = new int[]{
                Color.BLACK,
                Color.WHITE,

        };

        return new ColorStateList(state, color);

    }

    private void sendMessage(String message) {

        TextMessage textMessage = new TextMessage(receiverId,message,CometChatConstants.MESSAGE_TYPE_TEXT,receiverType);

        CometChat.sendMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage) {
                etMessage.setText("");
                Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onError: " + e.getMessage());
            }
        });
    }
}
