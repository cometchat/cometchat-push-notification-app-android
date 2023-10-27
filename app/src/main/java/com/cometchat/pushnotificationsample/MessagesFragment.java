package com.cometchat.pushnotificationsample;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cometchat.chat.constants.CometChatConstants;
import com.cometchat.chat.core.CometChat;
import com.cometchat.chat.exceptions.CometChatException;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;
import com.cometchat.chatuikit.messages.CometChatMessages;
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit;
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagesFragment extends Fragment {
    private final String TAG = MessagesFragment.class.getSimpleName();
    private CometChatMessages messages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view;
        if(!CometChat.isInitialized()){
            UIKitSettings uiKitSettings = new UIKitSettings.UIKitSettingsBuilder()
                    .setRegion(AppConfig.AppDetails.REGION)
                    .setAppId(AppConfig.AppDetails.APP_ID)
                    .setAuthKey(AppConfig.AppDetails.AUTH_KEY)
                    .subscribePresenceForAllUsers().build();
            CometChatUIKit.init(getContext(), uiKitSettings, new CometChat.CallbackListener<String>() {
                @Override
                public void onSuccess(String successString) {
                    Log.e(TAG, "onSuccess: CometChatUIKit init");
                }

                @Override
                public void onError(CometChatException e) {
                    Log.e(TAG, "onError: CometChatUIKit init");
                }
            });
        }
        view = inflater.inflate(R.layout.fragment_messages, container, false);
        messages = view.findViewById(R.id.message_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String payload = this.getArguments().getString("notification_payload");
        Log.e(TAG, "onCreateView: payload = "+payload);
        if(!TextUtils.isEmpty(payload)){
            User user = User.fromJson(payload);
            Log.e(TAG, "===>>>: user: " + user);
            messages.setUser(user);

            try {
                JSONObject payloadObject = new JSONObject(payload);
                if(payloadObject.has(CometChatConstants.GroupKeys.KEY_GROUP_GUID)){
                    Group group = Group.fromJson(payloadObject.toString());
                    messages.setGroup(group);
                }else{
                    messages.setUser(User.fromJson(payloadObject.toString()));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}