package se.slide.sgu;

import android.content.Context;
import android.os.Environment;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import java.io.File;

public class Utils {

    public static final String DIR_SGU = Environment.DIRECTORY_DOWNLOADS + "/sgu/";
    
    public static String formatFilename(String title) {
        int dashIndex = title.indexOf(" -");
        String name = title.substring(0, dashIndex);
        name = name.replace("#", "");
        
        return name + ".mp3";
    }
    
    public static File getFilepath(String filename) {
        return Environment.getExternalStoragePublicDirectory(DIR_SGU + filename);
    }
    
    public static void sendExceptionToGoogleAnalytics(Context ctx, String threadName, Throwable t, boolean fatal) {
        // https://developers.google.com/analytics/devguides/collection/android/v3/exceptions
        EasyTracker easyTracker = EasyTracker.getInstance(ctx);
        easyTracker.send(MapBuilder.createException(new StandardExceptionParser(ctx, null).getDescription(threadName, t), fatal).build());
    }
}
