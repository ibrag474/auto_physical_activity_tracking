# Android Foreground Service for Automatic Physical Activity Tracking 

The code was originally written to use as React Native module, so it comes with Module and Package.

This service is able to automatically detect when user is running or is on bicycle. When one of previously activities is detected, service requests location updates from Android Location Manager, calculates and stores distance in the AndroidX DataStore. Distance are being calculated for each hour and are stored in form of stringified JSON object.
JSON object example:
```
{
  "2023-08-05T14:48:00.000Z": {
    "meters_run": 5413,
    "meters_cycled": 8406
  },
  ...
}
```
This service also tracks steps, Android SensorManager Sensor.TYPE_STEP_COUNTER is used for this. It also calculates steps for each hour and are stored in form of stringified JSON object.
JSON object example:
```
{
  "2023-08-05T14:48:00.000Z": {
    "steps": 5413
  },
  ...
}
```
Service can be started by calling `startForegroundService()`, data can be retrieved by calling `getHourlySteps()` and `getHourlyRunningCyclingMeters()` which are located in `ForegroundServiceModule`.

This code can be used as React Native Module or can be easily transformed to in a native Android project.
