package tmap.iuh.personalexpenses;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DetailsReportActivity extends AppCompatActivity {

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
    }
}
