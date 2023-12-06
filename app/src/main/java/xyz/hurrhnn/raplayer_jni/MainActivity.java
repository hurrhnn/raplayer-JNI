package xyz.hurrhnn.raplayer_jni;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;

import java.util.HashMap;
import java.util.Map;

import xyz.hurrhnn.raplayer_jni.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    private void bufferCallback(byte[] frame, AudioTrack audioTrack) {
        audioTrack.write(frame, 0, frame.length);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        LinearLayout linearLayout = binding.lroot;


        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0,1);
        params1.bottomMargin=5;

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        params2.leftMargin=10;

        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1);
        params3.gravity= Gravity.RIGHT;
        params3.rightMargin=10;

        RequestThread pushprofile = new RequestThread(getApplicationContext(), "GET", "profile", "");
        pushprofile.start();
        try {
            pushprofile.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONObject profile = pushprofile.getResult();
        System.out.println(profile.toString());


        RequestThread getRoom = new RequestThread(getApplicationContext(), "GET", "", "");
        getRoom.start();
        try {
            getRoom.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        JSONObject jsonObject = getRoom.getResult();
        try {
            JSONArray jsonArray = (JSONArray) jsonObject.get("data");
            for(int i=0;i<jsonArray.length();i++){

                JSONObject roomObj = (JSONObject)jsonArray.get(i);
                String title = roomObj.get("title").toString();

                LinearLayout room_root = new LinearLayout(this);
                room_root.setLayoutParams(params1);
                room_root.setOrientation(LinearLayout.VERTICAL);
                room_root.setBackgroundColor(Color.GRAY);

                TextView titleText = new TextView(this);
                titleText.setLayoutParams(params2);
                titleText.setTextSize(20);
                titleText.setText(title + " - " + roomObj.get("userid").toString());

                TextView playText = new TextView(this);
                playText.setText(roomObj.get("cnt").toString()+"/"+roomObj.get("limit").toString());
                playText.setLayoutParams(params3);
                playText.setTextSize(20);

                room_root.addView(titleText);
                room_root.addView(playText);
                linearLayout.addView(room_root);

                room_root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("title",title);
                        try {
                            bundle.putString("userid",roomObj.get("userid").toString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        PopupFragment popup_fragment = new PopupFragment();
                        popup_fragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.root, popup_fragment).addToBackStack(null).commit();
                    }
                });

            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


//        for(int i = 0; i < 50; i++) {
//            String title = Integer.toString((int)(Math.random() * 899999) + 100000);
//            LinearLayout room_root = new LinearLayout(this);
//            room_root.setLayoutParams(params1);
//            room_root.setOrientation(LinearLayout.VERTICAL);
//            room_root.setBackgroundColor(Color.GRAY);
//
//
//            TextView titleText = new TextView(this);
//            titleText.setLayoutParams(params2);
//            titleText.setTextSize(20);
//            titleText.setText(title);
//
//
//            TextView playText = new TextView(this);
//            playText.setText("1/8");
//            playText.setLayoutParams(params3);
//            playText.setTextSize(20);
//
//            room_root.addView(titleText);
//            room_root.addView(playText);
//            linearLayout.addView(room_root);
//
//            room_root.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString("title",title);
//
//                    PopupFragment popup_fragment = new PopupFragment();
//                    popup_fragment.setArguments(bundle);
//                    getSupportFragmentManager().beginTransaction().replace(R.id.root, popup_fragment).addToBackStack(null).commit();
//                }
//            });
//        }





//        TextView textView = findViewById(R.id.main_text);
//        TextView logView = findViewById(R.id.logging_text);
//        logView.setTextColor(Color.WHITE);
//
//        EditText addressEditText = findViewById(R.id.address_edittext);
//        EditText portEditText = findViewById(R.id.port_edittext);
//        Button startButton = findViewById(R.id.main_start_button);
//
//        textView.setText(stringFromJNI());
//        startButton.setOnClickListener(new View.OnClickListener() {
//            int count = 0;
//
//            @Override
//            public void onClick(View v) {
//                AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, //sample rate
//                    AudioFormat.CHANNEL_OUT_STEREO, // 2 channel
//                    AudioFormat.ENCODING_PCM_16BIT, // 16-bit
//                    960 * 2 * 2, AudioTrack.MODE_STREAM);
//
//                audio.play();
//
//                String address = addressEditText.getText().toString();
//                String port = portEditText.getText().toString();
//
//                String text = logView.getText().toString() + " [" + new Formatter().format("%02d", ++count).toString() + "] Connecting to " + address + ":" + port + "...\n";
//                logView.setText(text);
//
//                class ClientThread extends Thread {
//                    private final String address;
//                    private final int port;
//
//                    public ClientThread(String address, int port) {
//                        this.address = address;
//                        this.port = port;
//                    }
//
//                    public void run() {
//                        startClientFromJNI(address, port, audio);
//                    }
//                }
//                ClientThread clientThread = new ClientThread(address, Integer.parseInt(port));
//                clientThread.start();
//            }
//        });
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        menu.add(0, 1, 0, "Create Room");
        menu.add(0, 2, 0, "Stats");
        menu.add(0, 3, 0, "Profile");
        menu.add(0, 4, 0, "refresh");
        return super.onCreateOptionsMenu(menu);
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



    //menu event
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent intent1 = new Intent(this, CreateRoomActivity.class);
                startActivity(intent1);
                return true;
            case 2:
                Intent intent2 = new Intent(this, StatsActivity.class);
                startActivity(intent2);
                return true;
            case 3:
                Intent intent3 = new Intent(this, ProfileActivity.class);
                startActivity(intent3);
                return true;
            case 4:
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    public native String stringFromJNI();

    public native boolean startClientFromJNI(String address, int port, AudioTrack audio);
}
