package se.slide.sgu;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;

import de.passsy.holocircularprogressbar.HoloCircularProgressBar;
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
    
    public static int calculatePercent(int total, int part) {
        /*
        if (total < 1 || part < 1)
            return 0;
        
        if (total == part)
            return 100;
        
        double d = (double)part / (double)total * 100;
        long percent = Math.round(d);
        
        return (int)percent;
        */
        
        return (int)(part * 100.0 / total + 0.5);
    }
    
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
            MyLog.e(TAG, "Update view with null updater, should not happen");
        }
    }
    
    public static void updateView(Resources resources, Content content, ContentAdapter.ViewHolder holder) {
        if (!content.exists && content.downloadStatus != DownloadManager.STATUS_PENDING && content.downloadStatus != DownloadManager.STATUS_RUNNING) {
            Utils.showProgress(holder, false);
            
            Drawable backgroundHolder = resources.getDrawable(R.color.white);
            Drawable background = resources.getDrawable(R.drawable.white_button_selector);
            Drawable action = resources.getDrawable(R.drawable.ic_action_download);
            
            holder.downloadPlay.setImageDrawable(action);
            holder.elapsedProgressBar.setVisibility(View.GONE);
            holder.elapsedTotal.setVisibility(View.GONE);
            Utils.setBackgroundForView(holder.downloadPlay, background);
            Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
        }
        else {
            if (content.downloadStatus == DownloadManager.STATUS_PENDING || content.downloadStatus == DownloadManager.STATUS_RUNNING) {
                Utils.showProgress(holder, true);
                
                Drawable backgroundHolder = resources.getDrawable(R.color.holo_blue_light);
                
                Utils.setBackgroundForView(holder.progressAndButtonHolder, backgroundHolder);
                animate(holder.downloadProgressBar, null, content.downloadProgressOld, content.downloadProgress, 200);
                content.downloadProgressOld = content.downloadProgress;
                //holder.downloadProgressBar.setProgress(content.downloadProgress);
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
        
        int percent = Utils.calculatePercent(content.duration, content.elapsed);
        holder.elapsedTotal.setText(percent + "%");
        
        /*
        if (content.duration > 0)
            holder.elapsedProgressBar.setMax(content.duration);
            */
        if (content.elapsed > 0)
            holder.elapsedProgressBar.setProgress(percent);
    }
    
    private static void animate(final HoloCircularProgressBar progressBar, final AnimatorListener listener, final float oldProgress, final float progress, final int duration) {
        
        ObjectAnimator mProgressBarAnimator;
    
        mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", oldProgress, progress);
        mProgressBarAnimator.setDuration(duration);
    
        mProgressBarAnimator.addListener(new AnimatorListener() {
    
                @Override
                public void onAnimationCancel(final Animator animation) {
                }
    
                @Override
                public void onAnimationEnd(final Animator animation) {
                        progressBar.setProgress(progress);
                }
    
                @Override
                public void onAnimationRepeat(final Animator animation) {
                }
    
                @Override
                public void onAnimationStart(final Animator animation) {
                }
        });
        if (listener != null) {
                mProgressBarAnimator.addListener(listener);
        }
        mProgressBarAnimator.reverse();
        mProgressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
    
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                        progressBar.setProgress((Float) animation.getAnimatedValue());
                }
        });
        //progressBar.setMarkerProgress(progress);
        mProgressBarAnimator.start();
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
