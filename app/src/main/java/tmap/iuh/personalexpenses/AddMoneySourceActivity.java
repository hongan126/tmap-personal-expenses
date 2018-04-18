package tmap.iuh.personalexpenses;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class AddMoneySourceActivity extends BaseActivity implements View.OnClickListener {

    private EditText mNameOfMoneySource;
    private EditText mFirstBalance;
    private TextInputLayout mNameOfMoneySourceLayout;
    private TextInputLayout mFirstBalanceLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money_source);

        //View
        mNameOfMoneySource = (EditText)findViewById(R.id.moneysrc_name_edt_addms);
        mNameOfMoneySourceLayout = (TextInputLayout)findViewById(R.id.layout_moneysrc_name_edt_addms);
        mFirstBalanceLayout = (TextInputLayout) findViewById(R.id.layout_first_balance_edt_addms);
        //[START View Amount Of Money]
        mFirstBalance = (EditText) findViewById(R.id.first_balance_edt_addms);
        mFirstBalance.addTextChangedListener(new MoneyTextWatcher(mFirstBalance));
        //[END View Amount Of Money]

        //Button set Listener
        findViewById(R.id.cancel_add_moneysrc_button).setOnClickListener(this);
        findViewById(R.id.finish_add_moneysrc_button).setOnClickListener(this);
    }

    private double getAmountOfMoney(){
        return Double.parseDouble(mFirstBalance.getText().toString().replaceAll("[$,.]", ""));
    }

    private boolean validateInput(String name, String balance){
        boolean valid = true;

        if (name.isEmpty()) {
            mNameOfMoneySourceLayout.setError("Không để trống!");
            valid = false;
        } else if (false) {
            //Todo check name
            valid = false;
        } else {
            mNameOfMoneySourceLayout.setError(null);
        }

        if (balance.isEmpty()) {
            mFirstBalanceLayout.setError("Không để trống!");
            valid = false;
        } else if (getAmountOfMoney()<0) {
            mFirstBalanceLayout.setError("Số tiền không thể âm!");
            valid = false;
        } else {
            mFirstBalanceLayout.setError(null);
        }

        return valid;
    }

    private void addNewMoneySource(){
        String moneySouceName = mNameOfMoneySource.getText().toString();
        String firstBalance = mFirstBalance.getText().toString();
        if(!validateInput(moneySouceName, firstBalance)){
            return;
        }
        Toast.makeText(this, "Do something!", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i){
            case R.id.cancel_add_moneysrc_button:
                finish();
                break;
            case R.id.finish_add_moneysrc_button:
                addNewMoneySource();
                break;
        }
    }
}
