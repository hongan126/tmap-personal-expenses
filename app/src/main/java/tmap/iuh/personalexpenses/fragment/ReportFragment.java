package tmap.iuh.personalexpenses.fragment;

import android.content.Intent;
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
import android.widget.PopupMenu;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import tmap.iuh.personalexpenses.DetailsReportActivity;
import tmap.iuh.personalexpenses.ListDiaryByMonthActivity;
import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.Report;
import tmap.iuh.personalexpenses.viewholder.ReportViewHolder;

public class ReportFragment extends Fragment {

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
                    int n = 0;
                    double total = 0.0;
                    for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                        Report model = reportSnapshot.getValue(Report.class);
                        total += model.expenseTotal;
                        n++;
                    }

                    double expectedCost = total/(double) n;

                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formatter.applyPattern("#,###,###,###");

                    mExpectedCostTextView.setText((formatter.format((expectedCost)) + "đ"));
                    mExpectedCostTextView.setVisibility(View.VISIBLE);
                    mExpectedLabelTextView.setVisibility(View.VISIBLE);
                    mExpectedLabelTextView.setText("Dự đoán chi phí cho tháng sau:");
                } else {
                    mExpectedCostTextView.setVisibility(View.GONE);
                    mExpectedLabelTextView.setText("Nhật ký thu chi của bạn trống!!!");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
