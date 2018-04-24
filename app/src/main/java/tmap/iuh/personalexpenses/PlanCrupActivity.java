package tmap.iuh.personalexpenses;

import android.app.DatePickerDialog;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import tmap.iuh.personalexpenses.fragment.PlanToSaveMoneyMgnFragment;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.SavingPlan;
import tmap.iuh.personalexpenses.models.User;

public class PlanCrupActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PlanCrupActivity";

    //Edit text
    private EditText mPlanNameEditText;
    private EditText mTargetAmountEditText;
    private EditText mDueDateEditText;

    //Text Input layout
    private TextInputLayout mPlanNameLayout;
    private TextInputLayout mTargetAmountLayout;
    private TextInputLayout mDueDateLayout;

    //Button
    private Button mCancelButton;
    private Button mFinishButton;

    private Calendar myCalendar = Calendar.getInstance();

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]
    final String userId = getUid();

    private Double amountOfSaved = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_crup);

        //Edit text
        mPlanNameEditText = (EditText) findViewById(R.id.plan_name_edt_addplan);
        mTargetAmountEditText = (EditText) findViewById(R.id.target_amount_addplan);
        mTargetAmountEditText.addTextChangedListener(new MoneyTextWatcher(mTargetAmountEditText));


        //[START date picker for Edit Text]
        mDueDateEditText = (EditText) findViewById(R.id.due_date_plan_edit_text);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDueDate();
            }
        };
        mDueDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PlanCrupActivity.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        //[END create date picker]


        //Text Input layout
        mPlanNameLayout = (TextInputLayout) findViewById(R.id.layout_plan_name_edt_addplan);
        mTargetAmountLayout = (TextInputLayout) findViewById(R.id.layout_target_amount_addplan);
        mDueDateLayout = (TextInputLayout) findViewById(R.id.layout_due_date_plan_edit_text);

        //Button
        mCancelButton = (Button) findViewById(R.id.cancel_plan_button);
        mFinishButton = (Button) findViewById(R.id.finish_plan_button);
        mCancelButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //For offline
        mDatabase.keepSynced(true);
        // [END initialize_database_ref]

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.cancel_plan_button:
                hideKeyboard(view);
                finish();
                break;

            case R.id.finish_plan_button:
                String planName = mPlanNameEditText.getText().toString();
                String targetAmount = mTargetAmountEditText.getText().toString();
                String dueDate = mDueDateEditText.getText().toString();
                if (!validateInput(planName, targetAmount, dueDate)) {
                    return;
                }
                SavingPlan plan = new SavingPlan(getUid(), planName, getAmountOfTargetAmount(), dueDate);
                submitAddSavingPlan(plan);
                hideKeyboard(view);
                finish();
                break;
        }
    }

    private double getAmountOfTargetAmount() {
        return Double.parseDouble(mTargetAmountEditText.getText().toString().replaceAll("[$,.]", ""));
    }

    private boolean validateInput(String name, String targetAmount, String date) {
        boolean valid = true;

        if (name.isEmpty()) {
            mPlanNameLayout.setError("Không để trống!");
            valid = false;
        } else if (false) {
            //Todo check name
            valid = false;
        } else {
            mPlanNameLayout.setError(null);
        }

        if (targetAmount.isEmpty()) {
            mTargetAmountLayout.setError("Không để trống!");
            valid = false;
        } else if (getAmountOfTargetAmount() <= 0) {
            mTargetAmountLayout.setError("Số tiền phải lớn hơn 0");
            valid = false;
        } else {
            mTargetAmountLayout.setError(null);
        }

        Date nowDate = new Date();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date dueDate = new Date();
        try {
            dueDate = df.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date.isEmpty()) {
            mDueDateLayout.setError("Không để trống!");
            valid = false;
        } else if (nowDate.after(dueDate)) {
            mDueDateLayout.setError("Ngày kết thúc phải sau ngày hôm nay.");
            valid = false;
        } else {
            mDueDateLayout.setError(null);
        }

        return valid;
    }

    private void updateDueDate() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mDueDateEditText.setText(sdf.format(myCalendar.getTime()));
    }

    public void submitAddSavingPlan(final SavingPlan model) {
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
                            Toast.makeText(PlanCrupActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new Plan
                            String key = mDatabase.child("saving-plan").push().getKey();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-saving-plan/" + userId + "/" + key, model.toMap());
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
}
