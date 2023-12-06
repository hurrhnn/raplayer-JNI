package xyz.hurrhnn.raplayer_jni;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import org.jsoup.HttpStatusException;

import java.util.HashMap;

import xyz.hurrhnn.raplayer_jni.databinding.CreateRoomBinding;

public class CreateRoomActivity extends AppCompatActivity {
    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateRoomBinding createroombinding = CreateRoomBinding.inflate(getLayoutInflater());
        setContentView(createroombinding.getRoot());

        createroombinding.createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(View v) {
                String pw = "";
                String title =  createroombinding.roomTitle.getText().toString();
                String limit = createroombinding.limitPlayer.getText().toString();
                if(createroombinding.pwSw.isChecked()){
                    pw = createroombinding.pwEt.getText().toString();
                }
                Boolean mic = createroombinding.micSw.isChecked();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("title", title);
                    jsonObject.put("limit", Integer.parseInt(limit));
                    jsonObject.put("password", pw);
                    jsonObject.put("mic_opt", mic);
                    jsonObject.put("server_ip", "123.123.123.123");
                    jsonObject.put("server_port", 1234);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestThread createroom = new RequestThread(getApplicationContext(), "POST", "create", jsonObject.toString());
                createroom.start();
                try {
                    createroom.join();
                    if(createroom.getResult() == null){
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(CreateRoomActivity.this, RoomActivity.class);
                intent.putExtra("title", title);
                startActivity(intent);
            }
        });

        createroombinding.pwSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    createroombinding.pwEt.setVisibility(View.VISIBLE);
                }else{
                    createroombinding.pwEt.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
