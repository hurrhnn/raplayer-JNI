package xyz.hurrhnn.raplayer_jni;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.helper.HttpConnection;

import java.util.HashMap;
import java.util.Objects;
@RequiresApi(api = Build.VERSION_CODES.N)
public class RequestThread extends Thread{
    JSONObject result;
    Context context;
    String URL, data, method;
    RequestThread(Context context, String method, String URL, String data) {
        this.context = context;
        this.URL = URL;
        this.data = data;
        this.method = method;
    }
    public void run() {
        HTTPRequestUtils httpRequestUtils = new HTTPRequestUtils();
        if(Objects.equals(method, "GET")){
            try {
                Connection.Response res = httpRequestUtils.GET(context, "https://ursobad.xyz/raplayer/"+URL, new HashMap<>(), (String[]) null);
                if(res.statusCode() == 200){
                    result = new JSONObject(res.body());
                } else {
                    JSONObject msg = new JSONObject(res.body());
                    postToastMessage(msg.get("msg").toString());
//                    throw new HttpStatusException("error!", res.statusCode(), "https://ursobad.xyz/raplayer/"+URL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Objects.equals(method, "POST")) {
            try {
                Connection.Response res = httpRequestUtils.POST(context, "https://ursobad.xyz/raplayer/"+URL, new HashMap<>(), data,(String[]) null);
                if(res.statusCode() == 200){
                    result = new JSONObject(res.body());
                } else {
                    JSONObject msg = new JSONObject(res.body());
                    postToastMessage(msg.get("msg").toString());
//                    throw new HttpStatusException("error!", res.statusCode(), "https://ursobad.xyz/raplayer/"+URL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
    public JSONObject getResult() {
        return this.result;
    }
}
