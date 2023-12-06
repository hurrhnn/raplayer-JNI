package xyz.hurrhnn.raplayer_jni;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequestUtils {

    @SuppressLint("HardwareIds")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Map<String, String> initHeadersMap(Context context, Map<String, String> cookies, String[][] headers) {
        Map<String, String> headersMap = new HashMap<>();
        if (headers[0] == null)
            Arrays.asList(headers).forEach((ArrayHeader) -> headersMap.put("Authorization", "Basic: " + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)));
        else
            Arrays.asList(headers).forEach((ArrayHeader) -> headersMap.put(ArrayHeader[0], ArrayHeader[1]));
        return headersMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Connection.Response GET(Context context, String URL, Map<String, String> cookies, String[]... headers) throws Exception {
        Map<String, String> headersMap = initHeadersMap(context, cookies, headers);
        return new HTTPRequestHandler.Connect(Jsoup.connect(URL)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Encoding", "gzip, deflate, bre")
                .header("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Connection", "keep-alive")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0")
                .headers(headersMap)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)).tryConnect();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Connection.Response POST(Context context,String URL, Map<String, String> cookies, String data, String[]... headers) throws Exception {
        Map<String, String> headersMap = initHeadersMap(context,cookies, headers);

        return new HTTPRequestHandler.Connect(Jsoup.connect(URL)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Encoding", "gzip, deflate, bre")
                .header("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Connection", "keep-alive")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0")
                .headers(headersMap)
                .cookies(cookies)
                .requestBody(data)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)).tryConnect();
    }
}
