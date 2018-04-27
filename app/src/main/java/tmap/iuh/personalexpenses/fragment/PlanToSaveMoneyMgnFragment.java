package tmap.iuh.personalexpenses.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import tmap.iuh.personalexpenses.DiaryCrudActivity;
import tmap.iuh.personalexpenses.PlanCrupActivity;
import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.SavingPlan;
import tmap.iuh.personalexpenses.models.User;
import tmap.iuh.personalexpenses.viewholder.SavingPlanViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlanToSaveMoneyMgnFragment extends Fragment {

    private static final String TAG = "PlanToSaveMoneyMgn";

    private FirebaseRecyclerAdapter<SavingPlan, SavingPlanViewHolder> mAdapter;
    private RecyclerView mRecycler;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private LinearLayoutManager mManager;
    final String userId = getUid();

    private TextView mSavingTips;

    private boolean hadPlan = false;
    private double savingBalanceOfUser = 0.0;

    public PlanToSaveMoneyMgnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_plan_to_save_money_mgn, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //for offline
        mDatabase.keepSynced(true);
        // [END create_database_refserence]

        mRecycler = rootView.findViewById(R.id.saving_plan_list);
        mRecycler.setHasFixedSize(true);

        mSavingTips = (TextView) rootView.findViewById(R.id.saving_tip_text_view);

        rootView.findViewById(R.id.fab_new_plan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getHadPlan()) {
                    AlertDialog.Builder builderWarning = new AlertDialog.Builder(getActivity());
                    builderWarning.setMessage(getResources().getString(R.string.warning_add_saving_plan_message));
                    builderWarning.setTitle(getResources().getString(R.string.warning_add_saving_plan_title));
                    builderWarning.setIcon(R.drawable.ic_warning_28p);
                    builderWarning.setNegativeButton(getResources().getString(R.string.warning_add_saving_plan_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog dialogWarning = builderWarning.create();
                    dialogWarning.show();
                    return;
                }
                startActivity(new Intent(getActivity(), PlanCrupActivity.class));
            }
        });

        //[Real-time] for User's saving balance
        mDatabase.child("users").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);

                if (user != null) {
                    savingBalanceOfUser = user.savingBalance;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });

        //[Real-time] If have one plan don't finish, set had plan state is true. And show saving tips. And update plan.
        mDatabase.child("user-saving-plan").child(getUid()).orderByChild("done").equalTo(false).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    setHadPlan(false);
                } else {
                    setHadPlan(true);

                    //Get plan in saving
                    Iterable<DataSnapshot> list = dataSnapshot.getChildren();
                    DataSnapshot dataSnapshotPlan = list.iterator().next();
                    SavingPlan planInSaving = dataSnapshotPlan.getValue(SavingPlan.class);
                    String planKey = dataSnapshotPlan.getKey();

                    //If saving money source has enough money for the plan, update plan and set had plan state = false
                    if (savingBalanceOfUser >= planInSaving.targetAmount) {
                        planInSaving.done = true;
                        planInSaving.savedAmount = planInSaving.targetAmount;
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/user-saving-plan/" + userId + "/" + planKey, planInSaving.toMap());
                        mDatabase.updateChildren(childUpdates);
                        setHadPlan(false);

                        //Notifying the user that the savings account is sufficient for the plan.
                        AlertDialog.Builder builderWarning = new AlertDialog.Builder(getActivity());
                        builderWarning.setMessage(getResources().getString(R.string.notify_plan_done_message, planInSaving.planName));
                        builderWarning.setTitle(getResources().getString(R.string.notify_plan_done_title));
                        builderWarning.setIcon(R.drawable.ic_done_green_24dp);
                        builderWarning.setNegativeButton(getResources().getString(R.string.notify_plan_done_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog dialogWarning = builderWarning.create();
                        dialogWarning.show();
                    }else {
                        //Set plan saved amount by user saving balance
                        planInSaving.savedAmount = savingBalanceOfUser;

                        //Update saving tips
                        Date nowDate = new Date();
                        Date dueDate = stringToDate(planInSaving.dueDate.get("init_date").toString());
                        int daysOfSaving = daysBetween(nowDate, dueDate);
                        double amountPerDay = (planInSaving.targetAmount - planInSaving.savedAmount) / daysOfSaving;
                        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                        formatter.applyPattern("#,###,###,###");
                        String amountPerDayStr = formatter.format(amountPerDay);
                        String savingTipStr = "Từ ngày hôm nay tới ngày: " + planInSaving.dueDate.get("init_date").toString() + "\nMỗi ngày hãy tiết kiệm: " + amountPerDayStr + "đ";
                        mSavingTips.setText(savingTipStr);

                        //Else: Update plan
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/user-saving-plan/" + userId + "/" + planKey, planInSaving.toMap());
                        mDatabase.updateChildren(childUpdates);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });

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
        Query planQuery = mDatabase.child("user-saving-plan").child(getUid());

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<SavingPlan>()
                .setQuery(planQuery, SavingPlan.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<SavingPlan, SavingPlanViewHolder>(options) {
            @Override
            public SavingPlanViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new SavingPlanViewHolder(inflater.inflate(R.layout.item_plan, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final SavingPlanViewHolder viewHolder, int position, final SavingPlan model) {
                final DatabaseReference diaryRef = getRef(position);
                final String savingPlanKey = diaryRef.getKey();

                // Bind Post to ViewHolder, setting OnClickListener for more button
                viewHolder.bindToSavingPlan(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.getImageButton());
                        //inflating menu from xml resource
                        popup.inflate(R.menu.menu_saving_plan);
                        if (model.savedAmount < model.targetAmount) {
                            popup.getMenu().findItem(R.id.menu_add_diary_plan).setVisible(false);
                        }
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_add_diary_plan:
                                        Intent intent = new Intent(getActivity(), DiaryCrudActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(DiaryCrudActivity.PLAN_FROM_MGN, model);
                                        intent.putExtra(DiaryCrudActivity.BUNDEL_DATA_FORM_PLAN_MGN, bundle);
                                        startActivity(intent);
                                        break;
                                    case R.id.menu_remove_plan:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage(getResources().getString(R.string.remove_message));
                                        builder.setTitle(getResources().getString(R.string.remove_plan_title, model.planName));
                                        builder.setNegativeButton(getResources().getString(R.string.remove_plan_cancel),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        builder.setPositiveButton(getResources().getString(R.string.remove_plan_remove),
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        submitDeleteSavingPlan(savingPlanKey);
                                                        Toasty.success(getActivity(), "Xóa hoàn tất!", Toast.LENGTH_SHORT, true).show();
                                                        dialog.cancel();
                                                    }
                                                });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
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

    // [START delete money source by key]
    public void submitDeleteSavingPlan(final String savingPlanKey) {
        mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toasty.error(getActivity(), "Error: could not fetch user.", Toast.LENGTH_SHORT, true).show();
                        } else {
                            // Delete saving plan by set value is null
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-saving-plan/" + userId + "/" + savingPlanKey, null);
                            mDatabase.updateChildren(childUpdates);
                        }
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }
    // [END delete money source by key]


    public boolean getHadPlan() {
        return hadPlan;
    }

    public void setHadPlan(boolean hadPlan) {
        this.hadPlan = hadPlan;
        if (hadPlan) {
            mSavingTips.setVisibility(View.VISIBLE);
        } else {
            if(mAdapter.getItemCount()<=0){
                mSavingTips.setVisibility(View.VISIBLE);
                mSavingTips.setText("Hiện tại bạn chưa có kế hoạch nào!");
            }else {
                mSavingTips.setText("");
                mSavingTips.setVisibility(View.GONE);
            }
        }
    }

    private Date stringToDate(String dateStr) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date dueDate = new Date();
        try {
            dueDate = df.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dueDate;
    }

    public int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24)) + 1;
    }

}
