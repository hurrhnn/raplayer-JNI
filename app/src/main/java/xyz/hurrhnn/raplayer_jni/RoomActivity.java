package xyz.hurrhnn.raplayer_jni;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        String message = this.getIntent().getStringExtra("title");
        String event = this.getIntent().getStringExtra("event");
        String server_userid = this.getIntent().getStringExtra("server_userid");

        TextView title = roombinding.roomTitle;
        title.setText(String.format(title.getText().toString(), message));
        LinearLayout inroomRoot = roombinding.inroomRoot;

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


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


                    JSONObject jsonObject = getinroom_user.getResult().getJSONObject("data");
                    String username = jsonObject.getString("username");
                    String idk1 = jsonObject.getString("introduction");
                    String img = jsonObject.getString("img");
                    createlaylout(username, idk1, img, inroomRoot);
                }

                myThread.setValue(server_userid, inroom_user, inroomRoot, true);
                myThread.start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            DatabaseHelper userDB = new DatabaseHelper(this);
            String userid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Cursor res = userDB.getData(userid);
            if (res.getCount() != 0) {
                while (res.moveToNext()) {
                    createlaylout(
                            res.getString(1),
                            res.getString(2),
                            res.getString(3),
                            inroomRoot
                    );
                }
            } else {
                RequestThread getprofile = new RequestThread(getApplicationContext(), "GET", "profile", "");
                getprofile.start();
                try {
                    getprofile.join();
                    JSONObject jsonObject = getprofile.getResult().getJSONObject("data");
                    createlaylout(
                            jsonObject.getString("username"),
                            jsonObject.getString("introduction"),
                            jsonObject.getString("img"),
                            inroomRoot
                    );
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            JSONArray inroom_root = new JSONArray();
            inroom_root.put(userid);
            myThread.setValue(userid, inroom_root, inroomRoot, true);
            myThread.start();
        }
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

                                    if(getinroom_user.getResult() != null){
                                        if(i==0) inroomRoot.removeAllViews();
                                        JSONObject jsonObject1 = getinroom_user.getResult().getJSONObject("data");
                                        String username = jsonObject1.getString("username");
                                        String idk1 = jsonObject1.getString("introduction");
                                        String img = jsonObject1.getString("img");
                                        createlaylout(username, idk1, img, inroomRoot);
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

    public void createlaylout(String username, String idk1, String img, LinearLayout inroomRoot){
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0,1);
        params1.bottomMargin=5;
        LinearLayout linearroom = new LinearLayout(this);
        linearroom.setLayoutParams(params1);
        linearroom.setBackgroundColor(Color.parseColor("#979AB1"));


        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.setMargins(5,10,0,0);
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
        usernameTx.setTextColor(Color.parseColor("#FFFFFF"));
        params3.setMargins(20,0,0,0);
        usernameTx.setLayoutParams(params3);

        TextView idk = new TextView(this);
        idk.setText(idk1);
        idk.setTextSize(20);
        idk.setTextColor(Color.parseColor("#FFFFFF"));
        params3.setMargins(20,0,0,0);
        idk.setLayoutParams(params3);

        linearprofile.addView(usernameTx);
        linearprofile.addView(idk);
        linearroom.addView(imageView);
        linearroom.addView(linearprofile);
        inroomRoot.addView(linearroom);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("방을 나가시겠습니까? 방장이 방을 나갈시 방이 삭제됩니다.")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RequestThread requestThread = new RequestThread(getApplicationContext(), "POST", "delete", "");
                        requestThread.start();
                        myThread.setValue("", null, null, false);
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
