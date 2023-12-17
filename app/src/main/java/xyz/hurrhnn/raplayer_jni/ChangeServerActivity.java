package xyz.hurrhnn.raplayer_jni;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;

import java.io.FileOutputStream;
import java.util.HashMap;

import xyz.hurrhnn.raplayer_jni.databinding.ChangeserverBinding;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ChangeServerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeserverBinding changeserverBinding = ChangeserverBinding.inflate(getLayoutInflater());
        setContentView(changeserverBinding.getRoot());

        changeserverBinding.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String server = changeserverBinding.newServer.getText().toString();
                if(!(server.startsWith("http://") || server.startsWith("https://"))){
                    server = "https://" + server;
                    if(!server.endsWith("/"))
                        server += "/";
                }
                String finalServer = server;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HTTPRequestUtils httpRequestUtils = new HTTPRequestUtils();
                        try {
                            Connection.Response res = httpRequestUtils.GET(getApplicationContext(), finalServer+"heartbeat", new HashMap<>(), (String[]) null);
                            if(res.statusCode() != 200){
                                postToastMessage("서버가 응답하지 않습니다.");
                            } else {
                                postToastMessage("서버 설정 성공");
                                System.out.println("server: " + finalServer);
                                FileOutputStream fos = null;
                                fos = openFileOutput("server.txt", MODE_PRIVATE);
                                fos.write(finalServer.getBytes());
                                fos.close();
                                finish();
                            }
                        } catch (Exception e) {
                            postToastMessage("서버가 응답하지 않습니다.");
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
