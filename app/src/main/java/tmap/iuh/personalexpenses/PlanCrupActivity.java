package tmap.iuh.personalexpenses;

import android.app.DatePickerDialog;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PlanCrupActivity extends BaseActivity implements View.OnClickListener {

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
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.cancel_plan_button:
                finish();
                break;

            case R.id.finish_plan_button:
                if(!validateInput(mPlanNameEditText.getText().toString(), mTargetAmountEditText.getText().toString(), mDueDateEditText.getText().toString())){
                    return;
                }
                //Todo add action
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

        if (date.isEmpty()) {
            mDueDateLayout.setError("Không để trống!");
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
}
