package com.ibrag474.tracker.tracking.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityTransitionReceiver extends BroadcastReceiver {

    private static final String TAG = ActivityTransitionReceiver.class.getSimpleName();

    private DistanceTracker distanceTracker = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive()" + intent);

        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                // chronological sequence of events....

                String info = "Transition: " + toActivityString(event.getActivityType()) +
                        " (" + toTransitionType(event.getTransitionType()) + ")" + "   " +
                        new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());

                Log.d(TAG, info);

                if (event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                    if (distanceTracker != null) {
                        if (event.getActivityType() == DetectedActivity.RUNNING)
                            distanceTracker.startTracking(DistanceTracker.RUNNING);
                        else if (event.getActivityType() == DetectedActivity.ON_BICYCLE)
                            distanceTracker.startTracking(DistanceTracker.CYCLING);
                    }
                } else {
                    if (distanceTracker != null) {
                        distanceTracker.stopTracking();
                    }
                }
            }
        }

    }

    public void setDistanceTracker(DistanceTracker distanceTracker) {
        this.distanceTracker = distanceTracker;
    }

    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            default:
                return "UNKNOWN";
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }
}
