package com.guy.class23b_ands_2;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service {

    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";

    public static int NOTIFICATION_ID = 167;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.guy.class23b_ands_2.CHANNEL_ID_FOREGROUND2";
    public static String MAIN_ACTION = "com.guy.class23b_ands_2.locationservice.action.main2";
    private NotificationCompat.Builder notificationBuilder;
    private boolean isServiceRunningRightNow = false;

    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            stopForeground(true);
            return START_NOT_STICKY;
        }


        String action = intent.getAction();
        Log.d("pttt", "onStartCommand A");
        if (action.equals(START_FOREGROUND_SERVICE)) {
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }
            Log.d("pttt", "onStartCommand B");


            isServiceRunningRightNow = true;
            notifyToUserForForegroundService();
            startRecording();

        } else if (action.equals(STOP_FOREGROUND_SERVICE)) {
            stopRecording();
            stopForeground(true);
            stopSelf();

            isServiceRunningRightNow = false;
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }


    private MCT5.CycleTicker tickerCycle = new MCT5.CycleTicker() {
        @Override
        public void periodic(int repeatsRemaining) {
            ticker();
        }

        @Override
        public void done() {}
    };

    private void ticker() {
        Log.d("pttt", Thread.currentThread().getName() + " - ticker: " + System.currentTimeMillis());
    }

    private void startRecording() {
        // Keep CPU working
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PassiveApp:tag");
        wakeLock.acquire();


        MCT5.get().cycle(tickerCycle, MCT5.CONTINUOUSLY_REPEATS, 5000);

        // Run GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(1000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateDistanceMeters(1.0f)
                    .setMinUpdateIntervalMillis(1000)
                    .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(1))
                            .build();

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult.getLastLocation() != null) {
                Log.d("pttt", ":getLastLocation");
                String str = new SimpleDateFormat("hh:mm:ss").format(System.currentTimeMillis());
                str = locationResult.getLastLocation().getLatitude() + "\n" + locationResult.getLastLocation().getLongitude() + " - " + str;
                updateNotification(str);

            } else {
                Log.d("pttt", "Location information isn't available.");
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    private void stopRecording() {
        // Release CPU Holding
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }

        MCT5.get().remove(tickerCycle);

        // Stop GPS
        if (fusedLocationProviderClient != null) {
            Task<Void> task = fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("pttt", "stop Location Callback removed.");
                        stopSelf();
                    } else {
                        Log.d("pttt", "stop Failed to remove Location Callback.");
                    }
                }
            });
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // // // // // // // // // // // // // // // // Notification  // // // // // // // // // // // // // // //

    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, Activity_Panel.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder
                .setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_running)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle("App in progress")
                .setContentText("Content")
        ;

        Notification notification = notificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);

        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Running map channel";
        String description = notifications_channel_description;
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    private void updateNotification(String content) {
        notificationBuilder.setContentText(content);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}