package com.ibrag474.tracker.tracking.step;

import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import com.ibrag474.tracker.DataStoreHolder;
import com.ibrag474.tracker.service.ForegroundService;
import com.ibrag474.tracker.utils.DateIsoString;

import org.json.JSONObject;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StepTracker implements SensorEventListener {

    public static final Preferences.Key<String> STEP_COUNTER = PreferencesKeys.stringKey("hourly_step_counter");

    private Context context;
    private NotificationCompat.Builder notificationBuilder = null;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor = null;

    private int stepCount = 0;

    private RxDataStore<Preferences> dataStore;

    public StepTracker(Context context) {
        this.context = context;
        dataStore = DataStoreHolder.getInstance(context).getStepsDataStore();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        start();
    }

    private void start() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void setNotificationInstance(
            NotificationCompat.Builder builder
    ) {
        notificationBuilder = builder;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) sensorEvent.values[0];

            //store hourly step count in json array in a String in a DataStore
            updateStepCount(stepCount);

            if (notificationBuilder != null) {
                notificationBuilder.setContentText(
                        "Steps: " + stepCount + ";"
                                + " Run: 0 km; "
                                + "Cycled: 0 km."
                );
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

                    notificationManager.notify(ForegroundService.NOTIFICATION_ID, notificationBuilder.build());
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public Single<String> getStepCounterValue(Context context) {
        return dataStore.data().firstOrError().map(preferences -> preferences.get(STEP_COUNTER))
                .subscribeOn(Schedulers.io());
    }

    private void updateStepCount(int steps) {
        Single<Preferences> updateResult = dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();

            String stringJson = prefsIn.get(STEP_COUNTER);
            if (stringJson == null) stringJson = "{}";

            JSONObject hourlyStepsObject = new JSONObject(stringJson);

            String isoString = DateIsoString.getCurrentDateISOString();
            try {
                if (hourlyStepsObject.has(isoString)) {
                        JSONObject hour = hourlyStepsObject.getJSONObject(isoString);
                        int stepDelta = steps - hour.getInt("previousSteps");
                        hour.put("steps", hour.getInt("steps") + stepDelta);
                        hour.put("previousSteps", steps);
                        hourlyStepsObject.put(isoString, hour);
                        Log.d("StepTracker", hourlyStepsObject.toString());
                } else {
                    JSONObject newHour = new JSONObject();
                    newHour.put("steps", 0);
                    newHour.put("previousSteps", steps);
                    hourlyStepsObject.put(isoString, newHour);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mutablePreferences.set(STEP_COUNTER, hourlyStepsObject.toString());
            return Single.just(mutablePreferences);
        });
        /*updateResult.subscribe(
                a -> { Log.d("StepTracker" , "success"); },
                t -> { Log.e("StepTracker", t.getMessage()); }
        );*/
    }

    public int getStepCount() {
        return stepCount;
    }
}
