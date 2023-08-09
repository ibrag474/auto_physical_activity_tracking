package com.ibrag474.tracker.tracking.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import com.ibrag474.tracker.DataStoreHolder;
import com.ibrag474.tracker.utils.DateIsoString;
import com.ibrag474.tracker.utils.DistanceCalculator;

import org.json.JSONObject;

import io.reactivex.rxjava3.core.Single;

public class DistanceTracker {

    private static final String TAG = DistanceTracker.class.getSimpleName();

    public static final String RUNNING = "running";
    public static final String CYCLING = "cycling";

    Context context;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private String activityType;

    public static final Preferences.Key<String> DISTANCE_COUNTER
            = PreferencesKeys.stringKey("hourly_distance_counter");
    private RxDataStore<Preferences> dataStore;

    private double[] prevCoordinates = null;

    public DistanceTracker(Context context) {
        this.context = context;
        this.dataStore = DataStoreHolder.getInstance(context).getDistanceDataStore();
    }

    public void startTracking(String activityType) {
        this.activityType = activityType;
        locationListener = setLocationListener();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                10,
                locationListener
        );

    }

    public void stopTracking() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private LocationListener setLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (prevCoordinates == null) {
                    prevCoordinates = new double[] {
                            location.getLatitude(), location.getLongitude()};
                } else {
                    updateRunningDistanceCount(DistanceCalculator.calculateDistance(
                            prevCoordinates[0],
                            prevCoordinates[1],
                            location.getLatitude(),
                            location.getLongitude()
                    ));
                    prevCoordinates[0] = location.getLatitude();
                    prevCoordinates[1] = location.getLongitude();
                }
            }
        };
    }

    private void updateRunningDistanceCount(int distance) {
        Single<Preferences> updateResult = dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();

            String stringJson = prefsIn.get(DISTANCE_COUNTER);
            if (stringJson == null) stringJson = "{}";

            JSONObject hourlyStepsObject = new JSONObject(stringJson);

            String isoString = DateIsoString.getCurrentDateISOString();
            try {
                if (hourlyStepsObject.has(isoString)) {
                    JSONObject hour = hourlyStepsObject.getJSONObject(isoString);
                    if (activityType.equals(RUNNING)) {
                        int metersRun = hour.getInt("meters_run") + distance;
                        hour.put("meters_run", metersRun);
                    } else if (activityType.equals(CYCLING)) {
                        int metersCycled = hour.getInt("meters_cycled") + distance;
                        hour.put("meters_cycled", metersCycled);
                    }
                    hourlyStepsObject.put(isoString, hour);
                    Log.d("DistanceTracker", hourlyStepsObject.toString());
                } else {
                    JSONObject newHour = new JSONObject();
                    newHour.put("meters_run", 0);
                    newHour.put("meters_cycled", 0);
                    if (activityType.equals(RUNNING))
                        newHour.put("meters_run", distance);
                    else if (activityType.equals(CYCLING))
                        newHour.put("meters_cycled", distance);
                    hourlyStepsObject.put(isoString, newHour);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mutablePreferences.set(DISTANCE_COUNTER, hourlyStepsObject.toString());
            return Single.just(mutablePreferences);
        });
        /*updateResult.subscribe(
                a -> { Log.d("StepTracker" , "success"); },
                t -> { Log.e("StepTracker", t.getMessage()); }
        );*/
    }

}
