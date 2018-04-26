package tmap.iuh.personalexpenses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Calendar;
import java.util.TimeZone;

import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.Report;
import tmap.iuh.personalexpenses.viewholder.DiaryViewHolder;

public class DetailsReportActivity extends BaseActivity {

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
        int month = cal.get(Calendar.MONTH)+1;
        if(month>10){
            monthStr = String.valueOf(month);
        }else{
            monthStr = "0"+String.valueOf(month);
        }
        long neededLevelStart = Long.parseLong(cal.get(Calendar.YEAR)+""+monthStr+""+2+"");
        long neededLevelEnd = Long.parseLong(cal.get(Calendar.YEAR)+""+monthStr+""+3+"");

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
}
