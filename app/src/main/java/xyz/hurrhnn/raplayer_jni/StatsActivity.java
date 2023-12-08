package xyz.hurrhnn.raplayer_jni;

import android.os.Bundle;
import android.provider.Settings;
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
        TextView title = statsbinding.statsTitle;
        title.setText(String.format(title.getText().toString(), userid));
    }
}
