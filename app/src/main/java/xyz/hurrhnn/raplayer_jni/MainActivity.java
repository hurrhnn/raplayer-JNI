package xyz.hurrhnn.raplayer_jni;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    private void bufferCallback(byte[] frame, AudioTrack audioTrack) {
        audioTrack.write(frame, 0, frame.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.main_text);
        Button startButton = findViewById(R.id.main_start_button);

        textView.setText(stringFromJNI());
        startButton.setOnClickListener(v -> {
            AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC,
                48000, //sample rate
                AudioFormat.CHANNEL_OUT_STEREO, // 2 channel
                AudioFormat.ENCODING_PCM_16BIT, // 16-bit
                960 * 2 * 2,
                AudioTrack.MODE_STREAM);
            audio.play();
            class ClientThread extends Thread {
                public void run() {
                    startClientFromJNI(audio);
                }
            }
            ClientThread clientThread = new ClientThread();
            clientThread.start();
        });
    }

    public native String stringFromJNI();

    public native boolean startClientFromJNI(AudioTrack audio);
}
