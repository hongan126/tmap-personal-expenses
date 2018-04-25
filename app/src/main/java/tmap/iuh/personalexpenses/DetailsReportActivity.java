package tmap.iuh.personalexpenses;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import tmap.iuh.personalexpenses.models.Report;

public class DetailsReportActivity extends AppCompatActivity {

    public static final String EXTRA_MS_MODEL = "report_model";
    public static final String BUNDEL_MS_DATA = "data";
    private Report mReportModel;

    private TextView mReportHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_report);

        findViewById(R.id.back_nav_button_details_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mReportHeader = (TextView) findViewById(R.id.header_month_of_report_textView);

        //Get data sourceMs
        Bundle bundle = getIntent().getBundleExtra(BUNDEL_MS_DATA);
        if (bundle != null) {
            mReportModel = (Report) bundle.getSerializable(EXTRA_MS_MODEL);
        }
        if (mReportModel != null) {
            mReportHeader.setText(("Tháng " + mReportModel.monthYear));
        }
    }
}
