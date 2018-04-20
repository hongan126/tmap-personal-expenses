package tmap.iuh.personalexpenses.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tmap.iuh.personalexpenses.DetailsReportActivity;
import tmap.iuh.personalexpenses.R;

public class ReportFragment extends Fragment {

    public ReportFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        //Todo remove
        rootView.findViewById(R.id.expected_cost_for_next_month_edit_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DetailsReportActivity.class));
            }
        });

        return rootView;
    }
}
