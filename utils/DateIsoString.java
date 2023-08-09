package com.ibrag474.tracker.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateIsoString {

    public static String getCurrentDateISOString() {
        Calendar calendar = Calendar.getInstance();
        // Reset minutes, seconds, and milliseconds to 0
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Create a SimpleDateFormat for ISO date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());

        // Format the current date as an ISO string
        return sdf.format(calendar.getTime());
    }

    public static String getMinusHourDateISOString() {
        // Get the current date and time
        Calendar calendar = Calendar.getInstance();

        // Subtract one hour from the current date
        calendar.add(Calendar.HOUR_OF_DAY, -1);

        // Reset minutes, seconds, and milliseconds to 0
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Create a SimpleDateFormat for ISO date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());

        // Format the current date as an ISO string
        return sdf.format(calendar.getTime());
    }

}
