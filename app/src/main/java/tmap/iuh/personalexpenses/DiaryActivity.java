package tmap.iuh.personalexpenses;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fr.ganfra.materialspinner.MaterialSpinner;

public class DiaryActivity extends AppCompatActivity {

    private ArrayAdapter<String> categoryArrayAdapter;
    private MaterialSpinner categorySpinner;

    private ArrayAdapter<String> moneySourceArrayAdapter;
    private MaterialSpinner moneySourceSpinner;

    private ArrayAdapter<String> neededLevelArrayAdapter;
    private MaterialSpinner neededLevelSpinner;

    private EditText mExpenseDate;
    private EditText mAmountOfMoney;
    private Calendar myCalendar = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

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
        mExpenseDate = (EditText)findViewById(R.id.date_diary_edit_text) ;
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateExpenseDate();
            }
        };
        mExpenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(DiaryActivity.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        mExpenseDate.setText(df.format(Calendar.getInstance().getTime()));
        //[END create date picker]

        //[START View Amount Of Money]
        mAmountOfMoney = (EditText)findViewById(R.id.amount_diary_edit_text);
        mAmountOfMoney.addTextChangedListener(new MoneyTextWatcher(mAmountOfMoney));
        setAmountOfMoney(12300000.0);
        //[END View Amount Of Money]
    }

    private void updateExpenseDate() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mExpenseDate.setText(sdf.format(myCalendar.getTime()));
    }

    private double getAmountOfMoney(){
        return Double.parseDouble(mAmountOfMoney.getText().toString().replaceAll("[$,.]", ""));
    }

    private void setAmountOfMoney(Double amount){
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        mAmountOfMoney.setText(formatter.format((amount)));
    }
}
