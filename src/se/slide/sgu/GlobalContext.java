package se.slide.sgu;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

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
    
    public void savePreference(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value);
    }
    
    public void savePreference(String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
    }
    
    public void savePreference(String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).commit();
    }
    
    public boolean getPreferenceBoolean(String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
    }
    
    public int getPreferenceInt(String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue);
    }
    
    public String getPreferenceString(String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Notification buildNotification(String ticker, String title, String text) {
        Intent i = new Intent(context, StartActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        
        Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_action_planet);
        
        builder.setTicker(ticker);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pi);
        builder.setAutoCancel(true);
        
        Notification notification = null; 
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        }
        else {
            notification = builder.getNotification();
        }
        
        notification.flags |= Notification.FLAG_NO_CLEAR;
        
        return notification;
    }
}
