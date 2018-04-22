package tmap.iuh.personalexpenses;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.User;

public class AddMoneySourceActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "AddMoneySourceActivity";

    private EditText mNameOfMoneySource;
    private EditText mFirstBalance;
    private TextInputLayout mNameOfMoneySourceLayout;
    private TextInputLayout mFirstBalanceLayout;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]
    final String userId = getUid();

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

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //For offline
        mDatabase.keepSynced(true);
        // [END initialize_database_ref]
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

    public void submitAddMoneySource(final MoneySource model){
        // [START single_value_read]
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(AddMoneySourceActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new Diary
                            String key = mDatabase.child("money-source").push().getKey();
                            model.msid = key;
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-money-source/" + userId + "/" + key, model.toMap());
                            mDatabase.updateChildren(childUpdates);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }

    private MoneySource getModelFromLayout(){
        String moneySourceName = mNameOfMoneySource.getText().toString();
        String firstBalance = mFirstBalance.getText().toString();
        if(!validateInput(moneySourceName, firstBalance)){
            return null;
        }
        return new MoneySource(userId, moneySourceName, getAmountOfMoney());
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i){
            case R.id.cancel_add_moneysrc_button:
                finish();
                break;
            case R.id.finish_add_moneysrc_button:
                MoneySource moneySource = getModelFromLayout();
                if(moneySource==null){
                    return;
                }
                submitAddMoneySource(moneySource);
                finish();
                break;
        }
    }


}
