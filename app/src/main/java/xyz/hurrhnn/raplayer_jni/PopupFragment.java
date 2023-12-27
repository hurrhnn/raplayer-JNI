package xyz.hurrhnn.raplayer_jni;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

public class PopupFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_fragment, container, false);
        LinearLayout backgroundLayout = view.findViewById(R.id.fragment_background);

        backgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        LinearLayout rootLayout = view.findViewById(R.id.root);
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing, just consume the click event.
            }
        });

        Button joinButton = view.findViewById(R.id.join_room);
        TextView title = view.findViewById(R.id.join_title);
        String message = this.getArguments().getString("title");
        String server_userid = this.getArguments().getString("userid");

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.N)
            @RequiresPermission(value = "android.permission.RECORD_AUDIO")
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                EditText passwordEd = view.findViewById(R.id.room_password);
                String password = passwordEd.getText().toString();
                System.out.println(password);

                try {
                    jsonObject.put("client_ip", "0.0.0.0");
                    jsonObject.put("client_port", 9999);
                    jsonObject.put("server_userid", server_userid);
                    jsonObject.put("password", password);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestThread joinroom = new RequestThread(getActivity(), "POST", "join", jsonObject.toString());
                joinroom.start();
                try {
                    joinroom.join();
                    if(joinroom.getResult() == null){
                        return;
                    }
                    String server_userid = (joinroom.getResult().getJSONObject("data")).getString("userid");

                    Raplayer raplayer = new Raplayer();
                    long spawn_id = raplayer.spawn(true, joinroom.getResult().getJSONObject("data").getString("server_ip"), (short) joinroom.getResult().getJSONObject("data").getInt("server_port"));
                    long provider = raplayer.registerMediaProvider(spawn_id);
                    long consumer = raplayer.registerMediaConsumer(spawn_id);
                    System.out.println(joinroom.getResult().getJSONObject("data").getString("server_ip") + ", " + joinroom.getResult().getJSONObject("data").getInt("server_port"));
                    Log.e("Join Room", ("id: " + spawn_id + ", provider: " + provider + ", consumer: " + consumer));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Intent intent = new Intent(getActivity(), RoomActivity.class);
                intent.putExtra("title", message);
                intent.putExtra("server_userid", server_userid);
                intent.putExtra("event", "join");
                startActivity(intent);
            }
        });

        title.setText(String.format(title.getText().toString(), message));

        return view;
    }
}
