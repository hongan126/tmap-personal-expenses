package tmap.iuh.personalexpenses;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.ganfra.materialspinner.MaterialSpinner;
import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.User;

public class DiaryCrudActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "DiaryCrudActivity";

    private ArrayAdapter<String> categoryArrayAdapter;
    private MaterialSpinner categorySpinner;

    private ArrayAdapter<String> moneySourceArrayAdapter;
    private MaterialSpinner moneySourceSpinner;

    private ArrayAdapter<String> neededLevelArrayAdapter;
    private MaterialSpinner neededLevelSpinner;

    private EditText mExpenseDate;
    private EditText mAmountOfMoney;
    private TextInputLayout mAmountOfMoneyLayout;
    private EditText mDescription;
    private Calendar myCalendar = Calendar.getInstance();

    //Button
    private Button mCancelNavBtn;
    private Button mFinishNavBtn;
    private Button mAddEndBtn;
    private Button mDeleteEndBtn;
    private Button mUpdateEndBtn;

    //TextView
    private TextView mExpenseTextView;
    private TextView mIncomTextView;

    //Switch
    private Switch mTypeLogSwitch;

    private boolean isIncomeDiary;
    private boolean isAddDiaryActivity;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_crud);

        mDescription = (EditText) findViewById(R.id.description_diary_edit_text);

        //Set Array Adapter for Category Spinner
        categoryArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.expense_category));
        categoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner = (MaterialSpinner) findViewById(R.id.category_diary_spinner);
        categorySpinner.setAdapter(categoryArrayAdapter);

        //Set Array Adapter for Money Source Spinner
        moneySourceArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.source_money));
        moneySourceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moneySourceSpinner = (MaterialSpinner) findViewById(R.id.money_source_spinner);
        moneySourceSpinner.setAdapter(moneySourceArrayAdapter);

        //Set Array Adapter for Needed level Spinner
        neededLevelArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.needed_level));
        neededLevelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        neededLevelSpinner = (MaterialSpinner) findViewById(R.id.needed_level_spinner);
        neededLevelSpinner.setAdapter(neededLevelArrayAdapter);


        //[START date picker for Edit Text]
        mExpenseDate = (EditText) findViewById(R.id.date_diary_edit_text);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateExpenseDate();
            }
        };
        mExpenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(DiaryCrudActivity.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        //Set date now Todo edit
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        mExpenseDate.setText(df.format(Calendar.getInstance().getTime()));
        //[END create date picker]

        //[START View Amount Of Money]
        mAmountOfMoney = (EditText) findViewById(R.id.amount_diary_edit_text);
        mAmountOfMoney.addTextChangedListener(new MoneyTextWatcher(mAmountOfMoney));
        //[END View Amount Of Money]
        mAmountOfMoneyLayout = (TextInputLayout)findViewById(R.id.layout_amount_diary_edit_text);


        //Button add Listener
        mCancelNavBtn = (Button) findViewById(R.id.cancel_diarycrud_nav_button);
        mFinishNavBtn = (Button) findViewById(R.id.finish_diarycrud_nav_button);
        mAddEndBtn = (Button) findViewById(R.id.add_diarycrud_end_button);
        mDeleteEndBtn = (Button) findViewById(R.id.delete_diarycrud_end_button);
        mUpdateEndBtn = (Button) findViewById(R.id.update_diarycrud_end_button);
        mCancelNavBtn.setOnClickListener(this);
        mFinishNavBtn.setOnClickListener(this);
        mAddEndBtn.setOnClickListener(this);
        mDeleteEndBtn.setOnClickListener(this);
        mUpdateEndBtn.setOnClickListener(this);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //For offline
        mDatabase.keepSynced(true);
        // [END initialize_database_ref]

        //Set type of Diary Activity Todo edit or remove
        setAddDiaryActivity(true);

        //TextView
        mExpenseTextView = (TextView) findViewById(R.id.expense_type_text_view);
        mIncomTextView = (TextView) findViewById(R.id.income_type_text_view);

        //Switch
        mTypeLogSwitch = (Switch) findViewById(R.id.type_of_expense_log_switch);
        mTypeLogSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setIncomeDiary(b);
            }
        });
    }

    private void updateExpenseDate() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mExpenseDate.setText(sdf.format(myCalendar.getTime()));
    }

    private double getAmountOfMoney() {
        return Double.parseDouble(mAmountOfMoney.getText().toString().replaceAll("[$,.]", ""));
    }

    private void setAmountOfMoney(Double amount) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        mAmountOfMoney.setText(formatter.format((amount)));
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.cancel_diarycrud_nav_button:
                finish();
                break;
            case R.id.finish_diarycrud_nav_button:
                //Todo add action
                finish();
                break;
            case R.id.add_diarycrud_end_button:
                //Todo add action
                Diary diary = getDiaryFromLayout();
                if (diary == null) {
                    break;
                }
                submitDiary(diary);
                finish();
                break;
            case R.id.delete_diarycrud_end_button:
                //Todo add action
                finish();
                break;
            case R.id.update_diarycrud_end_button:
                //Todo add action
                finish();
                break;
        }
    }

    public boolean isAddDiaryActivity() {
        return isAddDiaryActivity;
    }

    public void setAddDiaryActivity(boolean addDiaryActivity) {
        isAddDiaryActivity = addDiaryActivity;
        if (isAddDiaryActivity) {
            mFinishNavBtn.setText("Thêm");
            mAddEndBtn.setVisibility(View.VISIBLE);
            mDeleteEndBtn.setVisibility(View.GONE);
            mUpdateEndBtn.setVisibility(View.GONE);
        } else {
            mFinishNavBtn.setText("Cập nhật");
            mAddEndBtn.setVisibility(View.GONE);
            mDeleteEndBtn.setVisibility(View.VISIBLE);
            mUpdateEndBtn.setVisibility(View.VISIBLE);
        }
    }

    public boolean isIncomeDiary() {
        return isIncomeDiary;
    }

    public void setIncomeDiary(boolean incomeDiary) {
        isIncomeDiary = incomeDiary;
        if (isIncomeDiary()) {
            mExpenseTextView.setTextColor(getResources().getColor(R.color.lightGray));
            mExpenseTextView.setTypeface(Typeface.DEFAULT);
            mIncomTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            mIncomTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mAmountOfMoney.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            mExpenseTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            mExpenseTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mIncomTextView.setTextColor(getResources().getColor(R.color.lightGray));
            mIncomTextView.setTypeface(Typeface.DEFAULT);
            mAmountOfMoney.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public void submitDiary(final Diary diary) {

        // [START single_value_read]
        final String userId = getUid();
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
                            Toast.makeText(DiaryCrudActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            String key = mDatabase.child("diary").push().getKey();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-diary/" + userId + "/" + key, diary.toMap());
                            mDatabase.updateChildren(childUpdates);
                        }

                        // Finish this Activity, back to the stream
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }

    private boolean validateInput(String amount, int category, int moneySource, int needLevel) {
        boolean valid = true;

        if (amount.isEmpty()) {
            mAmountOfMoneyLayout.setError("Không để trống.");
            valid = false;
        } else if (getAmountOfMoney() <= 0) {
            mAmountOfMoneyLayout.setError("Số tiền phải lớn hơn 0.");
            valid = false;
        } else {
            mAmountOfMoneyLayout.setError(null);
        }

        if (category <= 0) {
            categorySpinner.setError("Vui lòng chọn một danh mục.");
            valid = false;
        } else {
            categorySpinner.setError(null);
        }

        if (moneySource <= 0) {
            moneySourceSpinner.setError("Vui lòng chọn nguồn tiền.");
            valid = false;
        } else {
            moneySourceSpinner.setError(null);
        }

        if (needLevel <= 0) {
            neededLevelSpinner.setError("Vui lòng chọn mức độ cần thiết.");
            valid = false;
        } else {
            neededLevelSpinner.setError(null);
        }

        return valid;
    }

    private Diary getDiaryFromLayout() {
        String amount = mAmountOfMoney.getText().toString();
        int categoryPos = categorySpinner.getSelectedItemPosition();
        int moneySourcePos = moneySourceSpinner.getSelectedItemPosition();
        int needLevelPos = neededLevelSpinner.getSelectedItemPosition();
        String date = mExpenseDate.getText().toString();
        if (!validateInput(amount, categoryPos, moneySourcePos, needLevelPos)) {
            return null;
        }

        String category = categoryArrayAdapter.getItem(categoryPos-1);
        String moneySource = moneySourceArrayAdapter.getItem(moneySourcePos-1);
        String needLevel = neededLevelArrayAdapter.getItem(needLevelPos-1);
        String description = mDescription.getText().toString();
        String typeLog = getResources().getString(R.string.expense_type);
        if (isIncomeDiary()) {
            typeLog = getResources().getString(R.string.income_type);
        }
        return new Diary(getUid(), moneySource, getAmountOfMoney(), category, description, needLevel, typeLog, date);
    }
}
