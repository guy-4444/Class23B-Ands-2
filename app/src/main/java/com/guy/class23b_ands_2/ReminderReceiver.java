package com.guy.class23b_ands_2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "com.guy.class23b_ands_2.CrashAlert";
    private static final int NOTIFICATION_ID = 356;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("pttt", "ReminderReceiver onReceive");

        if (checkToActivateReminder(context)) {
            // context.startActivity(new Intent(context, Activity_Panel.class));
            // actionToService(context, LocationService.START_FOREGROUND_SERVICE);
            createNotification(context);
        }
    }

    private boolean checkToActivateReminder(Context context) {
        Log.d("pttt", "checkToActivateReminder A");
        context = context.getApplicationContext();

        // user press start
        boolean needToRun = MyDB.isNeedToRun(context);
        boolean isServiceRunning = LocationService.isMyServiceRunning(context);
        if (needToRun &&  !isServiceRunning) {
            return true;
        }
        return false;
    }

    private void actionToService(Context context, String action) {
        Intent startIntent = new Intent(context, LocationService.class);
        startIntent.setAction(action);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent);
                // or
//                ContextCompat.startForegroundService(context, startIntent);
            } else {
                context.startService(startIntent);
            }
    }

    public static void cancelNotification(Context mContext) {
        mContext = mContext.getApplicationContext();
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    void createNotification(Context mContext) {
        mContext = mContext.getApplicationContext();
        Log.d("pttt", "createNotification");

        Intent intent = new Intent(mContext, Activity_Panel.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                mContext,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);


        NotificationCompat.Builder mBuilder;


        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "HOME_NOTIFICATION_CHANNEL", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
            assert mNotificationManager != null;

            mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
            //mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        } else {
            mBuilder = new NotificationCompat.Builder(mContext);
        }

        mBuilder.setContentTitle("Unexpected test stop")
                .setContentText("We realized that the test was stopped without an initiated stop. We turn it back on")
                .setSmallIcon(R.drawable.ic_stop)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        assert mNotificationManager != null;
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
