package com.ibrag474.tracker.tracking.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.ibrag474.tracker.BuildConfig;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class ActivityTracker {

    private Context context;
    public final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";
    private PendingIntent pendingIntent;

    private DistanceTracker distanceTracker;
    private ActivityTransitionReceiver activityTransitionReceiver;

    public ActivityTracker(Context context) {
        this.context = context;
    }

    public void registerForActivityTransitions() {
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_MUTABLE
                    );
        } else {
            pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            0,
                            intent,
                            0
                    );
        }

        activityTransitionReceiver = new ActivityTransitionReceiver();
        context.registerReceiver(
                activityTransitionReceiver,
                new IntentFilter(TRANSITIONS_RECEIVER_ACTION));

        Task<Void> task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(request, pendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        distanceTracker = new DistanceTracker(context);
                        activityTransitionReceiver.setDistanceTracker(distanceTracker);
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                }
        );

    }

    public void deregisterForActivityTransitions() {
        distanceTracker.stopTracking();
        Task<Void> task = ActivityRecognition.getClient(context)
                .removeActivityTransitionUpdates(pendingIntent);

    }

}
