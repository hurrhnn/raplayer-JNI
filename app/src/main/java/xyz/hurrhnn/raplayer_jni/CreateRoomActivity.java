package xyz.hurrhnn.raplayer_jni;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import org.jsoup.HttpStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.hurrhnn.raplayer_jni.databinding.CreateRoomBinding;

public class CreateRoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateRoomBinding createroombinding = CreateRoomBinding.inflate(getLayoutInflater());
        setContentView(createroombinding.getRoot());

        createroombinding.createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.N)
            @RequiresPermission(value = "android.permission.RECORD_AUDIO")
            public void onClick(View v) {
                // net link address check
                String address = null;
                Object connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager instanceof ConnectivityManager) {
                    LinkProperties link = (LinkProperties) ((ConnectivityManager) connectivityManager).getLinkProperties(((ConnectivityManager) connectivityManager).getActiveNetwork());
                    if(link != null) {
                        List<LinkAddress> addressList = link.getLinkAddresses();
                        Log.e("CreateRoom", addressList.toString());

                        String ipv4Regex = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
                        Pattern pattern = Pattern.compile(ipv4Regex);
                        List<String> ipv4Addresses = new ArrayList<>();

                        for (Object input : addressList) {
                            Matcher matcher = pattern.matcher(input.toString());
                            while (matcher.find()) {
                                ipv4Addresses.add(matcher.group());
                            }
                        }
                        address = ipv4Addresses.get(0);
                    } else {
                        Log.e("CreateRoom", "Could not determine network address!");
                        return;
                    }
                }

                String pw = "";
                String title = createroombinding.roomTitle.getText().toString();
                String limit = createroombinding.limitPlayer.getText().toString();
                if (createroombinding.pwSw.isChecked()) {
                    pw = createroombinding.pwEt.getText().toString();
                }
                Boolean mic = createroombinding.micSw.isChecked();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("title", title);
                    jsonObject.put("limit", Integer.parseInt(limit));
                    jsonObject.put("password", pw);
                    jsonObject.put("mic_opt", mic);
                    jsonObject.put("server_ip", address);
                    jsonObject.put("server_port", 3845);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestThread createroom = new RequestThread(getApplicationContext(), "POST", "create", jsonObject.toString());
                createroom.start();
                try {
                    createroom.join();
                    if (createroom.getResult() == null) {
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Raplayer raplayer = new Raplayer();
                long spawn_id = raplayer.spawn(false, "0.0.0.0", (short) 3845);
                long provider = raplayer.registerMediaProvider(spawn_id);
                long consumer = raplayer.registerMediaConsumer(spawn_id);

                Log.e("CreateRoom", ("id: " + spawn_id + ", provider: " + provider + ", consumer: " + consumer));

                Intent intent = new Intent(CreateRoomActivity.this, RoomActivity.class);
                intent.putExtra("title", title);
                startActivity(intent);
            }
        });

        createroombinding.pwSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    createroombinding.pwEt.setVisibility(View.VISIBLE);
                } else {
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
