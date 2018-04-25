package tmap.iuh.personalexpenses.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import tmap.iuh.personalexpenses.DetailsReportActivity;
import tmap.iuh.personalexpenses.ListDiaryByMonthActivity;
import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.TransferActivity;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.Report;
import tmap.iuh.personalexpenses.viewholder.MoneySourceViewHolder;
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
        Query postsQuery = mDatabase.child("user-report").child(getUid()).orderByChild("timestamp");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Report>()
                .setQuery(postsQuery, Report.class)
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
}
