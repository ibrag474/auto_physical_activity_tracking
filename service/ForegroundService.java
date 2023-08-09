package com.ibrag474.tracker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ibrag474.tracker.R;
import com.ibrag474.tracker.tracking.activity.ActivityTracker;
import com.ibrag474.tracker.tracking.step.StepTracker;

public class ForegroundService extends Service {
    private static final String CHANNEL_ID = "TrackerForegroundServiceChannel";
    public static final int NOTIFICATION_ID = 234565;

    private NotificationCompat.Builder notificationBuilder;

    private StepTracker stepTracker;
    private ActivityTracker activityTracker;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());

        //starting StepTracker here
        stepTracker = new StepTracker(getApplicationContext());
        stepTracker.setNotificationInstance(notificationBuilder);

        //starting ActivityTracker here
        activityTracker = new ActivityTracker(getApplicationContext());
        activityTracker.registerForActivityTransitions();

        // 2
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityTracker.deregisterForActivityTransitions();
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Foreground Service")
                        .setContentText("Steps: 0; Run: 0 km; Cycled: 0 km.")
                        .setOnlyAlertOnce(true)
                        .setSmallIcon(R.mipmap.ic_launcher);
        return notificationBuilder.build();
    }
}
