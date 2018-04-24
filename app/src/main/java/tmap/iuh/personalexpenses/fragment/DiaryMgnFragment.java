package tmap.iuh.personalexpenses.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tmap.iuh.personalexpenses.DiaryCrudActivity;
import tmap.iuh.personalexpenses.R;
import tmap.iuh.personalexpenses.models.Diary;
import tmap.iuh.personalexpenses.models.User;
import tmap.iuh.personalexpenses.viewholder.DiaryViewHolder;

public class DiaryMgnFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "DiaryMgnFragment";

    private Spinner spinner;
    private ArrayAdapter<String> spinnerArrayAdapter;

    private ImageView mImgBalance;
    private ImageView mImgSavingMoney;
    private ImageView mImgLimit;

    private TextView mTvBalance;
    private TextView mTvSavingMoney;
    private TextView mTvLimit;

    private double balance = -1.0;
    private double savingAmount = -1.0;
    private double limit = -1.0;

    private String hideBalanceStr;

    private FirebaseRecyclerAdapter<Diary, DiaryViewHolder> mAdapter;
    private RecyclerView mRecycler;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]
    private LinearLayoutManager mManager;

    public DiaryMgnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_diary_mgn, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //for offline
        mDatabase.keepSynced(true);
        // [END create_database_refserence]

        mRecycler = rootView.findViewById(R.id.diary_list);
        mRecycler.setHasFixedSize(true);

        rootView.findViewById(R.id.fab_new_diary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DiaryCrudActivity.class));
            }
        });

        //Get hide balance string: ********d
        hideBalanceStr = getResources().getString(R.string.hide_balance_str);

        // [START_FilterLog: Selection of the spinner]
        spinner = (Spinner) rootView.findViewById(R.id.diary_filter_spinner);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.diary_filter));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadDiaryListBy(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // [END_FilterLog Selection of the spinner]

        //Eye icon hide/show balances
        mImgBalance = (ImageView) rootView.findViewById(R.id.img_eye_balance_diary);
        mImgSavingMoney = (ImageView) rootView.findViewById(R.id.img_eye_saving_money_diary);
        mImgLimit = (ImageView) rootView.findViewById(R.id.img_eye_limit_diary);
        mImgBalance.setOnClickListener(this);
        mImgSavingMoney.setOnClickListener(this);
        mImgLimit.setOnClickListener(this);

        //Text view balance
        mTvBalance = (TextView) rootView.findViewById(R.id.balance_diary_text_view);
        mTvSavingMoney = (TextView) rootView.findViewById(R.id.saving_money_diary_text_view);
        mTvLimit = (TextView) rootView.findViewById(R.id.limit_diary_text_view);

        //Load data balance
        loadDataBalance();

        return rootView;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.img_eye_balance_diary:
                toggleBalances(mImgBalance, mTvBalance);
                break;
            case R.id.img_eye_saving_money_diary:
                toggleBalances(mImgSavingMoney, mTvSavingMoney);
                break;
            case R.id.img_eye_limit_diary:
                toggleBalances(mImgLimit, mTvLimit);
                break;
        }
    }

    public void toggleBalances(ImageView eyeImg, TextView balanceView) {
        if (balanceView.getText().toString().equalsIgnoreCase(hideBalanceStr)) {
            eyeImg.setImageResource(R.drawable.ic_eye_gray_24dp);
            int i = balanceView.getId();
            switch (i) {
                case R.id.balance_diary_text_view:
                    //Todo edit
                    balanceView.setText(convertMoneyToString(balance));
                    break;
                case R.id.saving_money_diary_text_view:
                    //Todo edit
                    balanceView.setText(convertMoneyToString(savingAmount));
                    break;
                case R.id.limit_diary_text_view:
                    //Todo edit
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
        //Todo change cho other currency
        return formatter.format((amount)) + "đ";
    }

    //Parameter: selectedPostion
    // 0: today
    // 1: this month
    // 2: all of diary list
    private void loadDiaryListBy(int selectedPostion) {
        //For reload list when selected diary filter
        if (mAdapter != null) {
            mAdapter.stopListening();
        }

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date mdate = new Date();
        postsQuery = mDatabase.child("user-diary").child(getUid()).orderByChild("date/init_date").equalTo(df.format(mdate));
        switch (selectedPostion) {
            case 1:
                Calendar cal = Calendar.getInstance();
                postsQuery = mDatabase.child("user-diary").child(getUid())
                        .orderByChild("date/month_year").equalTo((cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
                break;
            case 2:
                postsQuery = mDatabase.child("user-diary").child(getUid());
                break;
        }

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Diary>()
                .setQuery(postsQuery, Diary.class)
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
                        Intent intent = new Intent(getActivity(), DiaryCrudActivity.class);
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

        //For reload list when selected diary filter
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        mTvBalance.setText(convertMoneyToString(balance));
    }

    public double getSavingAmount() {
        return savingAmount;
    }

    public void setSavingAmount(double savingAmount) {
        this.savingAmount = savingAmount;
        mTvSavingMoney.setText(convertMoneyToString(savingAmount));
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
                    setSavingAmount(user.savingBalance);
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
