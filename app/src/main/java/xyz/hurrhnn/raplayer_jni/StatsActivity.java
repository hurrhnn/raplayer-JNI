package xyz.hurrhnn.raplayer_jni;

import android.os.Bundle;

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
        StatsBinding statsbinding = StatsBinding.inflate(getLayoutInflater());
        setContentView(statsbinding.getRoot());
    }
}
