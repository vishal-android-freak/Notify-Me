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
 */

package vishal.vaf.notifyme.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import vishal.vaf.notifyme.R;
import vishal.vaf.notifyme.model.NotificationModel;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = NotificationListener.class.getSimpleName();
    private List<String> waList = new ArrayList<>();
    private List<String> fbMsgList = new ArrayList<>();

    public static final String ENABLE_ASSIST = "enable_assist";
    public static final String ENABLE_NOTIFY = "enable_notify";
    public static final String ENABLE_WA = "enable_wa";
    public static final String ENABLE_FB_MSG = "enable_fb_msg";

    public static final String isWhatsAppOn = "is_wa_on";
    public static final String isFbMsgOn = "is_fb_msg_on";
    public static final String isAssistOn = "is_assist_on";
    public static final String isNotifyOn = "is_notify_on";

    private String phoneNumber = "";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private boolean isWhatsAppEnabled = false, isAssistEnabled = false, isNotifyEnabled = false, isFbMsgEnabled = false;

    private EnableToggleReceiver receiver;

    private DatabaseReference reference;

    private HashMap<String, NotificationModel> hashMap = new HashMap<>();

    private AIConfiguration configuration;
    AIDataService aiDataService;

    private RemoteInput[] remoteInputs;
    private PendingIntent pendingIntent;

    private Handler handler;

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
        try {
            if (isAssistEnabled) {
                handleAssistBotNotifications(sbn);
            } else if (isNotifyEnabled) {
                handleNotifyNotifications(sbn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (isNotifyEnabled) {
            if (sbn.getPackageName().equals("com.whatsapp") | sbn.getPackageName().equals("com.facebook.orca")) {
                String id = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE).toLowerCase().replaceAll("\\(.*?\\)", "").replace(" ", "").replace(":","");
                reference.child("remove" + "/id").setValue(id);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "connected");
        handler = new Handler();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        phoneNumber = sharedPreferences.getString("phone", "");
        isAssistEnabled = sharedPreferences.getBoolean(isAssistOn, false);
        isNotifyEnabled = sharedPreferences.getBoolean(isNotifyOn, false);
        isWhatsAppEnabled = sharedPreferences.getBoolean(isWhatsAppOn, false);
        isFbMsgEnabled = sharedPreferences.getBoolean(isFbMsgOn, false);
        if (isNotifyEnabled) {
            connectToFirebase();
        }

        configuration = new AIConfiguration(getString(R.string.client_access_token), AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, configuration);
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "disconnected");
        fbMsgList.clear();
        waList.clear();
        reference.removeEventListener(messagesListener);
    }

    private void handleAssistBotNotifications(StatusBarNotification statusBarNotification) {

        Bundle bundle = statusBarNotification.getNotification().extras;
        Notification.WearableExtender extender = new Notification.WearableExtender(statusBarNotification.getNotification());

        RemoteInput[] remoteInputs = null;
        String senderName = "";
        PendingIntent pendingIntent = null;

        String packageName = statusBarNotification.getPackageName();

        if (packageName.equals("com.whatsapp")) {
            if (!waList.contains(bundle.getString(Notification.EXTRA_TITLE))) {
                senderName = bundle.getString(Notification.EXTRA_TITLE);
                List<Notification.Action> actions = extender.getActions();
                for (Notification.Action act : actions) {
                    if (act != null && act.getRemoteInputs() != null) {
                        remoteInputs = act.getRemoteInputs();
                        pendingIntent = act.actionIntent;
                    }
                }
            } else {
                handleConversationalBot(statusBarNotification);
            }
        } else if (packageName.equals("com.facebook.orca")) {
            if (!fbMsgList.contains(bundle.getString(Notification.EXTRA_TITLE))) {
                senderName = bundle.getString(Notification.EXTRA_TITLE);
                List<Notification.Action> actions = extender.getActions();
                for (Notification.Action act : actions) {
                    if (act != null && act.getRemoteInputs() != null) {
                        remoteInputs = act.getRemoteInputs();
                        pendingIntent = act.actionIntent;
                    }
                }
            } else {
                handleConversationalBot(statusBarNotification);
            }
        }
        if (isWhatsAppEnabled) {
            if (packageName.equals("com.whatsapp"))
                reply(remoteInputs, pendingIntent, bundle, senderName, packageName, null, null);
        }

        if (isFbMsgEnabled) {
            if (packageName.equals("com.facebook.orca"))
                reply(remoteInputs, pendingIntent, bundle, senderName, packageName, null, null);
        }

    }

    private void handleNotifyNotifications(StatusBarNotification statusBarNotification) {
        Bundle bundle = statusBarNotification.getNotification().extras;
        RemoteInput[] remoteInputs = null;
        PendingIntent pendingIntent = null;

        String packageName = statusBarNotification.getPackageName();

        Notification.WearableExtender extender = new Notification.WearableExtender(statusBarNotification.getNotification());
        List<Notification.Action> actions = extender.getActions();
        for (Notification.Action act : actions) {
            if (act != null && act.getRemoteInputs() != null) {
                remoteInputs = act.getRemoteInputs();
                pendingIntent = act.actionIntent;
            }
        }
        if ((actions.size() > 0) && (packageName.equals("com.whatsapp") || packageName.equals("com.facebook.orca"))) {
            try {

                String id = bundle.getString(Notification.EXTRA_TITLE).toLowerCase().replaceAll("\\(.*?\\)", "").replace(" ", "").replace(":","");

                HashMap<String, String> object = new HashMap<>();
                object.put("name", bundle.getString(Notification.EXTRA_TITLE));
                object.put("message", bundle.getString(Notification.EXTRA_TEXT));
                object.put("id", id);
                object.put("app_name", packageName);

                reference.child("app").setValue(object);
                hashMap.put(id, new NotificationModel(bundle, pendingIntent, remoteInputs));

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

    private void reply(RemoteInput[] remoteInputs, PendingIntent pendingIntent, Bundle bundle, String senderName, String packageName, String message, String notifId) {

        if (remoteInputs == null || pendingIntent == null)
            return;

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (packageName.equals("com.whatsapp")) {
            for (RemoteInput remoteInput : remoteInputs) {
                bundle.putCharSequence(remoteInput.getResultKey(), message == null ? ("Hey " + senderName.split(" ")[0] + ", " + sharedPreferences.getString("assist_wa_msg", "")) : message);
            }
        } else if (packageName.equals("com.facebook.orca")) {
            for (RemoteInput remoteInput : remoteInputs) {
                bundle.putCharSequence(remoteInput.getResultKey(), message == null ? ("Hey " + senderName.split(" ")[0] + ", " + sharedPreferences.getString("assist_fb_msg", "")) : message);
            }
        }

        RemoteInput.addResultsToIntent(remoteInputs, localIntent, bundle);
        try {
            pendingIntent.send(this, 0, localIntent);
            if (packageName.equals("com.whatsapp") && !isNotifyEnabled) {
                waList.add(senderName);
            } else if (packageName.equals("com.facebook.orca") && !isNotifyEnabled) {
                fbMsgList.add(senderName);
            }
            if (isNotifyEnabled) {
                hashMap.remove(notifId);
            }
        } catch (PendingIntent.CanceledException e) {
            Log.e("", "replyToLastNotification error: " + e.getLocalizedMessage());
        }
    }

    private class EnableToggleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ENABLE_ASSIST)) {
                isAssistEnabled = intent.getStringExtra(isAssistOn).equals("1");
                editor.putBoolean(isAssistOn, isAssistEnabled);
                Log.d(TAG, "assist " + isAssistEnabled);
            } else if (intent.getAction().equals(ENABLE_NOTIFY)) {
                isNotifyEnabled = intent.getStringExtra(isNotifyOn).equals("1");
                editor.putBoolean(isNotifyOn, isNotifyEnabled);
                Log.d(TAG, "notify " + isNotifyEnabled);
                if (isNotifyEnabled) {
                    connectToFirebase();
                } else {
                    reference.removeEventListener(messagesListener);
                }
            } else if (intent.getAction().equals(ENABLE_WA)) {
                isWhatsAppEnabled = intent.getStringExtra(isWhatsAppOn).equals("1");
                editor.putBoolean(isWhatsAppOn, isWhatsAppEnabled);
                Log.d(TAG, "wa " + isWhatsAppEnabled);
                if (!isWhatsAppEnabled)
                    waList.clear();
            } else if (intent.getAction().equals(ENABLE_FB_MSG)) {
                isFbMsgEnabled = intent.getStringExtra(isFbMsgOn).equals("1");
                editor.putBoolean(isFbMsgOn, isFbMsgEnabled);
                Log.d(TAG, "fbmsg " + isFbMsgEnabled);
                if (!isFbMsgEnabled)
                    fbMsgList.clear();
            }
            editor.apply();
        }
    }

    private void connectToFirebase() {
        reference = FirebaseDatabase.getInstance().getReference("notifyme/" + phoneNumber);
        reference.child("desktop").addValueEventListener(messagesListener);
    }

    private ValueEventListener messagesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            HashMap<String, String> object = (HashMap<String, String>) dataSnapshot.getValue();
            if (object != null) {
                Log.d("data", object.toString());
                String appName = object.get("app_name");
                String senderName = object.get("name");
                String senderMsg = object.get("message");
                String id = object.get("id");

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reference.child("desktop").setValue(null);
                    }
                }, 1000);

                reply(hashMap.get(id).getRemoteInputs(), hashMap.get(id).getPendingIntent(), hashMap.get(id).getBundle(), senderName, appName, senderMsg, id);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void handleConversationalBot(StatusBarNotification statusBarNotification) {
        final Bundle bundle = statusBarNotification.getNotification().extras;

        final String packageName = statusBarNotification.getPackageName();

        Notification.WearableExtender extender = new Notification.WearableExtender(statusBarNotification.getNotification());
        List<Notification.Action> actions = extender.getActions();
        for (Notification.Action act : actions) {
            if (act != null && act.getRemoteInputs() != null) {
                remoteInputs = act.getRemoteInputs();
                pendingIntent = act.actionIntent;
            }
        }

        String queryText = bundle.getString(Notification.EXTRA_TEXT);
        final String senderName = bundle.getString(Notification.EXTRA_TITLE);

        if (!queryText.contains("new messages") || !queryText.contains("new message")) {
            new AsyncTask<AIRequest, Void, AIResponse>() {

                protected AIResponse doInBackground(AIRequest... requests) {
                    final AIRequest request = requests[0];
                    try {
                        return aiDataService.request(request);
                    } catch (AIServiceException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(AIResponse aiResponse) {
                    if (aiResponse != null) {
                        reply(remoteInputs, pendingIntent, bundle, senderName, packageName, aiResponse.getResult().getFulfillment().getSpeech(), senderName);
                    }
                }

            }.execute(new AIRequest(queryText));
        }
    }
}
