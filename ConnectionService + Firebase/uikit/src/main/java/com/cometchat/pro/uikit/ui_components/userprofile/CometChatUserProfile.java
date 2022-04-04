package com.cometchat.pro.uikit.ui_components.userprofile;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.R;
import com.cometchat.pro.uikit.databinding.FragmentCometchatUserProfileBinding;
import com.cometchat.pro.uikit.ui_components.shared.CometChatSnackBar;
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar;
import com.cometchat.pro.uikit.ui_components.users.block_users.CometChatBlockUserListActivity;
import com.cometchat.pro.uikit.ui_resources.utils.CometChatError;
import com.cometchat.pro.uikit.ui_resources.utils.MediaUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils;
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;
import com.cometchat.pro.uikit.ui_components.userprofile.privacy_security.CometChatMorePrivacyActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CometChatUserProfile extends Fragment {

    private AlertDialog.Builder dialog;
    private String avatarUrl = CometChat.getLoggedInUser().getAvatar();
    private CometChatAvatar avatar;
    FragmentCometchatUserProfileBinding moreInfoScreenBinding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        CometChatError.init(getContext());
        moreInfoScreenBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cometchat_user_profile, container, false);
        moreInfoScreenBinding.setUser(CometChat.getLoggedInUser());
        moreInfoScreenBinding.ivUser.setAvatar(CometChat.getLoggedInUser());

        moreInfoScreenBinding.tvTitle.setTypeface(FontUtils.getInstance(getActivity()).getTypeFace(FontUtils.robotoMedium));
        Log.e("onCreateView: ", CometChat.getLoggedInUser().toString());
        moreInfoScreenBinding.privacyAndSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CometChatBlockUserListActivity.class));
            }
        });

        if(Utils.isDarkMode(getContext())) {
            moreInfoScreenBinding.tvTitle.setTextColor(getResources().getColor(R.color.textColorWhite));
            moreInfoScreenBinding.tvSeperator.setBackgroundColor(getResources().getColor(R.color.grey));
            moreInfoScreenBinding.tvSeperator1.setBackgroundColor(getResources().getColor(R.color.grey));
        } else {
            moreInfoScreenBinding.tvTitle.setTextColor(getResources().getColor(R.color.primaryTextColor));
            moreInfoScreenBinding.tvSeperator.setBackgroundColor(getResources().getColor(R.color.light_grey));
            moreInfoScreenBinding.tvSeperator1.setBackgroundColor(getResources().getColor(R.color.light_grey));
        }

        moreInfoScreenBinding.editUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDialog();
            }
        });
        return moreInfoScreenBinding.getRoot();
    }

    private void updateUserDialog() {
        dialog = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.cometchat_update_user_dialog,null);
        avatar = view.findViewById(R.id.user_avatar);
        avatar.setAvatar(avatarUrl);

        TextInputEditText username = view.findViewById(R.id.username_edt);
        username.setText(CometChat.getLoggedInUser().getName());
        MaterialButton updateUserBtn = view.findViewById(R.id.updateUserBtn);
        MaterialButton cancelBtn = view.findViewById(R.id.cancelBtn);
        ImageView editAvatar = view.findViewById(R.id.edit_avatar);
        AlertDialog alertDialog = dialog.create();
        alertDialog.setView(view);
        editAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(MediaUtils.openGallery(getActivity()), UIKitConstants.RequestCode.GALLERY);
            }
        });
        avatar.setAvatar(avatarUrl);
        updateUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().isEmpty())
                    username.setError(getString(R.string.fill_this_field));
                else {
                    User user = CometChat.getLoggedInUser();
                    user.setName(username.getText().toString());
                    user.setAvatar(avatarUrl);
                    updateUser(user);
                    alertDialog.dismiss();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void updateUser(User user) {
        Log.d("updateUser: ",user.toString());
        CometChat.updateCurrentUserDetails(user, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (getContext()!=null)
                    CometChatSnackBar.show(getContext(),moreInfoScreenBinding.getRoot(),
                            getString(R.string.updated_user_successfully),CometChatSnackBar.SUCCESS);
                moreInfoScreenBinding.setUser(user);
            }

            @Override
            public void onError(CometChatException e) {
                e.printStackTrace();
                if (getContext()!=null)
                    CometChatSnackBar.show(getContext(),moreInfoScreenBinding.tvTitle, CometChatError.localized(e), CometChatSnackBar.ERROR);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            if (requestCode==UIKitConstants.RequestCode.GALLERY) {
                if (data != null) {
                    File file = MediaUtils.getRealPath(getContext(), data.getData(), false);
                    ContentResolver cr = getActivity().getContentResolver();
                    String mimeType = cr.getType(data.getData());
                    if (mimeType != null && mimeType.contains("image")) {
                        //encode image to base64 string
                        JSONObject body=new JSONObject();
                        try {
                            InputStream inputStream = new FileInputStream(file.getPath()); // You can get an inputStream using any I/O API
                            byte[] bytes;
                            byte[] buffer = new byte[10240];
                            int bytesRead;
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                            bytes = output.toByteArray();
                            String imageString = Base64.encodeToString(bytes, Base64.NO_WRAP);
                            body.put("avatar","data:image/png;base64,"+imageString);
//                            body.put("avatar", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHAAAABzCAMAAAB3qnbXAAACTFBMVEVHcEwAAAAAAAAAAAAAAAAAAAAAAAD+/v4AAAAAAAAlJCQAAAAAAAAAAAAAAAAAAAAAAABRUVEAAAAAAAAAAAABAAAAAAAwMDAAAAASEhIAAAAAAAACAgICAAAAAAD/AAAAAABVZmb///9WZmYzM0T+AACqu7s0NEQzM0UxMUL9AABDAAAyM0P/iIiru7v/QkJDQ0Pv7+9VVVX/RET/u7tUZWX/EBCIAAC9AAD/IyMEBAT/jIz3AAC+vr5GVFX/d3Y1QkQyNkH/mJj/SUn+bGy2tbX/ZGRNVln+3NwTAAA/QkvvAgL/q6tUAABYZGSNAAAGAACLioz+U1P/vb2tvb3MEBBlMjJSYmP/MjJISEirAADZAgGcnZ2aICH39/dra2v/8PCEAAD/zs7e39/b29vLAAAyQkJ8fH78//+IIyX/nJw1MkMhAABTVWb/aGj+y8svAAD+2NiYAADdEhKrq6v/hYX/+PhERlPmBQZiY2R2dXU7AAD/Wlr/k5NhAQFEMjTPzc17AQFULzCKlZn/fHx8iYn79/dyAAC7ERKqvb3/7u6VlJS1AgL5Rkbo6elnV11oAQGsLzD4EBClAABLAAAdICA6PEC7u7v6kZGwl5fN3d37Li7uFxfoa2ve6O+8yctZWlz/wsL3///+Ozv1t7d4UlOpvblUZ2qcq6qqt7fHyMl1IiSLREXqMDD6IyPtd3f8tbXP0dL/5eVOP0K9JSXUWFjOzt5OTlpgZ2z/trarFSCXQEHeRERldHXDf3/JtrXxqKit1NRYPDyEY2MAPLyJAAAAH3RSTlMAlLtqVKu3/vsF/q8jiNoQev5H30I0yv7t/igMpb/EbYJZFwAAFGVJREFUaN7UmP9PE2kawBcsTkFA3BP3dm+9mc60TNu0ZZiWSQql3xRKStOE0kLJAb0oKUADhKBBjIYNxgRLIsFEjB4RL0Yxup5xT3V19zaXu3/snmfemWmn7QDZ+Ms9v3Tazryfeb4/7/vVV/+ncratoa29/PvZtpaWlvP1IPo/vpA0mkwNJhN8KvKVyWQC4CkZiH98Wfn6JCNLAwK/M506xVRIw+kvSGs/0QpLjuf6c7CwimbGx3O53Fa/LPBH6xcinja1nIPVv5lgUaabEPVNrr+f1cv0OCj/O32F/gEHQUy01Z1s7QHAtzPa+j8wZybuyldUmcDXy0zr7w6OkoN6QMThB4TlY1M/sWyXiqZ8PjeIF+XGIDt1n2k+JqH5RHPpsqFVdRC6aKt/Wl2/N7sUsvWS60uJlNdGl8TvPr6Gf5IJ9RjVzW1yONwXt36ocBCbSN27WIzabE+RFnoasIFoOCkVusSyAnPyODyT2czcX7vPMCZTPTord+Wy4hPVR8WE22uzeX2UG9YeKPZmadqpsRCcSi0WwdLMH08fjzd+GRYfZv4MuFy/CmKLIyM+X9Qd8vpxUX+UikoI8EpOZ5kp4b/Oq/4QPNQlHistgJeTtZlqYpq2FPMt+0JZ3aLeBOvLEhM69TzafzVlmyePCUzb0TzGvAXaDM6zU7m3U/Jj8wN+Wi+LPtbnVX2mx9GheT/wwOgQug+Y+qNq8XmGGcZol3yKbpcGJJ25wJahZSqapWuLlEjZpCU2gZeyEw/PBYjIJuRFbSmZVoxmbTadKaXFBEVF/QY4OnsT/LvERuljAesQNzWFPH8RcW5JF+o0nYoW2cGQIY5exKiNwvOybVm27zCTtkPurU2BwP02n2xWm60c6HcXKTaastHGPNTsJj6P3wZZVjQMmsZGk5nZfYnhv+yFl4NU8+pDfdFHHWZLvGcgBK8Xku2JqQjF2zgPGxvNZlGuhcjxFymfpH93eN1lN32oDAwAxkv8B1dRls0ZN4vG783jwBska5IKUhIvVCnWLR3O8yJPKi5JChCioIlpN872JkibvxOLhSp4biycku1wXjaF/k4skftAVQoses4wYBjmCkstEx2k4nx53vmXIF7x7Z2K1M6/Trz3RlFLoxDFbjGnjID1zBr4TwmThOa/lc+vRu12K4jdbh+d2xwyBsovJw1mtaiOUuBCk2E5u49OInd2DhLe0NwGzwNIBdp5nt9ApjHw5tVSGvkodtwQWM/AcDKiPHdJrlqZUQcAKoG8wzGXMQI6L4yU5S3kLMM01p6ITjNMcXZWqY5PQwTncBQKBSEoEokFV4W8Av5cZlY0sU0ROlEex9AAenoMRrBTTG52dkl5s14w5qQDRVjgOIvFwnHPdg44EMtCLC9rOlcCbk5mNOBAqDyIKOquIZBhpmdnFwlQekoPbSAuLnIWGWg52B7aloEgsTCadmNICSrwMj83RHjSiC4rKaqvxyDtQUF2dlCxvURvrgNu98zYmALhDg7e7ajAZDIYBsi67MiMQ/bzKCGGvMcFms0vKWpRMxIu8k9LmXDcj3vbhCeLgME0NzkJfnbJgbR+DX0Z1U8ELNvP1NcMmhNmAUq1pPEAGOP0QG5lx1IicgKJXuC5YrEgRO76NafzYmcN4Hc1LWq+Qmm17BrYMxzjKoG/7JUDgSgDXbtiMrawkJeJUWc1EPYWNbYzreZpilLbziuHI/+EqwJyQ79YyoBcDIEuYWwsyQtJmdh5sRq4O7NbYzvTjFVmULkvw/PhJ5YasrOi/x5EoghoQeC4hbDVMbYXqAT21d7OnGWEUpXZqPSfKs9sP+p/WFWICASiwzFaBSQbKOYPFcAW5jrsEbQIDXM1gZaVdxU/CACEm8MRtHgMMmmuGtg7iD2xuXJygjp6UfUgLxgAd2zPahCFcCQSQ5cHgbhSCewNjLBQwc/qgecY2KVkSbV3QZzXxFm4Z/S7gzFSDLpB8LewlcgCXHfHC4X1sk5iy0ITpekRHL4rgAwD70K6dIZ3uUQDIHeN3h4biwWDJaCoAPNA7O7OF8prrM0m++kI4CQAnxgBd+idWNwRiYRFFcgJBFjII3ChwPObOmDnkcBXYFKLEXBbLgoRKGWiCuSIUQuFOAC7Y6WiToD0kcANaLY1kh5lLZ4fCkDX+gjAvAYUC6RB24N4T7gsUp3OpSUo6DB+XWdaDIF2I6AoQDueDFxbm/Dchjq6qgK5VWUisEPr7B6LlCLV6fz06UhgBh8Wq4AWcddqxTwLBN73eTw/89a8BuTCyggSh+tk3OHY0IBRN/ZkFka3uopSysBu128EFIPhCC7oiM9MBAL3+j54XsSt1pgGFBUgvwoqJsGomyrQ7Zbn8Oot4kkz1Dy5d37G8WGV0yQWjOfJ4PTx+kQ6nf4HTb+5ezc9A/W2W4vUIM/LL+TAwhqM8JCMJGguYAPy1zjIqDMPK5VmEoHYm+DFxfE4j4MG8IR/vQBa+tFjr9N5b+lu+sUqzwc1ILQqAozLFub5SQWYlTseuIthKtqvGWbgeQ0IAEFQR0IYpNZu35FxXV1db8BQf4PrnxUVFbOGCdCBkSrCY0PKxC4DfVV50WrG87LBMiCvzaDxtVt3QNL7iCPAwN5v6Re6XOSCCjCPKuJQpQAH8HOeZdeYE2W875kmuYtgMZ1TShXCIquxmYmOjo70/v6jx11E/k3i4deHTyKRONRVNbDiPPEjNqoYfK6UbWJ7WXZGlxfnmWmY8dkETkCjCvDW8+fPPR5PB4oKA3n8Rl1lKJP5y872tpo5ZxSgFZIxCQYeLQNW5kU7Klj0stIlSQd88PbtvqcM+PjRy74770vdZ2hu++BAy9U4ARZAxSSoaM+UgBcq9vl1MJKyUUgZnJonNeADhCDQ8+DDhw/7++k0+vK1Vrcy645gyaQclyfAwgKXBBXto8bAcz3QDFOwJ3D6nCWgxwOKPSI2VQRM7FEnyUDgvw67UF6Ngg7ie/Ri0GEHFbVjD5a9UjbVtOMA9RMNAex0e1Wg/bqHiB5Ysmjg/a0KIJdXgg0DFfYfoxrwqr6YtuE8My9hiEpup6phsibwN63XBdJVwKCdAIPyNaio7GjoT/o8rGegrKXkQup0+xWga7wGsOPX169LwOuVQCjiMlDORdwtq/fKQFPZrqKLZSVSbKUQ7E1kX7heVMAUJTs6Hkq4NXQOxyNYV3RzKvRufBYLI05XGZIaKbcuLZrNkBTL6sbSrQBd9pmaQECmH+5hFZmLRITKvqkABaWDKLnozuIJvHaW8TWeBSVUYOq1quFHAyAhOp2bkThXCYwTIK82SZKLIxd0/anOPMOyV7Wtc4gA7S7XhAGw407/8B7c+Z8nVUBRAa4qKs4hsNPtxoNlLUobzP0QM6UQVjS0u8Rq3YjcivHrUCp3DqqHLEFu1NZwMmmxQDF/hSve9CfwgM+kA5aO/m+sESC8561awI7bwQjZ8K7UAMYI0BojwEn5sJb+K8t+y5wuBSlUbn/pSEtUgVaxBvD2KraR+Fjy2VDlzkZOjV2roqIGDF3Ux8z/WjX/p6ayK4DDIOSBri501FW3+94cZJTE+AAYE0jSCHnEIMQkpLtCgjgBUihFCBgiClCiAB02QQuoYS3TBem0OFvLamd2M+3Odv+z3nPv+xaAnV3X90OSvPvl8+6555x77n2HAA0AFRrw9yrw0vwe4KP+642NjRfu3SDX3/9TcfcA4D257TUKRNMf4XOtggVsuh3ILRV4/vq3gX/I83a568HtqvPEsFE3We9/5v9Wvn/TI8eoVbgMN5JNeK2ZHzUYSrgjhwEr/qQC/9B44SKJ+9lFhkbm51lVjQopv/vvA4BVcoyKgUYj0fs2fthg6OO4vENH+Kk2QrJR0AGvXsfoUTEF8uvx3cf7gdfkkPEeWZEf8kbRydcZDLe5osOBdzUgDTOu/gavmnLdpUzZ46/2A29clWPU6xjYGJvwrMbAcbr9aENDrkgrzqsXzsfVO4dsTunx1AEbHjlGxesWPyXZswDzOW+7fhxYVV7+I8ADd1gq8OKnxnFjGgBOC2U5wGV5sTgIWP7zgddVoHFKNHowYst5nVfY4KDbxvcGvCMDL35mbDJKANVPcwaYV4Trr+baKioeXtKQ538WTDb+KnYkVnlruBbf7ew9pNkH/EwPrHkHYI18Bne3qaIOT/X3HJnkC/0A5vcJpEthZeVbW73TYICSvadQBe8feI8CvxrsICZ4Wyjcc3x5jDsN0KO+FyTA85cusUNXBOqNfe+3fv+fY/zY+ML3HR6A5oaGI/uPZhVDfG9A4mUav7G1AUAJvu/HE8z8Y7q9WrO8+z0AmOPPdMCc/b/mX9mtG9dI43UJgGioDBQ4riBn99vjAYCRJpEBfzc/C7NduOcrn4//ta+393nXg3Ls/H68F6D38nePZtW0BOjd+a68nNyong0wbOByLyzPbgKafKeDZUvESzViGVel5QFsp+1vK+d72avYbyorHWpJ182bdwLLe/MG6Kv26gBrAPM3b96e/0Itaxa+1OrFFfM4xnHk9hxG3i/MAJD+YwBge8OeblvqrZwFmHO7RNE5jURSkJ2y28U2gDl1U2N07aJw5lZe25B4m/BsL+0rc6MAX2LHK3Y8Q3UbDKVy4HacKwWQUkNEzpORiQ3wvH0FG+FVwWTvgQCMSsmZGZMpOeYG6FqGuahXEExhc0+4Rc3PMbkA5qzRoaHoOHx983Pw8JE1oSUZlqCvGjbGWEWfZHCwuKaAK+0GaUxu3DKRhS7whPH3jBtmYW5RLiHEashGafNYq3lGlxEUHgEe7w/Vw/L9L0B6wm6nNgEGV5VKRoOBnWTgO0ObeluYcYEDnIxiBIBak1ISIoJs97LfT9w+HTDa08Y6SAWhKzHaKiiVAMwWZST18lnNCeFMH9RP6uVTDfVD7Mk9eqB3CmDFL/9JGb2Tk9jK5PU+kcDNBjw2CK8SygCFlisM2GJaW1tLjUMXfXN5RCiFthCtPTExQ+UIPVFtTAg0hS1EYkM8tKHkY5EYPr3L5bKSfxXpdOYN/IVN1OIcVCdaye8Y76ODokBTrcs1VQfwjJ6c5AsBJkGrc6nH7fO1jgDYZHlZzDIw7UTJ8PAvAmxptbUycW9SIE2cmNDUZ9dKhzpsjVjNCpBa62k2hbgbdZFOY/V412xegj76f4hI1dsOICKw1hah007pLnhJZBkdgTorfQr4LXtCUtbCy7Nm90DQFgQV2Bx/wHHHz+qB+HD0ygLwOONrVOtkoHuRAq8QafnGQUrS0SvAXhCJkIcsbNYRMfQaMH+pL6ACzxDXll/MXlgEYBxndqI90wMATfXgwY4sOKZWGViPSjjpMhgFCrRZcoDVcAV1Cv+ttsHwDM4BODo/7xQCmkibawhROVEYp7q76It2NOEYp7H7KCqSdZMCW6hK+KYPATKzteDMRmwgTlLg15gr9FwGBoNviGtjwelJrsTQF/b7qb7Hxl4MAkhobDxqqs9GgcwqjAMDPNMk8x5gEypvFOVNTIfOswjQQ+SVSODkx6xWa6vbA7JrO8o1G5wpv2xgsdYs1HvRdhA0M6UDSp7DgM4k6owJTeefMrBNjEZ5fpgCmcVIILu2o1ynAdypIaVfF9UZGd+hAf1e6TCg5hyE8BIDhvFWC68BBbtySHuCw4QP28qYjzFD2ZDmtHgd0G/0HAKs0LnVIAJnpiJUYqFdFYiKy46hyz4RSh5hjtX4Czua04wU0XtJ3dOPNSEwsg84EtUa+GxoY6FMSPYc1EgsPp8v2pxo9mP4du4ToTMhm+BSfQzFQf2ONYX0sWkExux2OylZHJaB6MJ1QOq5fUQz6AqDdUIZKWRRgcRv2Gw9iUSJv4CaRWcikXW5shDvY0bXQfR6TdwMog0k52TnMu7DhjLwZSwHSHWG39wU13DWcYRkujMVOiDLBvX/msXdZKzWtTXqaCiQZ144gUvGZO1+oJNqlbVOBWIrol5s6eRx+N7XAwN7gI5Ov5/m8XwkVMMKUeiQs66uDhkmMiP+0DTAFFoHT59BDNIvF1aMdQTDzIkpQFx8ce2bxkmIZvDTaguiH1j9HyqNSaQphqUCPTnB/a8dVdhCJgFXMosFgR4AM0oqTB/fak3StTmNK2DSSj4n0wAycHSCuQjIpnDWM/g4k0na12qQAvGES1IyBk8Jr+BFTL98IxCVfRD9R0rU2dj2lFf5TWZSAQbxK4x+mFpN5r/qat6yAqpZjDkxhUB++TuoszwaPBDr34E3+Kg+Dbi4AcGUYlX2pVygPQF9gDPi3WhT46tFpwYk8G6awHeE+FKDpBL94QyRnnc4sb6zlfZqFkUEYx8FcLNgyx+SqM8mLvg19KRwPenujVOtiUyDFPJT3+y1e2A7uheYV0hc24BkDHlN5PKGN7rt3if8aKJ8fWvbHgoNLwyHvFjkS2P0Ce4oiRm9lAewEfKGMgBS0hQ1d8dLYVSKRLBEak2aTKur6W3wwIaPNMaYRhFpXjFxbQMDA5l2URRdrm3o3m2XRhM/+J9ubXkyuwsLC7vpNCmysdxZGHWL4lQW9bwLqjPp3QWAHqk92N39THDAks0GsA7gcYpiZntra/k+VNtovxjTdLKMwXOYKrszMCB7Gw8RHCR+eOj3P9zZWlgYfUuQCzRU7/3eoAX43/oxvMSy088Buru7ScQi0HzvdaFzB7/J864/JXV02d7KgeLZXwlCSX+Xw+Fobu5vaHgQj98XhKIiQagKlJ7hngYcePWf5jiOdHqf1Is/wP0XljsCJWSjEMAbpz4QhJr+fppdXkraYFvys6qf9tvcf0Y4rqa5nMs/ruVu070Vnv2Rm4RRqKgTCYI4pQ55nrK8AlrOFbIHyT+R9yHt5VTBBx+x+lxhWcFxTf/zi4tzEroLyXX06IcnTxZ8/LGc3V1WSDavR8j9oqKi/DJdPbW8kNUoKjhLOyvLpyUnsE0h2/iW5RfRi9QoLj4gLf5d0tt/Wtt37v+XXf8HYpztme1nOY8AAAAASUVORK5CYII=");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        Bitmap bm = BitmapFactory.decodeFile(file.getPath());
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
//                        byte[] b = baos.toByteArray();

//                        return Base64.encodeToString(b, Base64.NO_WRAP);
                        Log.e("File Info",body.toString());
                        CometChat.callExtension("avatar", "POST", "/v1/upload", body,
                                new CometChat.CallbackListener < JSONObject > () {
                                    @Override
                                    public void onSuccess(JSONObject jsonObject) {
                                        try {
                                            avatarUrl = jsonObject.getJSONObject("data").getString("avatarURL");
                                            if (avatar!=null)
                                                avatar.setAvatar(avatarUrl);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        // {avatarURL: "https://data-us.cometchat.io/avatars/1a2b3c.jpg"}
                                    }
                                    @Override
                                    public void onError(CometChatException e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                }
            }
        }
    }
}
