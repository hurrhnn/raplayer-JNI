package xyz.hurrhnn.raplayer_jni;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

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
            public void onClick(View v) {
                // TODO
                // server에 ["title", "ip", "port", "opt"] 보내기
                Intent intent = new Intent(CreateRoomActivity.this, RoomActivity.class);
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

}
