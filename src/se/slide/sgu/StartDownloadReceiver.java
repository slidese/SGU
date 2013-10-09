package se.slide.sgu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StartDownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean autoDownload = sharedPreferences.getBoolean("auto_download", false);
        if (autoDownload) {
            context.startService(new Intent(context, DownloaderService.class));    
        }
        
    }

}
