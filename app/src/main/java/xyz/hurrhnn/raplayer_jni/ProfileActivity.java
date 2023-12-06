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

        Cursor res = userDB.getData(userid);
        if (res.getCount() != 0) {
            while(res.moveToNext()){
                profilebinding.username.setText(res.getString(1));
                profilebinding.password.setText(res.getString(2));
                profilebinding.introduction.setText(res.getString(3));
                class DownloadImageTask extends Thread {
                    ImageView bmImage;
                    String url;
                    public DownloadImageTask(ImageView bmImage, String url) {
                        this.bmImage = bmImage;
                        this.url = url;
                    }
                    protected Bitmap doInBackground(Void... urls) {
                        String urldisplay = url;
                        Bitmap user_image = null;
                        try {
                            java.net.URL url = new java.net.URL(urldisplay);
                            user_image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return user_image;
                    }
                    protected void onPostExecute(Bitmap result) {
                        bmImage.setImageBitmap(result);
                    }
                }
                DownloadImageTask downloadImageTask = new DownloadImageTask(profilebinding.profileImageView, res.getString(4));
                downloadImageTask.start();
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
                        userDB.insertData(userid, username, password,introduction, "https://ursobad.xyz/raplayer/image/"+userid);
                    } else{
                        userDB.updateData(userid, username, password, introduction, "https://ursobad.xyz/raplayer/image/"+userid);
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
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                ImageView imageView = findViewById(R.id.profileImageView);
                                imageView.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
    );
}
