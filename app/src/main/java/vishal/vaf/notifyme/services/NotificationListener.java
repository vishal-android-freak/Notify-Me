package vishal.vaf.notifyme.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = NotificationListener.class.getSimpleName();
    private List<String> list = new ArrayList<>();

    private boolean isWhatsAppEnabled = false;

    public NotificationListener() {

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        handleNotifications(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "connected");
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "disconnected");
        list.clear();
    }

    private void handleNotifications(StatusBarNotification statusBarNotification) {

        if (!statusBarNotification.getPackageName().equals("com.whatsapp"))
            return;

        Bundle bundle = statusBarNotification.getNotification().extras;
        Notification.WearableExtender extender = new Notification.WearableExtender(statusBarNotification.getNotification());

        RemoteInput[] remoteInputs = null;
        String senderName = "";
        PendingIntent pendingIntent = null;

        for (String key : bundle.keySet()) {
            if (key.equals("android.title")) {
                if (!list.contains(bundle.getString(key))) {
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
        reply(remoteInputs, pendingIntent, bundle, senderName);
    }

    private void reply(RemoteInput[] remoteInputs, PendingIntent pendingIntent, Bundle bundle, String senderName) {

        if (remoteInputs == null || pendingIntent == null)
            return;

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        for (RemoteInput remoteInput : remoteInputs) {
            bundle.putCharSequence(remoteInput.getResultKey(), "Hey, this is Vishal's AssistBot. My master is sleeping right now. Leave your message here and he will get back to you ASAP!. Apologies for any inconvenience caused.\n\nHave a great day! :)");
        }
        RemoteInput.addResultsToIntent(remoteInputs, localIntent, bundle);
        try {
            pendingIntent.send(this, 0, localIntent);
            list.add(senderName);
        } catch (PendingIntent.CanceledException e) {
            Log.e("", "replyToLastNotification error: " + e.getLocalizedMessage());
        }
    }
}
