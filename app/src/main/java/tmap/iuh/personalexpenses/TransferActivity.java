package tmap.iuh.personalexpenses;

import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import fr.ganfra.materialspinner.MaterialSpinner;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.User;

public class TransferActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "TransferActivity";
    //Key Bundle and Extra
    public static final String EXTRA_MS_MODEL = "money_source_model";
    public static final String EXTRA_MS_KEY = "money_source_key";
    public static final String BUNDEL_MS_DATA = "data";

    private TextView mMoneySourceName;
    private EditText mAmountOfMoney;
    private TextInputLayout mLayoutAmount;

    private ArrayAdapter<String> moneySourceArrayAdapter;
    private List<String> moneySourceKeyList;
    private List<MoneySource> moneySourceList;
    private MaterialSpinner moneySourceSpinner;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]
    final String userId = getUid();

    private MoneySource smsModel;
    private String smsKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //For offline
        mDatabase.keepSynced(true);
        // [END initialize_database_ref]

        //Text view, Edit text
        mMoneySourceName = (TextView) findViewById(R.id.money_source_name_textview);
        mAmountOfMoney = (EditText) findViewById(R.id.amount_transfer_edit_text);
        mAmountOfMoney.addTextChangedListener(new MoneyTextWatcher(mAmountOfMoney));
        mLayoutAmount = (TextInputLayout) findViewById(R.id.layout_amount_transfer_edit_text);

        //Button set Listener
        findViewById(R.id.cancel_transfer_button).setOnClickListener(this);
        findViewById(R.id.finish_transfer_button).setOnClickListener(this);

        //Get data sourceMs
        Bundle bundle = getIntent().getBundleExtra(BUNDEL_MS_DATA);
        if (bundle != null) {
            smsModel = (MoneySource) bundle.getSerializable(EXTRA_MS_MODEL);
            smsKey = bundle.getString(EXTRA_MS_KEY);
            mMoneySourceName.setText(smsModel.moneySourceName);
        }

        //Set Array Adapter for Money Source Spinner
        moneySourceSpinner = (MaterialSpinner) findViewById(R.id.target_money_source_spinner);
        mDatabase.child("user-money-source").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> msNameList = new ArrayList<String>();
                moneySourceKeyList = new ArrayList<String>();
                moneySourceList = new ArrayList<MoneySource>();

                for (DataSnapshot moneySourceSnapshot : dataSnapshot.getChildren()) {
                    String msName = moneySourceSnapshot.child("moneySourceName").getValue(String.class);
                    if (!msName.equalsIgnoreCase(smsModel.moneySourceName)) {
                        msNameList.add(msName);
                        moneySourceKeyList.add(moneySourceSnapshot.getKey());
                        moneySourceList.add(moneySourceSnapshot.getValue(MoneySource.class));
                    }
                }
                if (moneySourceArrayAdapter != null) {
                    moneySourceArrayAdapter = null;
                }
                moneySourceArrayAdapter = new ArrayAdapter<String>(TransferActivity.this, android.R.layout.simple_spinner_item, msNameList);
                moneySourceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                moneySourceSpinner.setAdapter(moneySourceArrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private double getAmountOfMoney() {
        return Double.parseDouble(mAmountOfMoney.getText().toString().replaceAll("[$,.]", ""));
    }

    private boolean validateInput(String amount, int moneySourcePos) {
        boolean valid = true;

        if (amount.isEmpty()) {
            mLayoutAmount.setError("Không để trống.");
            valid = false;
        } else if (getAmountOfMoney() <= 0) {
            mLayoutAmount.setError("Số tiền phải lớn hơn 0.");
            valid = false;
        } else {
            mLayoutAmount.setError(null);
        }

        if (moneySourcePos <= 0) {
            moneySourceSpinner.setError("Vui lòng chọn nguồn tiền.");
            valid = false;
        } else {
            moneySourceSpinner.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.cancel_transfer_button:
                hideKeyboard(view);
                finish();
                break;
            case R.id.finish_transfer_button:
                String amount = mAmountOfMoney.getText().toString();
                int moneySourcePos = moneySourceSpinner.getSelectedItemPosition();
                if (!validateInput(amount, moneySourcePos)) {
                    return;
                }
                submitUpdateMoneySource(getAmountOfMoney(), smsModel, smsKey, moneySourceList.get(moneySourcePos - 1), moneySourceKeyList.get(moneySourcePos - 1));
                hideKeyboard(view);
                finish();
                break;
        }
    }

    private void submitUpdateMoneySource(final double amount, final MoneySource msSourceModel, final String msSourceKey, final MoneySource msTargetModel, final String msTargetKey) {
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
                            Toasty.error(TransferActivity.this, "Error: could not fetch user.", Toast.LENGTH_SHORT, true).show();
                        } else {
                            msSourceModel.currentBalance -= amount;
                            msTargetModel.currentBalance += amount;

                            // Update money source
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-money-source/" + userId + "/" + msSourceKey, msSourceModel.toMap());
                            childUpdates.put("/user-money-source/" + userId + "/" + msTargetKey, msTargetModel.toMap());
                            mDatabase.updateChildren(childUpdates);

                            //Update user info
                            if (msSourceModel.moneySourceName.equalsIgnoreCase(getString(R.string.saving_money_source))) {
                                user.setSavingBalance(user.savingBalance - amount);
                            }
                            if (msTargetModel.moneySourceName.equalsIgnoreCase(getString(R.string.saving_money_source))) {
                                user.setSavingBalance(user.savingBalance + amount);
                            }
                            mDatabase.child("users").child(userId).setValue(user);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
        Toasty.success(TransferActivity.this, "Chuyển khoản hoàn tất!", Toast.LENGTH_LONG, true).show();
    }
}
