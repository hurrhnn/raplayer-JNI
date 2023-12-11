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


        RequestThread pushprofile = new RequestThread(getApplicationContext(), "GET", "profile", "");
        pushprofile.start();

        class getroomThread extends Thread{
            private JSONArray roomlist;

            public void setValue(JSONArray roomlist){
                this.roomlist = roomlist;
            }

            @Override
            public void run() {
                super.run();

                class asdfRunnable implements Runnable{

                    JSONArray inroom;
                    @Override
                    public void run() {
                        RequestThread getRoom = new RequestThread(getApplicationContext(), "GET", "", "");
                        getRoom.start();
                        try {
                            getRoom.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        if(getRoom.getResult() != null) {
                            JSONObject jsonObject = getRoom.getResult();
                            try {
                                JSONArray troomlist = (JSONArray) jsonObject.get("data");
                                if(troomlist.length() != inroom.length()){
                                    inroom = troomlist;
                                    linearLayout.removeAllViews();
                                    createlayout(linearLayout, troomlist);
                                } else {
                                    for(int i=0;i<troomlist.length();i++){
                                        JSONObject roomObj = (JSONObject)troomlist.get(i);
                                        JSONObject inroomObj = (JSONObject)inroom.get(i);
                                        if(!roomObj.get("cnt").toString().equals(inroomObj.get("cnt").toString())){
                                            inroom = troomlist;
                                            linearLayout.removeAllViews();
                                            createlayout(linearLayout, troomlist);
                                            break;
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                asdfRunnable asdf = new asdfRunnable();
                asdf.inroom = roomlist;
                while(true){
                    try {
                        runOnUiThread(asdf);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        RequestThread getRoom = new RequestThread(getApplicationContext(), "GET", "", "");
        getRoom.start();
        try {
            getRoom.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(getRoom.getResult() == null) {
            postToastMessage("서버와 연결할 수 없습니다.");
            return;
        }

        try {
            JSONArray jsonArray = (JSONArray) getRoom.getResult().get("data");
            createlayout(linearLayout, jsonArray);
            getroomThread getroomthread = new getroomThread();
            getroomthread.setValue(jsonArray);
            getroomthread.start();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createlayout(LinearLayout linearLayout, JSONArray jsonArray){
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

        try {
            for(int i=0;i<jsonArray.length();i++){

                JSONObject roomObj = (JSONObject)jsonArray.get(i);
                String title = roomObj.get("title").toString();

                LinearLayout room_root = new LinearLayout(getApplicationContext());
                room_root.setLayoutParams(params1);
                room_root.setOrientation(LinearLayout.VERTICAL);
                room_root.setBackgroundColor(Color.GRAY);

                TextView titleText = new TextView(getApplicationContext());
                titleText.setLayoutParams(params2);
                titleText.setTextSize(20);
                titleText.setTextColor(Color.BLACK);
                titleText.setText(title + " - " + roomObj.get("userid").toString());

                TextView playText = new TextView(getApplicationContext());
                playText.setText(roomObj.get("cnt").toString()+"/"+roomObj.get("limit").toString());
                playText.setLayoutParams(params3);
                playText.setTextColor(Color.BLACK);
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
