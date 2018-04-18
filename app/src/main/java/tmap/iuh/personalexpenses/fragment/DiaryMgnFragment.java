package tmap.iuh.personalexpenses.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tmap.iuh.personalexpenses.DiaryCrudActivity;
import tmap.iuh.personalexpenses.R;

public class DiaryMgnFragment extends Fragment implements View.OnClickListener {

    private Spinner spinner;
    private ArrayAdapter<String> spinnerArrayAdapter;

    private ImageView mImgBalance;
    private ImageView mImgSavingMoney;
    private ImageView mImgLimit;

    private TextView mTvBalance;
    private TextView mTvSavingMoney;
    private TextView mTvLimit;

    private double balance = 300000.0;
    private double savingAmount = 200000.0;
    private double limit = 100000.0;

    private String hideBalanceStr;

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
                startActivity(new Intent(getActivity(), DiaryCrudActivity.class));
            }
        });

        //Get hide balance string: ********d
        hideBalanceStr = getResources().getString(R.string.hide_balance_str);

        // [START_FilterLog: Selection of the spinner]
        spinner = (Spinner) rootView.findViewById(R.id.diary_filter_spinner);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.diary_filter));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Todo Change
                Toast.makeText(getActivity(), spinnerArrayAdapter.getItem(i).toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // [END_FilterLog Selection of the spinner]

        //Eye icon hide/show balances
        mImgBalance = (ImageView) rootView.findViewById(R.id.img_eye_balance_diary);
        mImgSavingMoney = (ImageView) rootView.findViewById(R.id.img_eye_saving_money_diary);
        mImgLimit = (ImageView) rootView.findViewById(R.id.img_eye_limit_diary);
        mImgBalance.setOnClickListener(this);
        mImgSavingMoney.setOnClickListener(this);
        mImgLimit.setOnClickListener(this);

        //Text view balance
        mTvBalance = (TextView)rootView.findViewById(R.id.balance_diary_text_view);
        mTvSavingMoney = (TextView)rootView.findViewById(R.id.saving_money_diary_text_view);
        mTvLimit = (TextView)rootView.findViewById(R.id.limit_diary_text_view);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.img_eye_balance_diary:
                toggleBalances(mImgBalance, mTvBalance);
                break;
            case R.id.img_eye_saving_money_diary:
                toggleBalances(mImgSavingMoney, mTvSavingMoney);
                break;
            case R.id.img_eye_limit_diary:
                toggleBalances(mImgLimit, mTvLimit);
                break;
        }
    }

    public void toggleBalances(ImageView eyeImg, TextView balanceView){
        if(balanceView.getText().toString().equalsIgnoreCase(hideBalanceStr)){
            eyeImg.setImageResource(R.drawable.ic_eye_gray_24dp);
            int i = balanceView.getId();
            switch (i) {
                case R.id.balance_diary_text_view:
                    //Todo edit
                    balanceView.setText(convertMoneyToString(balance));
                    break;
                case R.id.saving_money_diary_text_view:
                    //Todo edit
                    balanceView.setText(convertMoneyToString(savingAmount));
                    break;
                case R.id.limit_diary_text_view:
                    //Todo edit
                    balanceView.setText(convertMoneyToString(limit));
                    break;
            }
        }
        else{
            eyeImg.setImageResource(R.drawable.ic_eye_off_gray_24dp);
            balanceView.setText(hideBalanceStr);
        }

    }

    private String convertMoneyToString(Double amount){
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        return formatter.format((amount))+"đ";
    }
}
