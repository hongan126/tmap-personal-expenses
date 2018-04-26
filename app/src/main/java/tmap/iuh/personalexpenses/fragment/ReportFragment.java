package tmap.iuh.personalexpenses.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tmap.iuh.personalexpenses.DetailsReportActivity;
import tmap.iuh.personalexpenses.ListDiaryByMonthActivity;
import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.Report;
import tmap.iuh.personalexpenses.viewholder.AmountReportMarkerView;
import tmap.iuh.personalexpenses.viewholder.ReportViewHolder;

public class ReportFragment extends Fragment implements OnChartValueSelectedListener {

    private static final String TAG = "ReportFragment";

    private FirebaseRecyclerAdapter<Report, ReportViewHolder> mAdapter;
    private RecyclerView mRecycler;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private LinearLayoutManager mManager;
    final String userId = getUid();

    private TextView mExpectedCostTextView;
    private TextView mExpectedLabelTextView;
    private LinearLayout mLayoutBarChar;

    //
    private BarChart mBarChart;
    private List<Report> mReportList;

    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //for offline
        mDatabase.keepSynced(true);
        // [END create_database_refserence]

        mRecycler = rootView.findViewById(R.id.report_list);
        mRecycler.setHasFixedSize(true);

        mExpectedCostTextView = (TextView) rootView.findViewById(R.id.expected_cost_for_next_month_edit_text);
        mExpectedLabelTextView = (TextView) rootView.findViewById(R.id.label_expected_cost_text_view);

        mBarChart = (BarChart) rootView.findViewById(R.id.bar_chart);
        AmountReportMarkerView mv = new AmountReportMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(mBarChart); // For bounds control
        mBarChart.setMarker(mv); // Set the marker to the chart
        mLayoutBarChar = (LinearLayout) rootView.findViewById(R.id.linear_layout);

        loadexpectedCost();

        return rootView;
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query reportQuery = mDatabase.child("user-report").child(getUid()).orderByChild("timestamp");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Report>()
                .setQuery(reportQuery, Report.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Report, ReportViewHolder>(options) {
            @Override
            public ReportViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new ReportViewHolder(inflater.inflate(R.layout.item_month_report, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final ReportViewHolder viewHolder, int position, final Report model) {
                final DatabaseReference diaryRef = getRef(position);
                final String reportKey = diaryRef.getKey();

                // Set click listener for the whole money view
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return false;
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for more button
                viewHolder.bindToReport(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.getImageButton());
                        //inflating menu from xml resource
                        popup.inflate(R.menu.menu_report);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_show_diary_list:
                                        Intent intent = new Intent(getActivity(), ListDiaryByMonthActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(ListDiaryByMonthActivity.EXTRA_MS_MODEL, model);
                                        intent.putExtra(ListDiaryByMonthActivity.BUNDEL_MS_DATA, bundle);
                                        startActivity(intent);
                                        break;
                                    case R.id.menu_details:
                                        Intent intent2 = new Intent(getActivity(), DetailsReportActivity.class);
                                        Bundle bundle2 = new Bundle();
                                        bundle2.putSerializable(DetailsReportActivity.EXTRA_MS_MODEL, model);
                                        intent2.putExtra(DetailsReportActivity.BUNDEL_MS_DATA, bundle2);
                                        startActivity(intent2);
                                        break;
                                }
                                return false;
                            }
                        });
                        //displaying the popup
                        popup.show();
                    }
                });
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

    private void loadexpectedCost() {
        //Get latest 4 months report
        mDatabase.child("user-report").child(userId).orderByChild("timestamp").limitToLast(4).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren().iterator().hasNext()) {
                    mReportList = new ArrayList<Report>();
                    int n = 0;
                    double total = 0.0;
                    for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                        Report model = reportSnapshot.getValue(Report.class);
                        mReportList.add(model);
                        total += model.expenseTotal;
                        n++;
                    }

                    double expectedCost = total / (double) n;

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###,###,###");

                    mExpectedCostTextView.setText((formatter.format((expectedCost)) + "đ"));
                    mExpectedCostTextView.setVisibility(View.VISIBLE);
                    mExpectedLabelTextView.setVisibility(View.VISIBLE);
                    mExpectedLabelTextView.setText("Dự đoán chi phí cho tháng sau:");
                    if (mReportList.size() > 0) {
                        mLayoutBarChar.setVisibility(View.VISIBLE);
                        setupBarChart();
                    }
                } else {
                    mExpectedCostTextView.setVisibility(View.GONE);
                    mExpectedLabelTextView.setText("Nhật ký thu chi của bạn trống!!!");
                    mLayoutBarChar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void setupBarChart() {
        //[START Setup Bar Char]
        mBarChart.setOnChartValueSelectedListener(this);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setDrawBorders(true);
        mBarChart.setBorderWidth(1.f);
        mBarChart.setBorderColor(Color.parseColor("#616161"));
        mBarChart.setPinchZoom(true);
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setTouchEnabled(true);
        mBarChart.setDoubleTapToZoomEnabled(false);

        Legend l = mBarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setYOffset(0f);
        l.setXOffset(10f);
        l.setYEntrySpace(0f);
        l.setTextSize(8f);

//        List<String> list = new ArrayList<String>();
//        for(Report report: mReportList){
//            list.add(report.monthYear);
//        }
//        final String[] stringMonthYear = list.toArray(new String[0]);

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int n = (int) value;
                switch (n) {
                    case 0:
                        if(mReportList.size()<1){
                            break;
                        }
                        return mReportList.get(0).monthYear;
                    case 1:
                        if(mReportList.size()<2){
                            break;
                        }
                        return mReportList.get(1).monthYear;
                    case 2:
                        if(mReportList.size()<3){
                            break;
                        }
                        return mReportList.get(2).monthYear;
                    case 3:
                        if(mReportList.size()<4){
                            break;
                        }
                        return mReportList.get(3).monthYear;
                }
                if (n < 0) {
                    return "Tháng";
                } else{
                    return "Tháng";
                }
            }
        });

        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mBarChart.getAxisRight().setEnabled(false);

        float groupSpace = 0.04f;
        float barSpace = 0.03f; // x2 dataset
        float barWidth = 0.45f; // x2 dataset
        // (0.45 + 0.03) * 2 + 0.04 = 1.00 -> interval per "group"

        int startMonth = 0;

        ArrayList<BarEntry> yValsThu = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yValsChi = new ArrayList<BarEntry>();

        int i = 0;
        for (Report report : mReportList) {
            yValsThu.add(new BarEntry(i, (float) report.incomeTotal));
            yValsChi.add(new BarEntry(i, (float) report.expenseTotal));
            i++;
        }

        BarDataSet setThu, setChi;

        if (mBarChart.getData() != null && mBarChart.getData().getDataSetCount() > 0) {

            setThu = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            setChi = (BarDataSet) mBarChart.getData().getDataSetByIndex(1);
            setThu.setValues(yValsThu);
            setChi.setValues(yValsChi);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();

        } else {
            // create 4 DataSets
            setThu = new BarDataSet(yValsThu, "Thu");
            setThu.setColor(Color.parseColor("#64dd17"));
            setChi = new BarDataSet(yValsChi, "Chi");
            setChi.setColor(Color.parseColor("#2196f3"));

            BarData data = new BarData(setThu, setChi);
            data.setValueFormatter(new LargeValueFormatter());

            mBarChart.setData(data);
        }

        // specify the width each bar should have
        mBarChart.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        mBarChart.getXAxis().setAxisMinimum(startMonth);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        mBarChart.getXAxis().setAxisMaximum(startMonth + mBarChart.getBarData().getGroupWidth(groupSpace, barSpace) * mReportList.size());
        mBarChart.groupBars(startMonth, groupSpace, barSpace);
        for (IBarDataSet set : mBarChart.getData().getDataSets()) {
            ((BarDataSet) set).setBarBorderWidth(1.f);
            ((BarDataSet) set).setBarBorderColor(Color.parseColor("#616161"));
        }
        mBarChart.invalidate();
        mBarChart.animateY(800);

        //[END Setup Bar Char]
    }
}
