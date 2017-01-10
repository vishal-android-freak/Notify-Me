package vishal.vaf.notifyme.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = NotificationListener.class.getSimpleName();
    private List<String> waList = new ArrayList<>();
    private List<String> fbMsgList = new ArrayList<>();

    public static final String ENABLE_ASSIST = "enable_assist";
    public static final String ENABLE_NOTIFY = "enable_notify";
    public static final String ENABLE_WA = "enable_wa";
    public static final String ENABLE_FB_MSG = "enable_fb_msg";

    private SharedPreferences sharedPreferences;
    private boolean isWhatsAppEnabled = false, isAssistEnabled = false, isFbMsgEnabled = false;
    private EnableToggleReceiver receiver;

    public NotificationListener() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ENABLE_ASSIST);
        filter.addAction(ENABLE_NOTIFY);
        filter.addAction(ENABLE_WA);
        filter.addAction(ENABLE_FB_MSG);
        receiver = new EnableToggleReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (isAssistEnabled) {
            handleAssistBotNotifications(sbn);
        } else {
            handleNotifyNotifications(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "connected");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "disconnected");
        fbMsgList.clear();
        waList.clear();
    }

    private void handleAssistBotNotifications(StatusBarNotification statusBarNotification) {

        Bundle bundle = statusBarNotification.getNotification().extras;
        Notification.WearableExtender extender = new Notification.WearableExtender(statusBarNotification.getNotification());

        RemoteInput[] remoteInputs = null;
        String senderName = "";
        PendingIntent pendingIntent = null;

        String packageName = statusBarNotification.getPackageName();

        for (String key : bundle.keySet()) {
            if (key.equals("android.title")) {
                if (packageName.equals("com.whatsapp")) {
                    if (!waList.contains(bundle.getString(key))) {
                        senderName = bundle.getString(key);
                        List<Notification.Action> actions = extender.getActions();
                        for (Notification.Action act : actions) {
                            if (act != null && act.getRemoteInputs() != null) {
                                remoteInputs = act.getRemoteInputs();
                                pendingIntent = act.actionIntent;
                            }
                        }
                    }
                } else if (packageName.equals("com.facebook.orca")) {
                    if (!fbMsgList.contains(bundle.getString(key))) {
                        senderName = bundle.getString(key);
                        List<Notification.Action> actions = extender.getActions();
                        for (Notification.Action act : actions) {
                            if (act != null && act.getRemoteInputs() != null) {
                                remoteInputs = act.getRemoteInputs();
                                pendingIntent = act.actionIntent;
                            }
                        }
                    }
                }
            }
        }
        if (isWhatsAppEnabled) {
            if (packageName.equals("com.whatsapp"))
                reply(remoteInputs, pendingIntent, bundle, senderName, packageName);
        }

        if (isFbMsgEnabled) {
            if (packageName.equals("com.facebook.orca"))
                reply(remoteInputs, pendingIntent, bundle, senderName, packageName);
        }

    }

    private void handleNotifyNotifications(StatusBarNotification statusBarNotification) {

    }

    private void reply(RemoteInput[] remoteInputs, PendingIntent pendingIntent, Bundle bundle, String senderName, String packageName) {

        if (remoteInputs == null || pendingIntent == null)
            return;

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (packageName.equals("com.whatsapp")) {
            for (RemoteInput remoteInput : remoteInputs) {
                bundle.putCharSequence(remoteInput.getResultKey(), sharedPreferences.getString("assist_wa_msg", ""));
            }
        } else if (packageName.equals("com.facebook.orca")) {
            for (RemoteInput remoteInput : remoteInputs) {
                bundle.putCharSequence(remoteInput.getResultKey(), sharedPreferences.getString("assist_fb_msg", ""));
            }
        }

        RemoteInput.addResultsToIntent(remoteInputs, localIntent, bundle);
        try {
            pendingIntent.send(this, 0, localIntent);
            if (packageName.equals("com.whatsapp")) {
                waList.add(senderName);
            } else if (packageName.equals("com.facebook.orca")) {
                fbMsgList.add(senderName);
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e("", "replyToLastNotification error: " + e.getLocalizedMessage());
        }
    }

    private class EnableToggleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ENABLE_ASSIST)) {
                isAssistEnabled = true;
                isFbMsgEnabled = true;
                isWhatsAppEnabled = true;
                Log.d(TAG, "whatsapp and messenger enabled");
            } else if (intent.getAction().equals(ENABLE_NOTIFY)) {
                isAssistEnabled = false;
            } else if (intent.getAction().equals(ENABLE_WA)) {
                if (intent.getBooleanExtra("whatsapp", false)) {
                    isWhatsAppEnabled = true;
                    Log.d(TAG, "whatsapp enabled");
                } else {
                    isWhatsAppEnabled = false;
                    Log.d(TAG, "whatsapp disabled");
                }
            } else if (intent.getAction().equals(ENABLE_FB_MSG)) {
                if (intent.getBooleanExtra("fb_msg", false)) {
                    isFbMsgEnabled = true;
                    Log.d(TAG, "fbmsg enabled");
                } else {
                    isFbMsgEnabled = false;
                    Log.d(TAG, "fbmsg disabled");
                }
            }
        }
    }
}
