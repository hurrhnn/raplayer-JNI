package xyz.hurrhnn.raplayer_jni;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Formatter;

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
        TextView logView = findViewById(R.id.logging_text);
        logView.setTextColor(Color.WHITE);

        EditText addressEditText = findViewById(R.id.address_edittext);
        EditText portEditText = findViewById(R.id.port_edittext);
        Button startButton = findViewById(R.id.main_start_button);

        textView.setText(stringFromJNI());
        startButton.setOnClickListener(new View.OnClickListener() {
            int count = 0;

            @Override
            public void onClick(View v) {
                AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, //sample rate
                    AudioFormat.CHANNEL_OUT_STEREO, // 2 channel
                    AudioFormat.ENCODING_PCM_16BIT, // 16-bit
                    960 * 2 * 2, AudioTrack.MODE_STREAM);

                audio.play();

                String address = addressEditText.getText().toString();
                String port = portEditText.getText().toString();

                String text = logView.getText().toString() + " [" + new Formatter().format("%02d", ++count).toString() + "] Connecting to " + address + ":" + port + "...\n";
                logView.setText(text);

                class ClientThread extends Thread {
                    private final String address;
                    private final int port;

                    public ClientThread(String address, int port) {
                        this.address = address;
                        this.port = port;
                    }

                    public void run() {
                        startClientFromJNI(address, port, audio);
                    }
                }
                ClientThread clientThread = new ClientThread(address, Integer.parseInt(port));
                clientThread.start();
            }
        });
    }

    public native String stringFromJNI();

    public native boolean startClientFromJNI(String address, int port, AudioTrack audio);
}
