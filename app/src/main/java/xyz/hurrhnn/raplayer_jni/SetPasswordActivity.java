package xyz.hurrhnn.raplayer_jni;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import xyz.hurrhnn.raplayer_jni.databinding.SetpasswordBinding;

public class SetPasswordActivity extends AppCompatActivity {
    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetpasswordBinding binding = SetpasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.setpwsw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    binding.loginPw.setVisibility(View.VISIBLE);
                } else {
                    binding.loginPw.setVisibility(View.INVISIBLE);
                    binding.loginPw.setText("");
                }
            }
        });
        binding.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                    jsonObject.put("check_password", binding.loginPw.getText().toString());
                    jsonObject.put("password", binding.newPw.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                RequestThread setpassword = new RequestThread(getApplicationContext(), "POST", "register", jsonObject.toString());
                setpassword.start();
                try {
                    setpassword.join();
                    if (setpassword.getResult() != null){
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}