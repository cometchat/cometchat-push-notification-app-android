package com.cometchat.pro.android.pushnotification;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cometchat.pro.android.pushnotification.utils.MyFirebaseMessagingService;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.uikit.ui_components.calls.callconnection.CallManager;
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MoreActionFragment extends BottomSheetDialogFragment {

    private TextView loggedInUser;
    private TextView launchUI;
    private TextView logout;
    private String TAG = "MoreAction";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more_actions, container, false);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                // androidx should use: com.google.android.material.R.id.design_bottom_sheet
                FrameLayout bottomSheet = (FrameLayout)
                        dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setPeekHeight(0);
            }
        });
        loggedInUser = view.findViewById(R.id.loggedIn_user);
        loggedInUser.setText("Logged In As : "+ CometChat.getLoggedInUser().getName());
        logout = view.findViewById(R.id.logout_tv);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MyFirebaseMessagingService.unsubscribeUserNotification(CometChat.getLoggedInUser().getUid());
                CometChat.logout(new CometChat.CallbackListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        if (getActivity() != null) {
                            getActivity().finish();
                            Toast.makeText(getActivity(), getResources().getString(R.string.logout_success), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(CometChatException e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }
                });
            }
        });

        view.findViewById(R.id.launch_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallManager callManager = new CallManager(requireContext());
                callManager.launchVoIPSetting(requireContext());
            }
        });
        launchUI = view.findViewById(R.id.launch_ui_kit);
        launchUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CometChatUI.class));
            }
        });
        return view;
    }
}