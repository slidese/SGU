package se.slide.sgu;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import se.slide.sgu.ContentFragment.UpdateHolder;
import se.slide.sgu.model.Content;

import java.io.File;

public class Utils {
    
    private static final String TAG = "Utils";

    public static final boolean DEBUG = false;
    public static final String DIR_SGU =                Environment.DIRECTORY_DOWNLOADS + "/sgu/";
    public static final String HTTP_PODCAST_IMAGES =    "http://www.theskepticsguide.org/images/podcast_images/";
    
    public static String formatFilename(String title) {
        int dashIndex = title.indexOf(" -");
        String name = title.substring(0, dashIndex);
        name = name.replace("#", "");
        
        return name + ".mp3";
    }
    
    public static File getBaseStorageDirectory() {
        return Environment.getExternalStoragePublicDirectory(DIR_SGU);
    }
    
    public static File getFilepath(String filename) {
        return Environment.getExternalStoragePublicDirectory(DIR_SGU + filename);
    }
    
    public static void cleanDownloadDirectory() {
        File downloadDir = getBaseStorageDirectory();
        
        for (File file: downloadDir.listFiles())
            file.delete();
    }
    
    public static void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
    
    public static int[] convertToIntArray(String delimitedString) {
        if (delimitedString == null)
            return null;
        
        String[] strArray = delimitedString.split(";");
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.valueOf(strArray[i]);
        }
        
        return intArray;
    }
    
    /*
    public static int calculatePercent(int total, int part) {
        if (total < 1 || part < 1)
            return 0;
        
        if (total == part)
            return 100;
        
        double d = (double)part / (double)total * 100;
        long percent = Math.round(d);
        
        return (int)percent;
    }
    */
    
    public static void updateView(Resources resources, UpdateHolder update, ContentAdapter.ViewHolder holder) {
        if (update != null) {
            if (!update.exists && update.status != DownloadManager.STATUS_PENDING && update.status != DownloadManager.STATUS_RUNNING) {
                Utils.showProgress(holder, false);
                
                Drawable backgroundHolder = resources.getDrawable(R.color.white);
                Drawable background = resources.getDrawable(R.drawable.white_button_selector);
                Drawable action = resources.getDrawable(R.drawable.ic_action_download);
                
                holder.downloadPlay.setImageDrawable(action);
                Utils.setBackgroundForView(holder.downloadPlay, background);
                Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
            }
            else {
                if (update.status == DownloadManager.STATUS_PENDING || update.status == DownloadManager.STATUS_RUNNING) {
                    Utils.showProgress(holder, true);
                    
                    Drawable backgroundHolder = resources.getDrawable(R.color.holo_blue_light);
                    Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
                    holder.downloadProgressBar.setProgress(update.progress);
                }
                else if (!update.played) {
                    Utils.showProgress(holder, false);
                    
                    Drawable backgroundHolder = resources.getDrawable(R.color.white);
                    Drawable background = resources.getDrawable(R.drawable.light_textured_button_selector);
                    Drawable action = resources.getDrawable(R.drawable.ic_action_playback_play_blue_light);
                    
                    holder.downloadPlay.setImageDrawable(action);
                    Utils.setBackgroundForView(holder.downloadPlay, background);
                    Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
                }
                else if (update.isPlaying || update.isPaused) {
                    Utils.showProgress(holder, false);
                    
                    Drawable backgroundHolder = resources.getDrawable(R.color.white);
                    Drawable background = resources.getDrawable(R.drawable.blue_button_selector);
                    
                    Drawable action = resources.getDrawable(R.drawable.ic_action_playback_pause);
                    if (update.isPaused)
                        action = resources.getDrawable(R.drawable.ic_action_playback_play);
                    
                    holder.downloadPlay.setImageDrawable(action);
                    Utils.setBackgroundForView(holder.downloadPlay, background);
                    Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
                }
                else {
                    Utils.showProgress(holder, false);
                    
                    Drawable backgroundHolder = resources.getDrawable(R.color.white);
                    Drawable background = resources.getDrawable(R.drawable.white_button_selector);
                    Drawable action = resources.getDrawable(R.drawable.ic_action_playback_play_holo_light);
                    
                    holder.downloadPlay.setImageDrawable(action);
                    Utils.setBackgroundForView(holder.downloadPlay, background);
                    Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
                }
            }
            
            /*
            holder.elapsedTotal.setText(Utils.calculatePercent(update.duration, update.elapsed) + "%");
            
            if (update.duration > 0)
                holder.elapsedProgressBar.setMax(update.duration);
            if (update.elapsed > 0)
                holder.elapsedProgressBar.setProgress(update.elapsed);
                */
            
        }
        else {
            Log.e(TAG, "Update view with null updater, should not happen");
        }
    }
    
    public static void updateView(Resources resources, Content content, ContentAdapter.ViewHolder holder) {
        if (!content.exists && content.downloadStatus != DownloadManager.STATUS_PENDING && content.downloadStatus != DownloadManager.STATUS_RUNNING) {
            Utils.showProgress(holder, false);
            
            Drawable backgroundHolder = resources.getDrawable(R.color.white);
            Drawable background = resources.getDrawable(R.drawable.white_button_selector);
            Drawable action = resources.getDrawable(R.drawable.ic_action_download);
            
            holder.downloadPlay.setImageDrawable(action);
            Utils.setBackgroundForView(holder.downloadPlay, background);
            Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
        }
        else {
            if (content.downloadStatus == DownloadManager.STATUS_PENDING || content.downloadStatus == DownloadManager.STATUS_RUNNING) {
                Utils.showProgress(holder, true);
                
                Drawable backgroundHolder = resources.getDrawable(R.color.holo_blue_light);
                
                Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
                holder.downloadProgressBar.setProgress(content.downloadProgress);
            }
            else if (!content.played) {
                Utils.showProgress(holder, false);
                
                Drawable backgroundHolder = resources.getDrawable(R.color.white);
                Drawable background = resources.getDrawable(R.drawable.light_textured_button_selector);
                Drawable action = resources.getDrawable(R.drawable.ic_action_playback_play_blue_light);
                
                holder.downloadPlay.setImageDrawable(action);
                Utils.setBackgroundForView(holder.downloadPlay, background);
                Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
            }
            else if (content.isPlaying || content.isPaused) {
                Utils.showProgress(holder, false);
                
                Drawable backgroundHolder = resources.getDrawable(R.color.white);
                Drawable background = resources.getDrawable(R.drawable.blue_button_selector);
                
                Drawable action = resources.getDrawable(R.drawable.ic_action_playback_pause);
                if (content.isPaused)
                    action = resources.getDrawable(R.drawable.ic_action_playback_play);
                
                holder.downloadPlay.setImageDrawable(action);
                Utils.setBackgroundForView(holder.downloadPlay, background);
                Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
            }
            else {
                Utils.showProgress(holder, false);
                
                Drawable backgroundHolder = resources.getDrawable(R.color.white);
                Drawable background = resources.getDrawable(R.drawable.white_button_selector);
                Drawable action = resources.getDrawable(R.drawable.ic_action_playback_play_holo_light);
                
                holder.downloadPlay.setImageDrawable(action);
                Utils.setBackgroundForView(holder.downloadPlay, background);
                Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
            }
        }
        
        /*
        holder.elapsedTotal.setText(Utils.calculatePercent(content.duration, content.elapsed) + "%");
        
        if (content.duration > 0)
            holder.elapsedProgressBar.setMax(content.duration);
        if (content.elapsed > 0)
            holder.elapsedProgressBar.setProgress(content.elapsed);
            */
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackgroundForView(View view, Drawable background) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }
    
    private static void showProgress(ContentAdapter.ViewHolder holder, boolean visible) {
        if (visible) {
            holder.downloadProgressBar.setVisibility(View.VISIBLE);
            holder.downloadPlay.setVisibility(View.INVISIBLE);
        }
        else {
            holder.downloadProgressBar.setVisibility(View.INVISIBLE);
            holder.downloadPlay.setVisibility(View.VISIBLE);
        }
    }
}
