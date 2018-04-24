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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import tmap.iuh.personalexpenses.AddMoneySourceActivity;
import tmap.iuh.personalexpenses.DiaryCrudActivity;
import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.TransferActivity;
import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.User;
import tmap.iuh.personalexpenses.viewholder.DiaryViewHolder;
import tmap.iuh.personalexpenses.viewholder.MoneySourceViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoneySourceMgnFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MoneySourceMgnFragment";

    private ImageView mImgBalance;
    private ImageView mImgLimit;

    private TextView mTvBalance;
    private TextView mTvLimit;

    private String hideBalanceStr;

    private double balance = -1.0;
    private double limit = -1.0;

    private FirebaseRecyclerAdapter<MoneySource, MoneySourceViewHolder> mAdapter;
    private RecyclerView mRecycler;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private LinearLayoutManager mManager;
    final String userId = getUid();

    public MoneySourceMgnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_money_source_mgn, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //for offline
        mDatabase.keepSynced(true);
        // [END create_database_refserence]

        mRecycler = rootView.findViewById(R.id.money_source_list);
        mRecycler.setHasFixedSize(true);

        //Get hide balance string: ********d
        hideBalanceStr = getResources().getString(R.string.hide_balance_str);

        //Add Float Button Listener
        rootView.findViewById(R.id.fab_new_money_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddMoneySourceActivity.class));
            }
        });

        //Eye icon hide/show balances
        mImgBalance = (ImageView) rootView.findViewById(R.id.img_eye_balance_moneysrc);
        mImgLimit = (ImageView) rootView.findViewById(R.id.img_eye_limit_moneysrc);
        mImgBalance.setOnClickListener(this);
        mImgLimit.setOnClickListener(this);

        //Text view balance
        mTvBalance = (TextView) rootView.findViewById(R.id.balance_moneysrc_text_view);
        mTvLimit = (TextView) rootView.findViewById(R.id.limit_moneysrc_text_view);

        //Load data balance
        loadDataBalance();

        return rootView;
    }

    public void showDialogRemoveMoneySource(final String moneySourceKey, final MoneySource model) {
        if(moneySourceKey.isEmpty()){
            return;
        }
        if (model.moneySourceName.equalsIgnoreCase(getResources().getString(R.string.saving_money_source))
                || model.moneySourceName.equalsIgnoreCase(getResources().getString(R.string.wallet_money_source))){
            AlertDialog.Builder builderWarning = new AlertDialog.Builder(getActivity());
            builderWarning.setMessage(getResources().getString(R.string.warning_remove_ms_message));
            builderWarning.setTitle(getResources().getString(R.string.warning_remove_ms_title));
            builderWarning.setIcon(R.drawable.ic_warning_28p);
            builderWarning.setNegativeButton(getResources().getString(R.string.warning_remove_ms_cancel),
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.remove_ms_message));
        builder.setTitle(getResources().getString(R.string.remove_ms_title));
        builder.setIcon(R.drawable.ic_warning_28p);
        builder.setNegativeButton(getResources().getString(R.string.remove_ms_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setPositiveButton(getResources().getString(R.string.remove_ms_remove),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        submitDeleteMoneySource(moneySourceKey, model);
                        Toast.makeText(getActivity(), "Xóa hoàn tất!", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.img_eye_balance_moneysrc:
                toggleBalances(mImgBalance, mTvBalance);
                break;
            case R.id.img_eye_limit_moneysrc:
                toggleBalances(mImgLimit, mTvLimit);
                break;
        }
    }

    public void toggleBalances(ImageView eyeImg, TextView balanceView) {
        if (balanceView.getText().toString().equalsIgnoreCase(hideBalanceStr)) {
            eyeImg.setImageResource(R.drawable.ic_eye_gray_24dp);
            int i = balanceView.getId();
            switch (i) {
                case R.id.balance_moneysrc_text_view:
                    balanceView.setText(convertMoneyToString(balance));
                    break;
                case R.id.limit_moneysrc_text_view:
                    balanceView.setText(convertMoneyToString(limit));
                    break;
            }
        } else {
            eyeImg.setImageResource(R.drawable.ic_eye_off_gray_24dp);
            balanceView.setText(hideBalanceStr);
        }

    }

    private String convertMoneyToString(Double amount) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###,###,###");
        return formatter.format((amount)) + "đ";
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
        Query postsQuery = mDatabase.child("user-money-source").child(getUid());

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<MoneySource>()
                .setQuery(postsQuery, MoneySource.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<MoneySource, MoneySourceViewHolder>(options) {
            @Override
            public MoneySourceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MoneySourceViewHolder(inflater.inflate(R.layout.item_money_source, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final MoneySourceViewHolder viewHolder, int position, final MoneySource model) {
                final DatabaseReference diaryRef = getRef(position);
                final String moneySourceKey = diaryRef.getKey();

                // Set click listener for the whole money view
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return false;
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for more button
                viewHolder.bindToMoneySource(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getActivity(), viewHolder.getImageButton());
                        //inflating menu from xml resource
                        popup.inflate(R.menu.menu_money_source);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_transfer_monsrc:
                                        Intent intent = new Intent(getActivity(), TransferActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(TransferActivity.EXTRA_MS_MODEL, model);
                                        bundle.putString(TransferActivity.EXTRA_MS_KEY, moneySourceKey);
                                        intent.putExtra(TransferActivity.BUNDEL_MS_DATA, bundle);
                                        startActivity(intent);
                                        break;
                                    case R.id.menu_remove_monsrc:
                                        showDialogRemoveMoneySource(moneySourceKey, model);
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // [START delete money source by key]
    public void submitDeleteMoneySource(final String moneySourceKey, final MoneySource moneySourceModel) {
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
                            Toast.makeText(getActivity(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            //Update user info
                            user.setTotalBalance(user.totalBalance - moneySourceModel.currentBalance);
                            mDatabase.child("users").child(userId).setValue(user);

                            mDatabase.child("user-diary").child(userId).orderByChild("msid").equalTo(moneySourceKey).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    dataSnapshot.getRef().setValue(null);
//                                    mDatabase.child("user-diary").child(userId).child(dataSnapshot.getKey()).setValue(null);
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            // Delete money source by set value is null
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-money-source/" + userId + "/" + moneySourceKey, null);
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        mTvBalance.setText(convertMoneyToString(balance));
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
        mTvLimit.setText(convertMoneyToString(limit));
    }

    public void loadDataBalance(){
        mDatabase.child("users").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user != null) {
                    setBalance(user.totalBalance);
                    setLimit(user.limitAmount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });
    }
}
