package xyz.hurrhnn.raplayer_jni;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xyz.hurrhnn.raplayer_jni.databinding.RoomBinding;

public class RoomActivity extends AppCompatActivity {
    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoomBinding roombinding = RoomBinding.inflate(getLayoutInflater());
        setContentView(roombinding.getRoot());
    }
}
