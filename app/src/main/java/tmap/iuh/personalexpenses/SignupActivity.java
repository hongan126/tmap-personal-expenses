package tmap.iuh.personalexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.User;

public class SignupActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Sign-up";

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mPassConfirmEditText;
    private TextInputLayout mEmailEditLayout;
    private TextInputLayout mPasswordEditLayout;
    private TextInputLayout mPassConfirmEditLayout;
    private Button mFinishSignupButton;
    private Button mCancelButton;

    // [START declare_auth_and_database]
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    // [END declare_auth_and_database]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //View
        mEmailEditText = (EditText) findViewById(R.id.signup_email_edit_text);
        mPassConfirmEditText = (EditText) findViewById(R.id.signup_password_confirm_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.signup_password_edit_text);
        mEmailEditLayout = (TextInputLayout) findViewById(R.id.signup_email_layout);
        mPassConfirmEditLayout = (TextInputLayout) findViewById(R.id.signup_password_confirm_layout);
        mPasswordEditLayout = (TextInputLayout) findViewById(R.id.signup_password_layout);

        //Button Listener
        mFinishSignupButton = (Button) findViewById(R.id.finish_signup_button);
        mCancelButton = (Button) findViewById(R.id.cancel_signup_button);
        mFinishSignupButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        // [START initialize_auth_and_database]
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_auth_and_database]
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.finish_signup_button) {
            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            String passConfirm = mPassConfirmEditText.getText().toString();
            Log.d(TAG, "signIn:" + email);

            if (!validateInput(email, password, passConfirm)) {
                return;
            }

            // [START_EXCLUDE]
            showProgressDialog();
            // [END_EXCLUDE]

            // [START create_user_with_email]
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, start Main Functional Screen
                                Log.d(TAG, "createUserWithEmail:success");

                                // Write new user
                                FirebaseUser user = mAuth.getCurrentUser();
                                String username = getUsername(user);
                                writeNewUser(user.getUid(), username, user.getEmail());

                                sendEmailVerification();
                                Toasty.success(SignupActivity.this, "Tạo tài khoản thành công.",
                                        Toast.LENGTH_LONG, true).show();
                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toasty.error(SignupActivity.this,
                                        "Quá trình đăng ký đã thất bại!\nCó thể email đã được đăng ký!\nVui lòng kiểm tra lại kết nối mạng.",
                                        Toast.LENGTH_LONG, true).show();
                            }
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }
                    });
            // [END create_user_with_email]
            hideKeyboard(view);
        }

        if (view.getId() == R.id.cancel_signup_button) {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            hideKeyboard(view);
            finish();
        }
    }

    private void sendEmailVerification() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        if (task.isSuccessful()) {
                            Toasty.success(SignupActivity.this,
                                    "Cảm ơn bạn, " + user.getEmail(),
                                    Toast.LENGTH_SHORT, true).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toasty.warning(SignupActivity.this,
                                    "Có vẻ email của bạn không có thực?",
                                    Toast.LENGTH_SHORT, true).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    //Check input data and show error.
    public boolean validateInput(String email, String password, String passConfirm) {
        boolean valid = true;
        if (email.isEmpty()) {
            mEmailEditLayout.setError("Không để trống!");
            valid = false;
        } else if (!email.matches("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
            mEmailEditLayout.setError("Email không đúng!");
            valid = false;
        } else {
            mEmailEditLayout.setError(null);
        }

        if (password.isEmpty()) {
            mPasswordEditLayout.setError("Không để trống!");
            valid = false;
        } else if (password.length() < 8) {
            mPasswordEditLayout.setError("Tối thiểu 8 ký tự!");
            valid = false;
        } else {
            mPasswordEditLayout.setError(null);
        }

        if (passConfirm.isEmpty()) {
            mPassConfirmEditLayout.setError("Không để trống!");
            valid = false;
        } else if (passConfirm.length() < 8) {
            mPassConfirmEditLayout.setError("Tối thiểu 8 ký tự!");
            valid = false;
        } else if (!password.equals(passConfirm)) {
            mPassConfirmEditLayout.setError("Mật khẩu không khớp!");
            valid = false;
        } else {
            mPassConfirmEditLayout.setError(null);
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
    }

    // Write database user info
    private void writeNewUser(final String userId, final String name, final String email) {
        // [START single_value_read]
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            User newUser = new User(name, email);
                            mDatabase.child("users").child(userId).setValue(newUser);

                            String walletMsKey = mDatabase.child("money-source").push().getKey();
                            MoneySource walletMs = new MoneySource(userId, getResources().getString(R.string.wallet_money_source), 0, walletMsKey);
                            String savingMsKey = mDatabase.child("money-source").push().getKey();
                            MoneySource savingMs = new MoneySource(userId, getResources().getString(R.string.saving_money_source), 0, savingMsKey);

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/user-money-source/" + userId + "/" + walletMsKey, walletMs.toMap());
                            childUpdates.put("/user-money-source/" + userId + "/" + savingMsKey, savingMs.toMap());
                            mDatabase.updateChildren(childUpdates);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }

    // Get username
    private String getUsername(FirebaseUser user) {
        // TODO delete
//        if(user.getDisplayName()!=null){
//            return user.getDisplayName();
//        }

        String email = user.getEmail();
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}
