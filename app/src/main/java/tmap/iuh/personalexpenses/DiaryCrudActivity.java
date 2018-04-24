package tmap.iuh.personalexpenses;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.ganfra.materialspinner.MaterialSpinner;
import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.SavingPlan;
import tmap.iuh.personalexpenses.models.User;

public class DiaryCrudActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "DiaryCrudActivity";

    private ArrayAdapter<String> categoryArrayAdapter;
    private MaterialSpinner categorySpinner;

    private ArrayAdapter<String> moneySourceArrayAdapter;
    private List<String> moneySourceKey;
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
    final String userId = getUid();

    //Key Bundle and Extra
    public static final String DIARY_MODEL = "diary_model";
    public static final String DIARY_KEY = "diary_key";
    public static final String BUNDEL_DATA_FROM_DIARY_MGN = "data";
    public static final String BUNDEL_DATA_FORM_PLAN_MGN = "data_from_plan_mgn";
    public static final String PLAN_FROM_MGN = "plan";

    //Data for DETAILS DIARY
    private String mDiaryKey;
    private Diary mDiaryModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_crud);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //For offline
        mDatabase.keepSynced(true);
        // [END initialize_database_ref]

        //Description EditText
        mDescription = (EditText) findViewById(R.id.description_diary_edit_text);

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
        //Set date now
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        mExpenseDate.setText(df.format(Calendar.getInstance().getTime()));
        //[END create date picker]

        //[START View Amount Of Money]
        mAmountOfMoney = (EditText) findViewById(R.id.amount_diary_edit_text);
        mAmountOfMoney.addTextChangedListener(new MoneyTextWatcher(mAmountOfMoney));
        //[END View Amount Of Money]
        mAmountOfMoneyLayout = (TextInputLayout) findViewById(R.id.layout_amount_diary_edit_text);


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

        //Set Array Adapter for Category Spinner (Default is Expense Category)
        setCategorySpinner(R.array.expense_category);

        //Set Array Adapter for Needed level Spinner
        neededLevelArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.needed_level));
        neededLevelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        neededLevelSpinner = (MaterialSpinner) findViewById(R.id.needed_level_spinner);
        neededLevelSpinner.setAdapter(neededLevelArrayAdapter);

        //Set Array Adapter for Money Source Spinner
        moneySourceSpinner = (MaterialSpinner) findViewById(R.id.money_source_spinner);
        mDatabase.child("user-money-source").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> msNameList = new ArrayList<String>();
                moneySourceKey = new ArrayList<String>();

                for (DataSnapshot moneySourceSnapshot : dataSnapshot.getChildren()) {
                    String msName = moneySourceSnapshot.child("moneySourceName").getValue(String.class);
                    msNameList.add(msName);
                    moneySourceKey.add(moneySourceSnapshot.getKey());
                }
                if (moneySourceArrayAdapter != null) {
                    moneySourceArrayAdapter = null;
                }
                moneySourceArrayAdapter = new ArrayAdapter<String>(DiaryCrudActivity.this, android.R.layout.simple_spinner_item, msNameList);
                moneySourceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                moneySourceSpinner.setAdapter(moneySourceArrayAdapter);

                //Get data if this isn't Add Diary
                Bundle bundle = getIntent().getBundleExtra(BUNDEL_DATA_FROM_DIARY_MGN);
                if (bundle != null) {
                    setAddDiaryActivity(false);
                    mDiaryModel = (Diary) bundle.getSerializable(DIARY_MODEL);
                    mDiaryKey = bundle.getString(DIARY_KEY);
                    fillDataToLayout(mDiaryModel);
                } else {
                    bundle = getIntent().getBundleExtra(BUNDEL_DATA_FORM_PLAN_MGN);
                    if (bundle != null) {
                        //[START FILL DATA] Fill data for add a plan to diary from PlanToSaveMoneyMgnFragment
                        SavingPlan plan = (SavingPlan) bundle.getSerializable(PLAN_FROM_MGN);
                        setAmountOfMoney(plan.savedAmount);
                        String description = "Chi tiêu cho kế hoạch: " + plan.planName;
                        mDescription.setText(description);
                        neededLevelSpinner.setSelection(neededLevelArrayAdapter.getCount());
                        categorySpinner.setSelection(2);
                        moneySourceSpinner.setSelection(moneySourceArrayAdapter.getPosition(getString(R.string.saving_money_source)) + 1);
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        //[END FILL DATA] Fill data for add a plan to diary from PlanToSaveMoneyMgnFragment
                    }
                    setAddDiaryActivity(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Fill data to layout if this is Details Diary
    private void fillDataToLayout(Diary model) {
        if (model == null) {
            return;
        }
        setAmountOfMoney(model.amount);
        mDescription.setText(model.description);
        mExpenseDate.setText(model.date.get("init_date").toString());
        if (model.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
            setIncomeDiary(true);

        } else {
            setIncomeDiary(false);
            neededLevelSpinner.setSelection(neededLevelArrayAdapter.getPosition(model.needLevel) + 1);
        }
        categorySpinner.setSelection(categoryArrayAdapter.getPosition(model.category) + 1);
        moneySourceSpinner.setSelection(moneySourceArrayAdapter.getPosition(model.moneySourceName) + 1);
        mTypeLogSwitch.setChecked(isIncomeDiary());
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
                hideKeyboard(view);
                finish();
                break;
            case R.id.finish_diarycrud_nav_button:
                Diary diary = getDiaryFromLayout();
                if (diary == null) {
                    break;
                }
                if (isAddDiaryActivity()) {
                    submitAddDiary(diary);
                } else {
                    submitUpdateDiary(diary, mDiaryKey);
                }
                hideKeyboard(view);
                finish();
                break;
            case R.id.add_diarycrud_end_button:
                Diary diary2 = getDiaryFromLayout();
                if (diary2 == null) {
                    break;
                }
                submitAddDiary(diary2);
                hideKeyboard(view);
                finish();
                break;
            case R.id.delete_diarycrud_end_button:
                if (mDiaryKey.isEmpty()) {
                    return;
                }
                submitDeleteDiary(mDiaryKey);
                hideKeyboard(view);
                finish();
                break;
            case R.id.update_diarycrud_end_button:
                Diary diary3 = getDiaryFromLayout();
                if (diary3 == null) {
                    break;
                }
                submitUpdateDiary(diary3, mDiaryKey);
                hideKeyboard(view);
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
            mExpenseTextView.setTextColor(getResources().getColor(R.color.gray));
            mExpenseTextView.setTypeface(Typeface.DEFAULT);
            mIncomTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            mIncomTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mAmountOfMoney.setTextColor(getResources().getColor(R.color.colorAccent));
            setCategorySpinner(R.array.income_category);
            neededLevelSpinner.setVisibility(View.GONE);
            moneySourceSpinner.setFloatingLabelText("Đến:");
        } else {
            mExpenseTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            mExpenseTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mIncomTextView.setTextColor(getResources().getColor(R.color.gray));
            mIncomTextView.setTypeface(Typeface.DEFAULT);
            mAmountOfMoney.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            setCategorySpinner(R.array.expense_category);
            neededLevelSpinner.setVisibility(View.VISIBLE);
            moneySourceSpinner.setFloatingLabelText("Từ:");
        }
    }

    //Set Array Adapter for Category Spinner
    public void setCategorySpinner(int idStringArray) {
        categoryArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(idStringArray));
        categoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner = (MaterialSpinner) findViewById(R.id.category_diary_spinner);
        categorySpinner.setAdapter(categoryArrayAdapter);
    }


    public void submitAddDiary(final Diary diary) {
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
                            Toast.makeText(DiaryCrudActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            //Update user information
                            updateUserInfoWhenAddDiary(diary, user);
                            submitUpdateMoneySource(null, diary);

                            // Write new Diary
                            String key = mDatabase.child("diary").push().getKey();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-diary/" + userId + "/" + key, diary.toMap());
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

    // [START delete diary by key]
    public void submitDeleteDiary(final String diaryKey) {
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
                            //Update user information
                            updateUserInfoWhenDeleteDiary(mDiaryModel, user);
                            submitUpdateMoneySource(mDiaryModel, null);

                            // Delete diary by set value is null
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-diary/" + userId + "/" + diaryKey, null);
                            mDatabase.updateChildren(childUpdates);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }
    // [END delete diary by key]

    public void submitUpdateDiary(final Diary diary, final String diaryKey) {
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
                            Toast.makeText(DiaryCrudActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            //Update user information
                            updateUserInfoWhenAddDiary(diary, user);
                            updateUserInfoWhenDeleteDiary(mDiaryModel, user);
                            submitUpdateMoneySource(mDiaryModel, diary);

                            // Update Diray
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-diary/" + userId + "/" + diaryKey, diary.toMap());
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

        if (!isIncomeDiary()) {
            if (needLevel <= 0) {
                neededLevelSpinner.setError("Vui lòng chọn mức độ cần thiết.");
                valid = false;
            } else {
                neededLevelSpinner.setError(null);
            }
        }

        return valid;
    }

    private Diary getDiaryFromLayout() {
        String amount = mAmountOfMoney.getText().toString();
        int categoryPos = categorySpinner.getSelectedItemPosition();
        int moneySourcePos = moneySourceSpinner.getSelectedItemPosition();
        int needLevelPos = neededLevelSpinner.getSelectedItemPosition();
        if (!validateInput(amount, categoryPos, moneySourcePos, needLevelPos)) {
            return null;
        }
        String date = mExpenseDate.getText().toString();
        String category = categoryArrayAdapter.getItem(categoryPos - 1);
        String moneySource = moneySourceArrayAdapter.getItem(moneySourcePos - 1);
        String msid = moneySourceKey.get(moneySourcePos - 1);
        String needLevel = " ";
        if (!isIncomeDiary()) {
            needLevel = neededLevelArrayAdapter.getItem(needLevelPos - 1);
        }
        String description = mDescription.getText().toString();
        String typeLog = getResources().getString(R.string.expense_type);
        if (isIncomeDiary()) {
            typeLog = getResources().getString(R.string.income_type);
        }
        return new Diary(getUid(), msid, moneySource, getAmountOfMoney(), category, description, needLevel, typeLog, date);
    }

    private void updateUserInfoWhenAddDiary(final Diary diary, User user) {
        if (diary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
            user.setTotalBalance(user.totalBalance + diary.amount);
            if (diary.moneySourceName.equalsIgnoreCase(getResources().getString(R.string.saving_money_source))) {
                user.setSavingBalance(user.savingBalance + diary.amount);
            }
        } else {
            user.setTotalBalance(user.totalBalance - diary.amount);
            if (diary.moneySourceName.equalsIgnoreCase(getResources().getString(R.string.saving_money_source))) {
                user.setSavingBalance(user.savingBalance - diary.amount);
            }
        }
        mDatabase.child("users").child(userId).setValue(user);
    }

    private void updateUserInfoWhenDeleteDiary(final Diary diary, User user) {
        if (diary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
            user.setTotalBalance(user.totalBalance - diary.amount);
            if (diary.moneySourceName.equalsIgnoreCase(getResources().getString(R.string.saving_money_source))) {
                user.setSavingBalance(user.savingBalance - diary.amount);
            }
        } else {
            user.setTotalBalance(user.totalBalance + diary.amount);
            if (diary.moneySourceName.equalsIgnoreCase(getResources().getString(R.string.saving_money_source))) {
                user.setSavingBalance(user.savingBalance + diary.amount);
            }
        }
        mDatabase.child("users").child(userId).setValue(user);

        // [Start update balance money source]
        mDatabase.child("user-money-source").child(userId).child(diary.msid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        MoneySource msModel = dataSnapshot.getValue(MoneySource.class);

                        // [START_EXCLUDE]
                        if (msModel == null) {
                            // Money source is null, error out
                            Log.e(TAG, "Money source " + dataSnapshot.getKey() + " is unexpectedly null");
                            Toast.makeText(DiaryCrudActivity.this,
                                    "Error: could not fetch money source.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            if (diary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
                                msModel.currentBalance -= diary.amount;
                            } else {
                                msModel.currentBalance += diary.amount;
                            }
                            mDatabase.child("user-money-source").child(userId).child(diary.msid).setValue(msModel);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "updateBalanceMoneySource:onCancelled", databaseError.toException());
                    }
                });
        // [END update balance money source]
    }

    private void submitUpdateMoneySource(final Diary oldDiary, final Diary newDiary) {
        if (oldDiary != null && newDiary != null) {
            if ((oldDiary.msid).equalsIgnoreCase(newDiary.msid)) {
                // [Start update balance money source ]
                mDatabase.child("user-money-source").child(userId).child(oldDiary.msid).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                MoneySource msModel = dataSnapshot.getValue(MoneySource.class);

                                // [START_EXCLUDE]
                                if (msModel == null) {
                                    // Money source is null, error out
                                    Log.e(TAG, "Money source " + dataSnapshot.getKey() + " is unexpectedly null");
                                    Toast.makeText(DiaryCrudActivity.this,
                                            "Error: could not fetch money source.",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    if (oldDiary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
                                        msModel.currentBalance -= oldDiary.amount;
                                    } else {
                                        msModel.currentBalance += oldDiary.amount;
                                    }
                                    if (newDiary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
                                        msModel.currentBalance += newDiary.amount;
                                    } else {
                                        msModel.currentBalance -= newDiary.amount;
                                    }
                                    mDatabase.child("user-money-source").child(userId).child(oldDiary.msid).setValue(msModel);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "updateBalanceMoneySource:onCancelled", databaseError.toException());
                            }
                        });
                // [END update balance money source]
            } else {
                //when update diary
                // [Start update balance money source ]
                mDatabase.child("user-money-source").child(userId).child(oldDiary.msid).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                MoneySource msModel = dataSnapshot.getValue(MoneySource.class);

                                // [START_EXCLUDE]
                                if (msModel == null) {
                                    // Money source is null, error out
                                    Log.e(TAG, "Money source " + dataSnapshot.getKey() + " is unexpectedly null");
                                    Toast.makeText(DiaryCrudActivity.this,
                                            "Error: could not fetch money source.",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    if (oldDiary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
                                        msModel.currentBalance -= oldDiary.amount;
                                    } else {
                                        msModel.currentBalance += oldDiary.amount;
                                    }
                                    mDatabase.child("user-money-source").child(userId).child(oldDiary.msid).setValue(msModel);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "updateBalanceMoneySource:onCancelled", databaseError.toException());
                            }
                        });
                // [END update balance money source]

                DatabaseReference mDatabaseSourceMoney;
                // [START initialize_database_ref]
                mDatabaseSourceMoney = FirebaseDatabase.getInstance().getReference();
                //For offline
                mDatabaseSourceMoney.keepSynced(true);
                // [END initialize_database_ref]
                mDatabaseSourceMoney.child("user-money-source").child(userId).child(newDiary.msid).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                MoneySource msModel = dataSnapshot.getValue(MoneySource.class);

                                // [START_EXCLUDE]
                                if (msModel == null) {
                                    // Money source is null, error out
                                    Log.e(TAG, "Money source " + dataSnapshot.getKey() + " is unexpectedly null");
                                    Toast.makeText(DiaryCrudActivity.this,
                                            "Error: could not fetch money source.",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    if (newDiary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
                                        msModel.currentBalance += newDiary.amount;
                                    } else {
                                        msModel.currentBalance -= newDiary.amount;
                                    }
                                    mDatabase.child("user-money-source").child(userId).child(newDiary.msid).setValue(msModel);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "updateBalanceMoneySource:onCancelled", databaseError.toException());
                            }
                        });
                // [END update balance money source]
            }
        } else {
            //When add or delete diary
            // [Start update balance money source]
            String moneySourceKey;
            if (oldDiary != null) {
                moneySourceKey = oldDiary.msid;
            } else {
                moneySourceKey = newDiary.msid;
            }
            mDatabase.child("user-money-source").child(userId).child(moneySourceKey).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            MoneySource msModel = dataSnapshot.getValue(MoneySource.class);

                            // [START_EXCLUDE]
                            if (msModel == null) {
                                // Money source is null, error out
                                Log.e(TAG, "Money source " + dataSnapshot.getKey() + " is unexpectedly null");
                                Toast.makeText(DiaryCrudActivity.this,
                                        "Error: could not fetch money source.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                //When Add new Diary
                                if (oldDiary == null && newDiary != null) {
                                    if (newDiary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
                                        msModel.currentBalance += newDiary.amount;
                                    } else {
                                        msModel.currentBalance -= newDiary.amount;
                                    }
                                    mDatabase.child("user-money-source").child(userId).child(newDiary.msid).setValue(msModel);
                                }
                                //When delete Diary
                                if (oldDiary != null && newDiary == null) {
                                    if (oldDiary.type.equalsIgnoreCase(getResources().getString(R.string.income_type))) {
                                        msModel.currentBalance -= oldDiary.amount;
                                    } else {
                                        msModel.currentBalance += oldDiary.amount;
                                    }
                                    mDatabase.child("user-money-source").child(userId).child(oldDiary.msid).setValue(msModel);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "updateBalanceMoneySource:onCancelled", databaseError.toException());
                        }
                    });
            // [END update balance money source]
        }
    }
}
