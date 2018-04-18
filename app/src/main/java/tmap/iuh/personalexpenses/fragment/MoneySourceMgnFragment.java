package tmap.iuh.personalexpenses.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tmap.iuh.personalexpenses.AddMoneySourceActivity;
import tmap.iuh.personalexpenses.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoneySourceMgnFragment extends Fragment implements View.OnClickListener {

    private ImageView mImgBalance;
    private ImageView mImgLimit;

    private TextView mTvBalance;
    private TextView mTvLimit;

    private String hideBalanceStr;

    private double balance = 300000.0;
    private double limit = 100000.0;

    public MoneySourceMgnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_money_source_mgn, container, false);

        //Get hide balance string: ********d
        hideBalanceStr = getResources().getString(R.string.hide_balance_str);

        //Add Float Button Listener
        rootView.findViewById(R.id.fab_new_money_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddMoneySourceActivity.class));
            }
        });

        //Todo delete
        rootView.findViewById(R.id.limit_moneysrc_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogRemoveMoneySource();
            }
        });

        //Eye icon hide/show balances
        mImgBalance = (ImageView) rootView.findViewById(R.id.img_eye_balance_moneysrc);
        mImgLimit = (ImageView) rootView.findViewById(R.id.img_eye_limit_moneysrc);
        mImgBalance.setOnClickListener(this);
        mImgLimit.setOnClickListener(this);

        //Text view balance
        mTvBalance = (TextView)rootView.findViewById(R.id.balance_moneysrc_text_view);
        mTvLimit = (TextView)rootView.findViewById(R.id.limit_moneysrc_text_view);

        return rootView;
    }

    public void showDialogRemoveMoneySource(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.remove_ms_message));
        builder.setTitle(getResources().getString(R.string.remove_ms_title));
        // builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setNegativeButton(getResources().getString(R.string.remove_ms_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setPositiveButton(getResources().getString(R.string.remove_ms_remove),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Todo Delete Money source
                        Toast.makeText(getActivity(), "Xóa hoàn tất!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.img_eye_balance_moneysrc:
                toggleBalances(mImgBalance, mTvBalance);
                break;
            case R.id.img_eye_limit_moneysrc:
                toggleBalances(mImgLimit, mTvLimit);
                break;
        }
    }

    public void toggleBalances(ImageView eyeImg, TextView balanceView){
        if(balanceView.getText().toString().equalsIgnoreCase(hideBalanceStr)){
            eyeImg.setImageResource(R.drawable.ic_eye_gray_24dp);
            int i = balanceView.getId();
            switch (i) {
                case R.id.balance_moneysrc_text_view:
                    //Todo edit
                    balanceView.setText(convertMoneyToString(balance));
                    break;
                case R.id.limit_moneysrc_text_view:
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
