package tmap.iuh.personalexpenses.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import tmap.iuh.personalexpenses.DiaryActivity;
import tmap.iuh.personalexpenses.R;

public class DiaryMgnFragment extends Fragment {

    private Spinner spinner;
    private String filters[] = {"Hôm nay", "Tháng này", "Tất cả"};
    private ArrayAdapter<String> spinnerArrayAdapter;

    public DiaryMgnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_diary_mgn, container, false);

        rootView.findViewById(R.id.fab_new_diary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DiaryActivity.class));
            }
        });

        // Selection of the spinner
        spinner = (Spinner) rootView.findViewById(R.id.diary_filter_spinner);

        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.diary_filter));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);

        return rootView;
    }

}
