package vishal.vaf.notifyme.activitites;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import vishal.vaf.notifyme.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private Switch notifyModeSwitch, assistSwitch, whatsappSwitch;
    private SharedPreferences.Editor editor;
    private boolean fromSettings = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fromSettings = true;
        if (isNotificationServiceEnabled()) {
            assistSwitch.setChecked(true);
            whatsappSwitch.setChecked(true);
            editor.putBoolean("is_assist_on", true);
            editor.putBoolean("is_whatsapp_on", true);
            editor.apply();
        } else {
            assistSwitch.setChecked(false);
            whatsappSwitch.setChecked(false);
            editor.putBoolean("is_assist_on", false);
            editor.putBoolean("is_whatsapp_on", false);
            editor.apply();
        }
        fromSettings = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NotoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        notifyModeSwitch = (Switch) findViewById(R.id.enable_notify_switch);
        if (sharedPreferences.getBoolean("is_notify_on", false)) {
            notifyModeSwitch.setChecked(true);
        } else {
            notifyModeSwitch.setChecked(false);
        }
        assistSwitch = (Switch) findViewById(R.id.enable_assist_switch);
        if (sharedPreferences.getBoolean("is_assist_on", false)) {
            assistSwitch.setChecked(true);
        } else {
            assistSwitch.setChecked(false);
        }
        assistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!isNotificationServiceEnabled()) {
                        startActivityForResult(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS), 0);
                    }
                } else {
                    if (!fromSettings)
                    startActivityForResult(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS), 0);
                }
            }
        });
        whatsappSwitch = (Switch) findViewById(R.id.whatsapp_switch);
        if (sharedPreferences.getBoolean("is_whatsapp_on", false)) {
            whatsappSwitch.setChecked(true);
        } else {
            whatsappSwitch.setChecked(false);
        }

        Button whatsAppCustomize = (Button) findViewById(R.id.customize_whatsapp);
        whatsAppCustomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CustomizationActivity.class);
                intent.putExtra("package", "WhatsApp");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
