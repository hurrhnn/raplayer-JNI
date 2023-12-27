package xyz.hurrhnn.raplayer_jni;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xyz.hurrhnn.raplayer_jni.databinding.StatsBinding;

public class StatsActivity extends AppCompatActivity {
static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userid = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        StatsBinding statsbinding = StatsBinding.inflate(getLayoutInflater());
        setContentView(statsbinding.getRoot());

        LinearLayout statsRoot = statsbinding.statsRoot;
        TextView title = statsbinding.statsTitle;
        title.setText(String.format(title.getText().toString(), userid));


        DatabaseHelper statsDB = new DatabaseHelper(this);
        Cursor res = statsDB.statsgetallData();
        if (res.getCount() != 0) {
            while(res.moveToNext()){

                long time = Long.parseLong(res.getString(3));
                layaoutcreate(statsRoot, res.getString(0), res.getString(1), res.getString(2), time);
//                System.out.println(String.format(
//                        "Room ID: %s\nLast Join Time: %s\nLast Out Time: %s\nTotal Time: %s",
//                        res.getString(0),
//                        res.getString(1),
//                        res.getString(2),
//                        res.getString(3)
//                        ));
            }
        }
    }

    public void layaoutcreate(LinearLayout statsRoot, String roomid, String starttime, String endtime, long time) {
        LinearLayout instats = new LinearLayout(this);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        params1.setMargins(0, 0, 0, 5);
        instats.setLayoutParams(params1);
        instats.setBackgroundColor(Color.parseColor("#979AB1"));
        instats.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.setMargins(10, 0, 10, 0);

        TextView inroomid = new TextView(this);
        inroomid.setLayoutParams(params2);
        inroomid.setText(String.format("Room ID: %s", roomid));
        inroomid.setTextColor(Color.parseColor("#000000"));
        inroomid.setTextSize(20);

        TextView inroomstart = new TextView(this);
        inroomstart.setLayoutParams(params2);
        inroomstart.setText(String.format("Last Join Time: %s", starttime));
        inroomstart.setTextColor(Color.parseColor("#000000"));
        inroomstart.setTextSize(20);

        TextView inroomout = new TextView(this);
        inroomout.setLayoutParams(params2);
        inroomout.setText(String.format("Last Out Time: %s", endtime));
        inroomout.setTextColor(Color.parseColor("#000000"));
        inroomout.setTextSize(20);

        TextView inroomtime = new TextView(this);
        inroomtime.setLayoutParams(params2);
        inroomtime.setText(String.format("Total Time: %02d:%02d:%02d", time/3600, time/60, time%60));
        inroomtime.setTextColor(Color.parseColor("#000000"));
        inroomtime.setTextSize(20);

        instats.addView(inroomid);
        instats.addView(inroomstart);
        instats.addView(inroomout);
        instats.addView(inroomtime);
        statsRoot.addView(instats);
    }
}
