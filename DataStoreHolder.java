package com.ibrag474.tracker;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DataStoreHolder {
    private static DataStoreHolder instance;
    private final RxDataStore<Preferences> stepDataStore;
    private final RxDataStore<Preferences> distanceDataStore;
    private final Executor dataStoreExecutor = Executors.newSingleThreadExecutor();

    private DataStoreHolder(Context context) {
        stepDataStore = new RxPreferenceDataStoreBuilder(context, "steps").build();
        distanceDataStore = new RxPreferenceDataStoreBuilder(context, "distance").build();
    }

    public static synchronized DataStoreHolder getInstance(Context context) {
        if (instance == null) {
            instance = new DataStoreHolder(context);
        }
        return instance;
    }

    public RxDataStore<Preferences> getStepsDataStore() {
        return stepDataStore;
    }

    public RxDataStore<Preferences> getDistanceDataStore() {
        return distanceDataStore;
    }

    public Executor getDataStoreExecutor() {
        return dataStoreExecutor;
    }
}
