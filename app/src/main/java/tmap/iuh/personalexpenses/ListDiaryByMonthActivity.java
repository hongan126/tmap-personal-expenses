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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.Report;
import tmap.iuh.personalexpenses.viewholder.DiaryViewHolder;

public class ListDiaryByMonthActivity extends BaseActivity{

    private static final String TAG = "ListDiaryByMonthActivity";

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
        setContentView(R.layout.activity_list_diary_by_month);

        mReportHeader = (TextView) findViewById(R.id.month_header);

        //Set back button listener
        findViewById(R.id.back_nav_button_diary_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Get data sourceMs
        Bundle bundle = getIntent().getBundleExtra(BUNDEL_MS_DATA);
        if (bundle != null) {
            mReportModel = (Report) bundle.getSerializable(EXTRA_MS_MODEL);
        }
        if (mReportModel != null) {
            mReportHeader.setText(("Tháng " + mReportModel.monthYear));
        }



        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //for offline
        mDatabase.keepSynced(true);
        // [END create_database_refserence]

        mRecycler = findViewById(R.id.diary_list);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Calendar firstDateMonth = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        firstDateMonth.setTimeInMillis(mReportModel.timestamp);
        firstDateMonth.set(firstDateMonth.get(Calendar.YEAR), firstDateMonth.get(Calendar.MONTH), firstDateMonth.getActualMinimum(Calendar.DAY_OF_MONTH), 0,0,0);
        firstDateMonth.set(Calendar.MILLISECOND, 0);

        Calendar lastOfMonth = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        lastOfMonth.setTimeInMillis(mReportModel.timestamp);
        lastOfMonth.set(firstDateMonth.get(Calendar.YEAR), firstDateMonth.get(Calendar.MONTH), firstDateMonth.getActualMaximum(Calendar.DAY_OF_MONTH), 23,59,59);
        lastOfMonth.set(Calendar.MILLISECOND, 999);

        Query diaryQuery = mDatabase.child("user-diary").child(getUid())
                .orderByChild("date/timestamp").startAt(firstDateMonth.getTimeInMillis()).endAt(lastOfMonth.getTimeInMillis());

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
                        Intent intent = new Intent(ListDiaryByMonthActivity.this, DiaryCrudActivity.class);
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
