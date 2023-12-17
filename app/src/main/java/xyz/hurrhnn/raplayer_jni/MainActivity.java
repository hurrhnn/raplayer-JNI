package xyz.hurrhnn.raplayer_jni;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import xyz.hurrhnn.raplayer_jni.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    private void bufferCallback(byte[] frame, AudioTrack audioTrack) {
        audioTrack.write(frame, 0, frame.length);
    }

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(binding.loginPw.getText().toString());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                    jsonObject.put("password", binding.loginPw.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                RequestThread login = new RequestThread(getApplicationContext(), "POST", "login", jsonObject.toString());
                login.start();
                try {
                    login.join();
                    if (login.getResult() != null){
                        System.out.println(login.getResult().getString("msg"));
                        pref = getApplicationContext().getSharedPreferences("jwt", MODE_PRIVATE);
                        editor = pref.edit();
                        editor.putString("jwt", login.getResult().getString("msg"));
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), RoomListActivity.class);
                        startActivity(intent);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        binding.setpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SetPasswordActivity.class);
                startActivity(intent);
            }
        });

        binding.serverChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChangeServerActivity.class);
                startActivity(intent);
            }
        });
    }
}