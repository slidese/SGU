package se.slide.sgu;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public enum GlobalContext {
    INSTANCE;
    
    private Context context;
    
    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    public void sendExceptionToGoogleAnalytics(String threadName, Throwable t, boolean fatal) {
        // https://developers.google.com/analytics/devguides/collection/android/v3/exceptions
        EasyTracker easyTracker = EasyTracker.getInstance(context);
        easyTracker.send(MapBuilder.createException(new StandardExceptionParser(context, null).getDescription(threadName, t), fatal).build());
    }
    
    public String formatDate(Date date) {
        DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
        return dateFormat.format(date);
    }
    
    public void setScheduling() {
        Date now = new Date();
        
        // Set the time to download to 18:00
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, StartDownloadReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        mgr.cancel(pi);

        // 1 * 24 * 60 * 60 * 1000 = repeat this every day
        mgr.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), 1 * 24 * 60 * 60 * 1000, pi);
    }
}
