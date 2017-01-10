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
import vishal.vaf.notifyme.services.NotificationListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private Switch notifyModeSwitch, assistSwitch, whatsappSwitch, fbMessengerSwitch;
    private SharedPreferences.Editor editor;
    private boolean fromSettings = false, fromFbMsg = false, fromWa = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fromSettings = true;
        if (requestCode == 0) {
            if (isNotificationServiceEnabled()) {
                assistSwitch.setChecked(true);
                if (fromWa) {
                    fbMessengerSwitch.setChecked(fbMessengerSwitch.isChecked());
                    editor.putBoolean("is_fb_msg_on", fbMessengerSwitch.isChecked());
                } else {
                    fbMessengerSwitch.setChecked(true);
                    editor.putBoolean("is_fb_msg_on", true);
                }
                if (fromFbMsg) {
                    whatsappSwitch.setChecked(whatsappSwitch.isChecked());
                    editor.putBoolean("is_whatsapp_on", whatsappSwitch.isChecked());
                } else {
                    whatsappSwitch.setChecked(true);
                    editor.putBoolean("is_whatsapp_on", true);
                }
                editor.putBoolean("is_assist_on", true);
                editor.apply();
                Intent intent = new Intent(NotificationListener.ENABLE_ASSIST);
                sendBroadcast(intent);
            } else {
                assistSwitch.setChecked(false);
                if (fromWa) {
                    fbMessengerSwitch.setChecked(fbMessengerSwitch.isChecked());
                    editor.putBoolean("is_fb_msg_on", fbMessengerSwitch.isChecked());
                } else {
                    fbMessengerSwitch.setChecked(false);
                    editor.putBoolean("is_fb_msg_on", false);
                }
                if (fromFbMsg) {
                    whatsappSwitch.setChecked(whatsappSwitch.isChecked());
                    editor.putBoolean("is_whatsapp_on", whatsappSwitch.isChecked());
                } else {
                    whatsappSwitch.setChecked(false);
                    editor.putBoolean("is_whatsapp_on", false);
                }
                editor.putBoolean("is_assist_on", false);
                editor.apply();
            }
        } else if (requestCode == 1) {
            if (isNotificationServiceEnabled()) {
                whatsappSwitch.setChecked(true);
                if (!assistSwitch.isChecked()) {
                    assistSwitch.setChecked(true);
                }
                editor.putBoolean("is_assist_on", true);
                editor.putBoolean("is_whatsapp_on", true);
                editor.apply();
                Intent intent = new Intent(NotificationListener.ENABLE_WA);
                intent.putExtra("whatsapp", true);
                sendBroadcast(intent);
            } else {
                whatsappSwitch.setChecked(false);
                editor.putBoolean("is_whatsapp_on", false);
                editor.apply();
            }
        } else if (requestCode == 2) {
            if (isNotificationServiceEnabled()) {
                fbMessengerSwitch.setChecked(true);
                if (!assistSwitch.isChecked()) {
                    assistSwitch.setChecked(true);
                }
                editor.putBoolean("is_assist_on", true);
                editor.putBoolean("is_fb_msg_on", true);
                editor.apply();
                Intent intent = new Intent(NotificationListener.ENABLE_FB_MSG);
                intent.putExtra("fb_msg", true);
                sendBroadcast(intent);
            } else {
                fbMessengerSwitch.setChecked(false);
                editor.putBoolean("is_fb_msg_on", false);
                editor.apply();
            }
        }

        if (!whatsappSwitch.isChecked() && !fbMessengerSwitch.isChecked())
            assistSwitch.setChecked(false);
        fromSettings = false;
        fromWa = false;
        fromFbMsg = false;
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
        whatsappSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!isNotificationServiceEnabled()) {
                        startActivityForResult(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS), 1);
                    } else {
                        editor.putBoolean("is_whatsapp_on", true);
                        editor.apply();
                        Intent intent = new Intent(NotificationListener.ENABLE_WA);
                        intent.putExtra("whatsapp", true);
                        sendBroadcast(intent);
                    }
                } else {
                    if (!fbMessengerSwitch.isChecked()) {
                        assistSwitch.setChecked(false);
                        fromWa = true;
                    } else {
                        editor.putBoolean("is_whatsapp_on", false);
                        editor.apply();
                        Intent intent = new Intent(NotificationListener.ENABLE_WA);
                        intent.putExtra("whatsapp", false);
                        sendBroadcast(intent);
                    }
                }
            }
        });

        fbMessengerSwitch = (Switch) findViewById(R.id.fb_msg_switch);
        if (sharedPreferences.getBoolean("is_fb_msg_on", false)) {
            fbMessengerSwitch.setChecked(true);
        } else {
            fbMessengerSwitch.setChecked(false);
        }

        fbMessengerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!isNotificationServiceEnabled()) {
                        startActivityForResult(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS), 2);
                    } else {
                        editor.putBoolean("is_fb_msg_on", true);
                        editor.apply();
                        Intent intent = new Intent(NotificationListener.ENABLE_FB_MSG);
                        intent.putExtra("fb_msg", true);
                        sendBroadcast(intent);
                    }
                } else {
                    if (!whatsappSwitch.isChecked()) {
                        assistSwitch.setChecked(false);
                        fromFbMsg = true;
                    } else {
                        editor.putBoolean("is_fb_msg_on", false);
                        editor.apply();
                        Intent intent = new Intent(NotificationListener.ENABLE_FB_MSG);
                        intent.putExtra("fb_msg", false);
                        sendBroadcast(intent);
                    }
                }
            }
        });

        Button whatsAppCustomize = (Button) findViewById(R.id.customize_whatsapp);
        whatsAppCustomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CustomizationActivity.class);
                intent.putExtra("package", "WhatsApp");
                startActivity(intent);
            }
        });

        Button fbMsgCustomize = (Button) findViewById(R.id.customize_fb_msg);
        fbMsgCustomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CustomizationActivity.class);
                intent.putExtra("package", "FBMessenger");
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
