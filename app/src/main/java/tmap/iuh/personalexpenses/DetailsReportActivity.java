package tmap.iuh.personalexpenses;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import es.dmoral.toasty.Toasty;
import tmap.iuh.personalexpenses.models.Category;
import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.Report;
import tmap.iuh.personalexpenses.viewholder.DiaryViewHolder;

public class DetailsReportActivity extends BaseActivity implements OnChartValueSelectedListener {

    public static final String EXTRA_MS_MODEL = "report_model";
    public static final String BUNDEL_MS_DATA = "data";
    private Report mReportModel;

    private TextView mReportHeader;

    private FirebaseRecyclerAdapter<Diary, DiaryViewHolder> mAdapter;
    private RecyclerView mRecycler;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private LinearLayoutManager mManager;

    //Chart
    private PieChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_report);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //for offline
        mDatabase.keepSynced(true);
        // [END create_database_refserence]

        mRecycler = findViewById(R.id.expense_log_list);
        mRecycler.setHasFixedSize(true);

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
            mReportHeader.setText(("Phân tích chi tiêu tháng " + mReportModel.monthYear));
        }

        loadDiaryList();

        //[START setup chart]
        mChart = (PieChart) findViewById(R.id.pie_chart);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.setDrawHoleEnabled(false);

        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setRotationAngle(0);

        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData(mReportModel);

        mChart.animateY(1200, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTextSize(12f);
        //[END setup chart]


    }

    private void loadDiaryList() {
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        cal.setTimeInMillis(mReportModel.timestamp);
        String monthStr;
        int month = cal.get(Calendar.MONTH) + 1;
        if (month > 10) {
            monthStr = String.valueOf(month);
        } else {
            monthStr = "0" + String.valueOf(month);
        }
        long neededLevelStart = Long.parseLong(cal.get(Calendar.YEAR) + "" + monthStr + "" + 2 + "");
        long neededLevelEnd = Long.parseLong(cal.get(Calendar.YEAR) + "" + monthStr + "" + 3 + "");

        Query diaryQuery = mDatabase.child("user-diary").child(getUid())
                .orderByChild("neededLevel_month").startAt(neededLevelStart).endAt(neededLevelEnd);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Diary>()
                .setQuery(diaryQuery, Diary.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Diary, DiaryViewHolder>(options) {
            @Override
            public DiaryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new DiaryViewHolder(inflater.inflate(R.layout.item_diary, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(DiaryViewHolder viewHolder, int position, final Diary model) {
                final DatabaseReference diaryRef = getRef(position);
                final String diaryKey = diaryRef.getKey();

                // Set click listener for the whole diary view
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DetailsReportActivity.this, DiaryCrudActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(DiaryCrudActivity.DIARY_MODEL, model);
                        bundle.putString(DiaryCrudActivity.DIARY_KEY, diaryKey);
                        intent.putExtra(DiaryCrudActivity.BUNDEL_DATA_FROM_DIARY_MGN, bundle);
                        startActivity(intent);
                    }
                });

                // Bind Diary to ViewHolder
                viewHolder.bindToDiary(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    private void setData(Report report) {

        String[] mFieldKey = Category.mFieldKey;
        int n = mFieldKey.length;

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        for (int i = 0; i < n; i++) {
            String keyName = mFieldKey[i];
            if (report.category.containsKey(keyName)) {
                entries.add(new PieEntry(Float.parseFloat(report.category.get(mFieldKey[i]).toString()), Category.getFieldName(keyName)));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Danh mục");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

//        for (int c : ColorTemplate.PASTEL_COLORS)
//            colors.add(c);

//        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        PieEntry pie = (PieEntry)e;

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        String amount = formatter.format(e.getY())+"đ";

        Toasty.info(DetailsReportActivity.this, pie.getLabel()+": " + amount, Toast.LENGTH_SHORT, true).show();

//        Log.i("VAL SELECTED",
//                "Value: " + pie.getValue() + "- Label" + pie.getLabel()+ ", index: " + h.getX()
//                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {

    }
}
