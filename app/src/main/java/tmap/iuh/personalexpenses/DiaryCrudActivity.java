package tmap.iuh.personalexpenses;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fr.ganfra.materialspinner.MaterialSpinner;

public class DiaryCrudActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayAdapter<String> categoryArrayAdapter;
    private MaterialSpinner categorySpinner;

    private ArrayAdapter<String> moneySourceArrayAdapter;
    private MaterialSpinner moneySourceSpinner;

    private ArrayAdapter<String> neededLevelArrayAdapter;
    private MaterialSpinner neededLevelSpinner;

    private EditText mExpenseDate;
    private EditText mAmountOfMoney;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_crud);

        mDescription = (EditText) findViewById(R.id.description_diary_edit_text);

        //Set Array Adapter for Category Spinner
        categoryArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.diary_filter));
        categoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner = (MaterialSpinner) findViewById(R.id.category_diary_spinner);
        categorySpinner.setAdapter(categoryArrayAdapter);

        //Set Array Adapter for Money Source Spinner
        moneySourceArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.diary_filter));
        moneySourceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moneySourceSpinner = (MaterialSpinner) findViewById(R.id.money_source_spinner);
        moneySourceSpinner.setAdapter(moneySourceArrayAdapter);

        //Set Array Adapter for Needed level Spinner
        neededLevelArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.diary_filter));
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

        //Set type of Diary Activity Todo edit or remove
        setAddDiaryActivity(false);

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
}
