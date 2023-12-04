package xyz.hurrhnn.raplayer_jni;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                // server에 정보 보내고 join
                Intent intent = new Intent(getActivity(), RoomActivity.class);
                startActivity(intent);
            }
        });

        TextView title = view.findViewById(R.id.join_title);
        String message = this.getArguments().getString("title");
        title.setText(String.format(title.getText().toString(), message));

        return view;
    }
}
