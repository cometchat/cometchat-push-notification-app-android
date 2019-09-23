package com.inscripts.cometchatpro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;

import static com.inscripts.cometchatpro.Constant.API_KEY;
import static com.inscripts.cometchatpro.Constant.GUID;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         if (CometChat.getLoggedInUser()!=null){
             startActivity(new Intent(this,MainActivity.class));
             finish();
         }
         else {
             setContentView(R.layout.activity_login);
             initView();
         }
    }

    private void initView() {

        Button btnSuperhero1 = findViewById(R.id.superhero1);
        Button btnSuperhero2 = findViewById(R.id.superhero2);
        Button btnSuperhero3 = findViewById(R.id.superhero3);
        Button btnSuperhero4 = findViewById(R.id.superhero4);

        btnSuperhero1.setOnClickListener(this);
        btnSuperhero2.setOnClickListener(this);
        btnSuperhero3.setOnClickListener(this);
        btnSuperhero4.setOnClickListener(this);
    }

    private void login(String UID){

        CometChat.login(UID, API_KEY, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {

                MyFirebaseService.subscribeUser(user.getUid());

                MyFirebaseService.subscribeGroup(GUID);


                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();

            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.superhero1:
                Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();
                login("superhero1");
                break;

            case R.id.superhero2:
                Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();
                login("superhero2");
                break;

            case R.id.superhero3:
                Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();
                login("superhero3");
                break;

            case R.id.superhero4:
                Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();
                login("superhero4");
                break;
        }
    }
}
