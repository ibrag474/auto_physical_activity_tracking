package com.ibrag474.tracker.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.rxjava3.RxDataStore;

import com.ibrag474.tracker.DataStoreHolder;
import com.ibrag474.tracker.tracking.activity.DistanceTracker;
import com.ibrag474.tracker.tracking.step.StepTracker;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ForegroundServiceModule extends ReactContextBaseJavaModule {

    ForegroundServiceModule(ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return "ForegroundServiceModule";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @ReactMethod
    public void startForegroundService() {
        Log.d(getName(), "startForegroundService: called");
        Intent serviceIntent =
                new Intent(getReactApplicationContext(), ForegroundService.class);
        getReactApplicationContext().startForegroundService(serviceIntent);
    }

    @SuppressLint("CheckResult")
    @ReactMethod
    public void getHourlySteps(String startDate, Promise promise) {
        RxDataStore<Preferences> dataStore
                = DataStoreHolder.getInstance(getReactApplicationContext()).getStepsDataStore();
        Single<String> stepsSingle = dataStore.data().firstOrError().map(
                        preferences -> preferences.get(StepTracker.STEP_COUNTER)
                )
                .subscribeOn(Schedulers.io());
        stepsSingle.subscribe(
                stepsObjectString -> {
                    JSONObject jsonObject = new JSONObject(stepsObjectString);
                    // Convert the filterDate string to SimpleDateFormat
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    long filterTimeMillis = sdf.parse(startDate).getTime();

                    // Create a new JSONObject to hold the filtered result
                    JSONObject filteredJson = new JSONObject();

                    // Iterate through the JSON objects and filter by date
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        long currentObjectTimeMillis = sdf.parse(key).getTime();

                        // Compare the date of the current object with the filterDate
                        if (currentObjectTimeMillis == filterTimeMillis) {
                            JSONObject filteredObject = jsonObject.getJSONObject(key);
                            filteredObject.remove("previousSteps");
                            filteredJson.put(key, filteredObject);
                        }
                    }

                    promise.resolve(filteredJson.toString());
                },
                throwable -> {
                    promise.reject(throwable);
                }
        );
    }

    @SuppressLint("CheckResult")
    @ReactMethod
    public void getHourlyRunningCyclingMeters(String startDate, Promise promise) {
        RxDataStore<Preferences> dataStore
                = DataStoreHolder.getInstance(getReactApplicationContext()).getDistanceDataStore();
        Single<String> distanceSingle = dataStore.data().firstOrError().map(
                        preferences -> preferences.get(DistanceTracker.DISTANCE_COUNTER)
                )
                .subscribeOn(Schedulers.io());
        distanceSingle.subscribe(
                distanceObjectString -> {
                    JSONObject jsonObject = new JSONObject(distanceObjectString);
                    // Convert the filterDate string to SimpleDateFormat
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    long filterTimeMillis = sdf.parse(startDate).getTime();

                    // Create a new JSONObject to hold the filtered result
                    JSONObject filteredJson = new JSONObject();

                    // Iterate through the JSON objects and filter by date
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        long currentObjectTimeMillis = sdf.parse(key).getTime();

                        // Compare the date of the current object with the filterDate
                        if (currentObjectTimeMillis == filterTimeMillis) {
                            JSONObject filteredObject = jsonObject.getJSONObject(key);
                            filteredJson.put(key, filteredObject);
                        }
                    }

                    promise.resolve(filteredJson.toString());
                },
                throwable -> {
                    promise.reject(throwable);
                }
        );
    }
}
