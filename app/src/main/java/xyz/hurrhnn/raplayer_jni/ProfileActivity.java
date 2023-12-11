package xyz.hurrhnn.raplayer_jni;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONObject;

import java.io.IOException;

import xyz.hurrhnn.raplayer_jni.databinding.ProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileBinding profilebinding = ProfileBinding.inflate(getLayoutInflater());
        setContentView(profilebinding.getRoot());
        profilebinding.profileImageView.setImageResource(R.drawable.profile_default);
        DatabaseHelper userDB = new DatabaseHelper(this);
        String userid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Cursor res = userDB.usergetData(userid);
        if (res.getCount() != 0) {
            while(res.moveToNext()){
                profilebinding.username.setText(res.getString(1));
                profilebinding.password.setText(res.getString(2));
                profilebinding.introduction.setText(res.getString(3));
                System.out.println(res.getString(4));
                Glide.with(getApplicationContext())
                        .load(res.getString(4))
                        .centerCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into((ImageView) findViewById(R.id.profileImageView));
            }
        }

        profilebinding.profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(View v) {
                try {
                    String username = profilebinding.username.getText().toString();
                    String password = profilebinding.password.getText().toString();
                    String introduction = profilebinding.introduction.getText().toString();
                    Bitmap profileBitmap = ((BitmapDrawable) profilebinding.profileImageView.getDrawable()).getBitmap();
                    String base64Image = ImageUtil.bitmapToBase64(profileBitmap);


                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("username", username);
                        jsonObject.put("password", password);
                        jsonObject.put("introduction", introduction);
                        jsonObject.put("img", base64Image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    RequestThread requestThread = new RequestThread(getApplicationContext(), "POST", "profile", jsonObject.toString());
                    requestThread.start();
                    if (res.getCount() == 0) {
                        userDB.userinsertData(userid, username, password,introduction, "https://ursobad.xyz/raplayer/image/"+userid);
                    } else{
                        userDB.userupdateData(userid, username, password, introduction, "https://ursobad.xyz/raplayer/image/"+userid);
                    }
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "error!", Toast.LENGTH_LONG).show();
                }
            }
        });

        profilebinding.selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);
                activityResultLauncher.launch(intent);
            }
        });
    }
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            Glide.with(getApplicationContext()).load(uri).into((ImageView) findViewById(R.id.profileImageView));
                        }
                    }
                }
            }
    );
}
