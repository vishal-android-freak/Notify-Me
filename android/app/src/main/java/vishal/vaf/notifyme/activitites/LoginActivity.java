package vishal.vaf.notifyme.activitites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import vishal.vaf.notifyme.R;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private ProgressBar progressBar;
    private EditText phone;
    private boolean mVerificationInProgress = false;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private String phoneNumber;

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private FirebaseAuth mAuth;
    private int state;

    private static final int STATE_CODE_SENT = 1;
    private static final int STAT_TIME_OUT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);
        phone = findViewById(R.id.phone);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = phone.getText().toString();
                switch (state) {
                    case STATE_CODE_SENT:
                        verifyOtp(mVerificationId, phone.getText().toString());
                        break;
                    case STAT_TIME_OUT:
                        resendOtp(phoneNumber, mResendToken);
                        break;
                    default:
                        if (isPhoneNumberValid())
                            sendOtp(phoneNumber);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                mVerificationInProgress = false;
                signInWithCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mVerificationInProgress = false;
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Verification failed. Try again.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                state = STATE_CODE_SENT;
                loginButton.setText("Verify");
                phone.setText("");
                phone.setHint("Enter verification code");
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                state = STAT_TIME_OUT;
                loginButton.setText("Resend OTP");
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVerificationInProgress && isPhoneNumberValid())
            sendOtp(phoneNumber);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void sendOtp(String phoneNumber) {
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
        mVerificationInProgress = true;
    }

    private void verifyOtp(String mVerificationId, String code) {
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithCredentials(credential);
    }

    private void resendOtp(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        );
    }

    private void signInWithCredentials(PhoneAuthCredential credential) {
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                            editor.putBoolean("is_logged_in", true);
                            editor.putString("phone", phoneNumber);
                            editor.apply();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private boolean isPhoneNumberValid() {
        return Patterns.PHONE.matcher(phoneNumber).matches();
    }
}
