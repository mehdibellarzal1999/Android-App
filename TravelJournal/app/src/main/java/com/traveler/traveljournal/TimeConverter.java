package com.traveler.traveljournal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeConverter {
    public static String convertTimeToString(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date resultDate = new Date(timeInMillis);
        return sdf.format(resultDate);
    }
}
