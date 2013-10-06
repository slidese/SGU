package se.slide.sgu;

import android.content.Context;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

// https://github.com/augusto/android-sandbox/blob/master/src/com/augusto/mymediaplayer/common/Formatter.java
public final class Formatter {
    private Formatter() {}
    
    
    private static NumberFormat numberFormat = new DecimalFormat("00");
    
    /**
     * Formats time from milliseconds in the format m:ss
     * 
     * @param timeInMillis time in milliseconds
     * @return
     */
    public static String formatTimeFromMillis(int timeInMillis) {
        int minutes = timeInMillis/60000;
        int seconds = (timeInMillis%60000)/1000;
        
        return minutes + ":" + numberFormat.format(seconds);
    }
    
    /**
     * Formats the given date to user preference date string formatting
     * 
     * @param context
     * @param date
     * @return
     */
    public static String formatDate(Context context, Date date) {
        DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
        return dateFormat.format(date);
    }
}
