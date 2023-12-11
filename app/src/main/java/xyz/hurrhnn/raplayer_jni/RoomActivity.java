package xyz.hurrhnn.raplayer_jni;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import xyz.hurrhnn.raplayer_jni.databinding.RoomBinding;

public class RoomActivity extends AppCompatActivity {
    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    myThread myThread = new myThread();



    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoomBinding roombinding = RoomBinding.inflate(getLayoutInflater());
        setContentView(roombinding.getRoot());

        DatabaseHelper userDB = new DatabaseHelper(this);
//        userDB.onUpgrade(userDB.getWritableDatabase(),1,2);

        String message = this.getIntent().getStringExtra("title");
        String event = this.getIntent().getStringExtra("event");
        String server_userid = this.getIntent().getStringExtra("server_userid");
        String user_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        TextView title = roombinding.roomTitle;
        title.setText(String.format(title.getText().toString(), message));
        LinearLayout inroomRoot = roombinding.inroomRoot;
        boolean endflag = false;


        if(Objects.equals(event, "join")) {
            RequestThread getRoom = new RequestThread(getApplicationContext(), "GET", "room/"+server_userid, "");
            getRoom.start();
            try {
                getRoom.join();
                JSONArray inroom_user = getRoom.getResult().getJSONArray("inuser");
                for(int i=0;i<inroom_user.length();i++){
                    RequestThread getinroom_user = new RequestThread(getApplicationContext(), "GET", "user/"+inroom_user.get(i), "");
                    getinroom_user.start();
                    getinroom_user.join();

                    if(getinroom_user.getResult() == null){
                        Toast.makeText(getApplicationContext(), "알수없는 오류 발생", Toast.LENGTH_SHORT).show();
                        endflag = true;
                        break;
                    }
                        JSONObject jsonObject = getinroom_user.getResult().getJSONObject("data");
                        String username = jsonObject.getString("username");
                        String idk1 = jsonObject.getString("introduction");
                        String img = jsonObject.getString("img");
                        createlaylout(username, idk1, img, inroomRoot,jsonObject.getString("userid"));

                }
                myThread.setValue(server_userid, inroom_user, inroomRoot, true);
                myThread.start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("in data base in data");
            String userid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Cursor res = userDB.usergetData(userid);
            if (res.getCount() != 0) {
                while (res.moveToNext()) {
                    createlaylout(
                            res.getString(1),
                            res.getString(3),
                            res.getString(4),
                            inroomRoot,
                            user_id
                    );
                }
            } else {
                System.out.println("nononnonononono data base in data");
                RequestThread getprofile = new RequestThread(getApplicationContext(), "GET", "profile", "");
                getprofile.start();
                try {
                    getprofile.join();
                    JSONObject jsonObject = getprofile.getResult().getJSONObject("data");
                    createlaylout(
                            jsonObject.getString("username"),
                            jsonObject.getString("introduction"),
                            jsonObject.getString("img"),
                            inroomRoot,
                            user_id
                    );
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(endflag);
            JSONArray inroom_root = new JSONArray();
            inroom_root.put(userid);
            myThread.setValue(userid, inroom_root, inroomRoot, true);
            myThread.start();
        }
        System.out.println(endflag);
        if(endflag){
            myThread.setValue("", new JSONArray(), null, false);
            finish();
        }

        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        String starttime = mFormat.format(mDate);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String roomid = server_userid!=null ? server_userid:user_id;
                showExitDialog(roomid, starttime);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    class myThread extends Thread{
        private JSONArray inroom_user;
        private String server_userid;
        private LinearLayout inroomRoot;
        private boolean condition = true;

        public void setValue(String server_userid, JSONArray inroom_user, LinearLayout inroomRoot, boolean condition){
            this.server_userid = server_userid;
            this.inroom_user = inroom_user;
            this.inroomRoot = inroomRoot;
            this.condition = condition;
        }

        public void run() {
            class asdfRunnable implements Runnable{
                int cnt = inroom_user.length();
                @Override
                public void run() {
                    JSONArray tinroom_user = new JSONArray();
                    RequestThread getinRoomuser = new RequestThread(getApplicationContext(), "GET", "room/"+server_userid, "");
                    getinRoomuser.start();
                    try {
                        getinRoomuser.join();
                        JSONObject jsonObject = getinRoomuser.getResult();
                        if (jsonObject != null) {
                            tinroom_user = jsonObject.getJSONArray("inuser");
                            if (tinroom_user.length() != cnt) {
                                cnt = tinroom_user.length();
                                for (int i = 0; i < tinroom_user.length(); i++) {
                                    RequestThread getinroom_user = new RequestThread(getApplicationContext(), "GET", "user/" + tinroom_user.get(i), "");
                                    getinroom_user.start();
                                    getinroom_user.join();
                                    System.out.println(getinroom_user.toString());

                                    if(getinroom_user.getResult() != null){
                                        if(i==0) inroomRoot.removeAllViews();
                                        JSONObject jsonObject1 = getinroom_user.getResult().getJSONObject("data");
                                        String username = jsonObject1.getString("username");
                                        String idk1 = jsonObject1.getString("introduction");
                                        String img = jsonObject1.getString("img");
                                        createlaylout(username, idk1, img, inroomRoot, jsonObject1.getString("userid"));
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            asdfRunnable asdfRunnable = new asdfRunnable();
            asdfRunnable.cnt = inroom_user.length();
            while (condition) {
                try {
                    runOnUiThread(asdfRunnable);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void createlaylout(String username, String idk1, String img, LinearLayout inroomRoot, String id){
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0,1);
        params1.bottomMargin=5;
        LinearLayout linearroom = new LinearLayout(this);
        linearroom.setLayoutParams(params1);
        linearroom.setBackgroundColor(Color.parseColor("#979AB1"));


        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                316, 316);
        params2.setMargins(10,10,10,10);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_launcher_foreground);
        imageView.setLayoutParams(params2);


        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout linearprofile = new LinearLayout(this);
        linearprofile.setLayoutParams(params3);
        linearprofile.setOrientation(LinearLayout.VERTICAL);


        TextView usernameTx = new TextView(this);
        usernameTx.setText(username);
        usernameTx.setTextSize(20);
        usernameTx.setTextColor(Color.parseColor("#000000"));
        params3.setMargins(20,0,0,0);
        usernameTx.setLayoutParams(params3);

        TextView idk = new TextView(this);
        idk.setText(idk1);
        idk.setTextSize(20);
        idk.setTextColor(Color.parseColor("#000000"));
        params3.setMargins(20,0,0,0);
        idk.setLayoutParams(params3);

        View view = new View(this);
        LinearLayout.LayoutParams viewp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 2);
        viewp.setMargins(0,0,5,0);
        view.setLayoutParams(viewp);
        view.setBackgroundColor(Color.BLACK);

        LinearLayout microot = new LinearLayout(this);
        microot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        microot.setGravity(Gravity.CENTER);

        Switch micsw = new Switch(this);
        params3.setMargins(10,10,0,0);
        micsw.setLayoutParams(params3);
        if(!id.equals(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID))){
            micsw.setClickable(false);
        }


        TextView mic = new TextView(this);
        mic.setTextColor(Color.parseColor("#000000"));
        mic.setText("MIC");
        mic.setTextSize(20);
        mic.setLayoutParams(params3);

        if(!Objects.equals(img, "")) {
            Glide.with(this).load(img)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        linearprofile.addView(usernameTx);
        linearprofile.addView(idk);
        linearprofile.addView(view);
        microot.addView(mic);
        microot.addView(micsw);
        linearprofile.addView(microot);
        linearroom.addView(imageView);
        linearroom.addView(linearprofile);
        inroomRoot.addView(linearroom);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showExitDialog(String roomid, String starttime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setMessage("방을 나가시겠습니까? 방장이 방을 나갈시 방이 삭제됩니다.")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RequestThread requestThread = new RequestThread(getApplicationContext(), "POST", "delete", "");
                        requestThread.start();
                        myThread.setValue("", null, null, false);

                        DatabaseHelper userDB = new DatabaseHelper(getApplicationContext());
                        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        long mNow = System.currentTimeMillis();
                        Date mDate = new Date(mNow);
                        String endtime = mFormat.format(mDate);

                        System.out.println("room id : "+roomid);
                        System.out.println("start time : "+starttime);
                        System.out.println("end time : "+endtime);
                        try {
                            if (userDB.statsgetData(roomid).getCount() == 0) {
                                userDB.statsinsertData(roomid, starttime, endtime);
                            }
                            else{
                                userDB.statsupdateData(roomid, starttime, endtime);
                            }
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        finish();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
