package com.cometchat.pushnotificationsample.helper;

import com.cometchat.chat.core.Call;
import com.cometchat.chat.models.Group;
import com.cometchat.chat.models.User;

public interface CometChatObjectCallback {
    void onUserMessage(User user);

    void onGroupMessage(Group group);

    void onCallMessage();

    void onNoMessage();

}
