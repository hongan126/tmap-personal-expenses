package tmap.iuh.personalexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import tmap.iuh.personalexpenses.models.MoneySource;
import tmap.iuh.personalexpenses.models.User;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth_and_database]
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    // [END declare_auth_and_database]

    private GoogleSignInClient mGoogleSignInClient;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private TextInputLayout mEmailTextInputLayout;
    private TextInputLayout mPasswordTextInputLayout;
    private CallbackManager mCallbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // [START initialize_auth_and_database]
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_auth_and_database]

        //Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //View
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        mEmailTextInputLayout = (TextInputLayout) findViewById(R.id.email_login_layout);
        mPasswordTextInputLayout = (TextInputLayout) findViewById(R.id.password_login_layout);

        //Button listener
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.google_login_button).setOnClickListener(this);
        findViewById(R.id.signup_button).setOnClickListener(this);
        findViewById(R.id.forgot_password_button).setOnClickListener(this);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_fblogin]
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
        // [END initialize_fblogin]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and Open MAIN ACTIVITY.
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
    // [END on_start_check_user]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e);
            }
        }

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, start Main Functional Screen
                            Log.d(TAG, "signInWithCredential:success");
                            // Go to MainActivity
                            onAuthSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            onAuthFailed(task);
                        }
                    }
                });
    }
    // [END auth_with_google]

    // [START signin With Google]
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin With Google]

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, start Main Functional Screen
                            Log.d(TAG, "signInWithCredential:success");
                            onAuthSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            onAuthFailed(task);
                        }
                    }
                });
    }
    // [END auth_with_facebook]

    //[START auth_with_email_password]
    private void signInWithEmailPassword() {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();
        Log.d(TAG, "signIn:" + email);
        if (!validateInput(email, password)) {
            return;
        }

        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, start Main Functional Screen
                            onAuthSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            onAuthFailed(task);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    //Check data email and password input. Display message to the user.
    private boolean validateInput(String email, String password) {
        boolean valid = true;

//        if (email.isEmpty()) {
//            mEmailTextInputLayout.setError("Không để trống!");
//            valid = false;
//        } else if (!email.matches("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
//            mEmailTextInputLayout.setError("Email không đúng!");
//            valid = false;
//        } else {
//            mEmailTextInputLayout.setError(null);
//        }
        valid = validateEmail(email);

        if (password.isEmpty()) {
            mPasswordTextInputLayout.setError("Không để trống!");
            valid = false;
        } else {
            mPasswordTextInputLayout.setError(null);
        }
        return valid;
    }

    private boolean validateEmail(String email) {
        boolean valid = true;
        if (email.isEmpty()) {
            mEmailTextInputLayout.setError("Không để trống!");
            valid = false;
        } else if (!email.matches("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
            mEmailTextInputLayout.setError("Email không đúng!");
            valid = false;
        } else {
            mEmailTextInputLayout.setError(null);
        }
        return valid;
    }

    //Auth success start MainActivity (Main functional screen)
    public void onAuthSuccess() {
        // [START_EXCLUDE]
        hideProgressDialog();
        // [END_EXCLUDE]

        FirebaseUser user = mAuth.getCurrentUser();
        String username = getUsername(user);
        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    // Get username
    private String getUsername(FirebaseUser user) {
        // TODO edit
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

    //Auth failed, display a message to the user.
    public void onAuthFailed(@NonNull Task<AuthResult> task) {
        // [START_EXCLUDE]
        hideProgressDialog();
        // [END_EXCLUDE]
        Log.w(TAG, "signInWithCredential:failure", task.getException());
        Toasty.warning(LoginActivity.this, "Đăng nhập thất bại!\nVui lòng kiểm tra lại tài khoản, mật khẩu và kết nối mạng của bạn.", Toast.LENGTH_LONG, true).show();
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
                            MoneySource walletMs = new MoneySource(userId, getResources().getString(R.string.wallet_money_source), 0.0, walletMsKey);
                            String savingMsKey = mDatabase.child("money-source").push().getKey();
                            MoneySource savingMs = new MoneySource(userId, getResources().getString(R.string.saving_money_source), 0.0, savingMsKey);

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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.google_login_button:
                signInWithGoogle();
                hideKeyboard(v);
                break;
            case R.id.login_button:
                signInWithEmailPassword();
                hideKeyboard(v);
                break;
            case R.id.signup_button:
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                hideKeyboard(v);
                finish();
                break;
            case R.id.forgot_password_button:
                final String email = mEmailEditText.getText().toString();
                if (!validateEmail(email)) {
                    return;
                }
                showProgressDialog();
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toasty.success(LoginActivity.this, "Chúng tôi đã gửi email hướng dẫn đặt lại mật khẩu đến " + email, Toast.LENGTH_LONG).show();
                                } else {
                                    Toasty.error(LoginActivity.this, "Không thể gửi email!\nVui lòng kiểm tra lại email\nvà kế nối mạng của bạn!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                hideProgressDialog();
                hideKeyboard(v);
                break;
        }
    }
}
