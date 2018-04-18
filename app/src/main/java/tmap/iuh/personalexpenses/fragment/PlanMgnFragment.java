package tmap.iuh.personalexpenses.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tmap.iuh.personalexpenses.PlanCrupActivity;
import tmap.iuh.personalexpenses.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlanMgnFragment extends Fragment {


    public PlanMgnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_plan_mgn, container, false);

        rootView.findViewById(R.id.fab_new_plan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(getActivity(), PlanCrupActivity.class));
            }
        });
        return rootView;
    }

}
