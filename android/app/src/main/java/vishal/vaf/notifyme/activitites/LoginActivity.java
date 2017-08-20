package vishal.vaf.notifyme.activitites;

import android.Manifest;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import vishal.vaf.notifyme.R;

public class LoginActivity extends AppCompatActivity {

    private static final int SMS_REQUEST_CODE = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkPermissions();

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otpViaSms();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SMS_REQUEST_CODE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (loginResult.getError() != null) {

            } else if (loginResult.wasCancelled()) {

            } else {
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {

                        final String phoneNumberString = account.getPhoneNumber().toString();
                        Log.d("number", phoneNumberString);
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().putBoolean("is_logged_in", true).apply();
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().putString("phone", phoneNumberString).apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    @Override
                    public void onError(final AccountKitError error) {
                        // Handle Error
                    }
                });
            }
        }
    }

    private void checkPermissions() {

        PermissionListener listener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
                Toast.makeText(LoginActivity.this, "App cannot function without sufficient permissions.", Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(this)
                .setPermissionListener(listener)
                .setPermissions(Manifest.permission.RECEIVE_SMS)
                .setDeniedMessage("App cannot function without all the necessary permissions. \n\nPlease turn on permissions at [Setting] > [Permission]")
                .check();
    }

    private void otpViaSms() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN)
                        .setReceiveSMS(true);
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, SMS_REQUEST_CODE);
    }
}
