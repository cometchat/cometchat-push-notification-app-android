package screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ConcurrentHashMap;

import constant.StringContract;
import screen.messagelist.CometChatMessageListActivity;

public class CallActivity extends AppCompatActivity {

    private RelativeLayout callView;

    private String sessionId;

    private static final String TAG = "CallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        callView = findViewById(R.id.call_view);
        if (getIntent().hasExtra(StringContract.IntentStrings.SESSION_ID)) {
            sessionId = getIntent().getStringExtra(StringContract.IntentStrings.SESSION_ID);

            CometChat.startCall(CallActivity.this,sessionId,callView, new CometChat.OngoingCallListener() {
                @Override
                public void onUserJoined(User user) {
                    Log.e("onUserJoined: ",user.getUid() );
                }

                @Override
                public void onUserLeft(User user) {
                    Snackbar.make(getWindow().getDecorView().getRootView(),"User Left: "+user.getName(),Snackbar.LENGTH_LONG).show();
                    Log.e( "onUserLeft: ",user.getUid() );
                    finish();
                }

                @Override
                public void onError(CometChatException e) {
                    Log.e( "onError: ",e.getMessage() );
                }

                @Override
                public void onCallEnded(Call call) {
                    Log.e(TAG, "onCallEnded: "+call.toString() );
                    finish();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

