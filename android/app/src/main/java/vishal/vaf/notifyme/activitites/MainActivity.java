/**
 * MIT License
 *
 * Copyright (c) 2017 Vishal Dubey
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package vishal.vaf.notifyme.activitites;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import vishal.vaf.notifyme.R;
import vishal.vaf.notifyme.services.NotificationListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private Switch notifyModeSwitch, assistSwitch, whatsappSwitch, fbMessengerSwitch;

    private boolean fromWhatsapp = true, fromFbMsg = true;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (!isNotificationServiceEnabled()) {
                activateNotificationListener();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NotoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.getBoolean("is_logged_in", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {

            setContentView(R.layout.activity_main);


            if (!isNotificationServiceEnabled()) {
                activateNotificationListener();
            }

            notifyModeSwitch = (Switch) findViewById(R.id.enable_notify_switch);
            notifyModeSwitch.setChecked(sharedPreferences.getBoolean(NotificationListener.isNotifyOn, false));
            notifyModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        Intent intent = new Intent(NotificationListener.ENABLE_NOTIFY);
                        intent.putExtra(NotificationListener.isNotifyOn, "1");
                        sendBroadcast(intent);
                        assistSwitch.setChecked(false);
                        whatsappSwitch.setChecked(false);
                        fbMessengerSwitch.setChecked(false);
                    } else {
                        Intent intent = new Intent(NotificationListener.ENABLE_NOTIFY);
                        intent.putExtra(NotificationListener.isNotifyOn, "0");
                        sendBroadcast(intent);
                    }
                }
            });

            assistSwitch = (Switch) findViewById(R.id.enable_assist_switch);
            assistSwitch.setChecked(sharedPreferences.getBoolean(NotificationListener.isAssistOn, false));
            assistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        Intent intent = new Intent(NotificationListener.ENABLE_ASSIST);
                        intent.putExtra(NotificationListener.isAssistOn, "1");
                        sendBroadcast(intent);
                        notifyModeSwitch.setChecked(false);
                        whatsappSwitch.setChecked(fromFbMsg);
                        fbMessengerSwitch.setChecked(fromWhatsapp);
                        fromFbMsg = true;
                        fromWhatsapp = true;
                    } else {
                        Intent intent = new Intent(NotificationListener.ENABLE_ASSIST);
                        intent.putExtra(NotificationListener.isAssistOn, "0");
                        sendBroadcast(intent);
                        whatsappSwitch.setChecked(false);
                        fbMessengerSwitch.setChecked(false);
                    }
                }
            });

            whatsappSwitch = (Switch) findViewById(R.id.whatsapp_switch);
            whatsappSwitch.setChecked(sharedPreferences.getBoolean(NotificationListener.isWhatsAppOn, false));
            whatsappSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        Intent intent = new Intent(NotificationListener.ENABLE_WA);
                        intent.putExtra(NotificationListener.isWhatsAppOn, "1");
                        sendBroadcast(intent);
                        if (!assistSwitch.isChecked()) {
                            fromWhatsapp = false;
                            assistSwitch.setChecked(true);
                        }
                        notifyModeSwitch.setChecked(false);
                    } else {
                        Intent intent = new Intent(NotificationListener.ENABLE_WA);
                        intent.putExtra(NotificationListener.isWhatsAppOn, "0");
                        sendBroadcast(intent);
                        if (!fbMessengerSwitch.isChecked()) {
                            assistSwitch.setChecked(false);
                        }
                    }
                }
            });

            fbMessengerSwitch = (Switch) findViewById(R.id.fb_msg_switch);
            fbMessengerSwitch.setChecked(sharedPreferences.getBoolean(NotificationListener.isFbMsgOn, false));
            fbMessengerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        Intent intent = new Intent(NotificationListener.ENABLE_FB_MSG);
                        intent.putExtra(NotificationListener.isFbMsgOn, "1");
                        sendBroadcast(intent);
                        if (!assistSwitch.isChecked()) {
                            fromFbMsg = false;
                            assistSwitch.setChecked(true);
                        }
                        notifyModeSwitch.setChecked(false);
                    } else {
                        Intent intent = new Intent(NotificationListener.ENABLE_FB_MSG);
                        intent.putExtra(NotificationListener.isFbMsgOn, "0");
                        sendBroadcast(intent);
                        if (!whatsappSwitch.isChecked()) {
                            assistSwitch.setChecked(false);
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
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private boolean isNotificationServiceEnabled() {
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

    private void activateNotificationListener() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(false);
        builder.setMessage("Notification access required for the application to function. Do you want to enable it now?");
        builder.setPositiveButton("Open settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivityForResult(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS), 0);
            }
        });

        builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                Toast.makeText(MainActivity.this, "App cannot function without notification access", Toast.LENGTH_SHORT).show();
            }
        });

        builder.create();
        builder.show();

    }
}
