package xyz.hurrhnn.raplayer_jni;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xyz.hurrhnn.raplayer_jni.databinding.ProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileBinding profilebinding = ProfileBinding.inflate(getLayoutInflater());
        setContentView(profilebinding.getRoot());

        profilebinding.profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                // server에 보내고 앱 내부 db에 저장하기
                String nickname = profilebinding.username.getText().toString();
                String password = profilebinding.password.getText().toString();
                String introduction = profilebinding.introduction.getText().toString();
                Toast.makeText(getApplicationContext(), "nickname: " + nickname + "\npassword: " + password + "\nintroduction: " + introduction, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
