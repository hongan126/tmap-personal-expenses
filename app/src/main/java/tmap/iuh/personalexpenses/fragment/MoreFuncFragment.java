package tmap.iuh.personalexpenses.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import tmap.iuh.personalexpenses.MainActivity;
import tmap.iuh.personalexpenses.R;

public class MoreFuncFragment extends Fragment implements View.OnClickListener {

    private Button mLogout;

    public MoreFuncFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_more_func, container, false);

        ((TextView)rootView.findViewById(R.id.user_email_more_text_view)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        //Button Listener
        mLogout = (Button)rootView.findViewById(R.id.logout_button);
        mLogout.setOnClickListener(this);

        return rootView;

    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i){
            case R.id.logout_button:
                ((MainActivity)getActivity()).logOut();
                break;
        }
    }
}
