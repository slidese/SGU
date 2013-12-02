package se.slide.sgu;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import org.apache.commons.lang3.exception.ExceptionUtils;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum GlobalContext {
    INSTANCE;
    
    private Context                 context;
    private GoogleAnalytics         googleAnalytics;
    private List<Content>           listOfContentAdFree;
    private List<Content>           listOfContentPremium;
    private List<Episode>           listOfEpisodes;
    private Map<String,Episode>     mapOfEpisodes;
    
    public void init(Context context) {
        
        if (this.context == null) {
            this.context = context.getApplicationContext();
            resetContentCache();
        }
        
        googleAnalytics = GoogleAnalytics.getInstance(this.context);
        if (Utils.DEBUG) {
            googleAnalytics.setDryRun(true);
        }
        else {
            googleAnalytics.setDryRun(false);
        }
        
    }
    
    public List<Content> getCachedContent(int mode, String mp3, boolean isPlaying, boolean isPaused) {
        if (mode == ContentFragment.MODE_ADFREE) {
            if (listOfContentAdFree != null) {
                return listOfContentAdFree;
            }
            
            listOfContentAdFree = DatabaseManager.getInstance().getAdFreeContents();
            enrichContentList(listOfContentAdFree, mp3, isPlaying, isPaused);
            
            return listOfContentAdFree;
        }
        else if (mode == ContentFragment.MODE_PREMIUM) {
            if (listOfContentPremium != null) {
                return listOfContentPremium;
            }
            
            listOfContentPremium = DatabaseManager.getInstance().getPremiumContents();
            enrichContentList(listOfContentPremium, mp3, isPlaying, isPaused);
            
            return listOfContentPremium;
        }
        
        return null;
    }
    
    public void enrichContentList(List<Content> listOfContent, String mp3, boolean isPlaying, boolean isPaused) {
        checkEpisodesList();
        
        Map<String, UpdateHolder> updates = gatherMetadata(mp3, isPlaying, isPaused);
        
        for (Content content : listOfContent) {
            Episode episode = mapOfEpisodes.get(content.guid);
            if (episode != null) {
                content.friendlyTitle = episode.title;
                content.image = episode.image;
            }
            
            UpdateHolder update = updates.get(content.mp3);
            if (update != null) {
                content.exists = update.exists;
                content.downloadProgress = update.progress;
                content.downloadStatus = update.status;
                content.isPaused = update.isPaused;
                content.isPlaying = update.isPlaying;
            }
        }
        
    }
    
    /**
     * Given a <List> of <Content> replace the currently playing content with its corresponding copy in the list
     * @param listOfContent
     * @param currentContent
     */
    public void replaceCurrentlyPLayingContent(List<Content> listOfContent, Content currentContent) {
        if (currentContent != null && !listOfContent.contains(currentContent)) {
            for (int i = 0; i < listOfContent.size(); i++) {
                
                if (((Content)listOfContent.get(i)).mp3.equals(currentContent.mp3)) {
                    listOfContent.set(i, currentContent);
                    break;
                }
            }
        }
    }
    
    private Map<String, UpdateHolder> gatherMetadata(String mp3, boolean isPlaying, boolean isPaused) {
        
        Map<String, UpdateHolder> map = new HashMap<String, UpdateHolder>();
        
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
        try {
            Cursor cursor = ContentDownloadManager.INSTANCE.query(q);
            
            while (cursor.moveToNext()) {
                //long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                
                float progress = (float)downloaded/(float)total;
                
                UpdateHolder holder = new UpdateHolder();
                holder.progress = progress;
                holder.status = status;
                
                map.put(uri, holder);
            }
            
            cursor.close();
            
            List<Content> listOfContent = DatabaseManager.getInstance().getAllContents();
            for (Content content : listOfContent) {
                File file = Utils.getFilepath(content.getFilename());
                
                UpdateHolder holder = map.get(content.mp3);
                if (holder == null) {
                    holder = new UpdateHolder();
                }

                if (mp3 != null && content.mp3.equals(mp3)) {
                    holder.isPlaying = isPlaying;
                    holder.isPaused = isPaused;
                }
                else {
                    holder.isPlaying = false;
                    holder.isPaused = false;
                }
                
                holder.exists = file.exists();
                holder.played = content.played;
                //holder.elapsed = content.elapsed;
                //holder.duration = content.duration;
                map.put(content.mp3, holder);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return map;
    }
    
    public void resetContentCache() {
        listOfContentAdFree = null;
        listOfContentPremium = null;
        listOfEpisodes = null;
    }
    
    public void checkEpisodesList() {
        if (listOfEpisodes == null)
            listOfEpisodes = DatabaseManager.getInstance().getAllEpisodes();
        
        mapOfEpisodes = new HashMap<String,Episode>();
        for (Episode episode : listOfEpisodes)
            mapOfEpisodes.put(episode.guid, episode);    
    }
    
    public class UpdateHolder {
        public String mp3;
        public int status;
        public boolean played;
        public float progress;
        public boolean exists = false;
        public boolean isPlaying = false;
        public boolean isPaused = false;
        //public int elapsed;
        //public int duration;
    }

    public void sendExceptionToGoogleAnalytics(String message, String threadName, Throwable t, boolean fatal) {
        // https://developers.google.com/analytics/devguides/collection/android/v3/exceptions
        
        String stacktrace = ExceptionUtils.getStackTrace(t);
        
        EasyTracker easyTracker = EasyTracker.getInstance(context);
        easyTracker.send(MapBuilder
                .createException(message + ": " + new StandardExceptionParser(context, null)           // Context and optional collection of package names
                                                                                      // to be used in reporting the exception.
                                 .getDescription(Thread.currentThread().getName(),    // The name of the thread on which the exception occurred.
                                                 t),                                  // The exception.
                                 false)                                               // False indicates a fatal exception
                .build()
            );
        //easyTracker.send(MapBuilder.createException(message + ", " + new StandardExceptionParser(context, null).getDescription(threadName, t) + ", Exception: " + stacktrace, fatal).build());
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
        builder.setSmallIcon(R.drawable.ic_actionbar_logo);
        
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
        
        return notification;
    }
}
