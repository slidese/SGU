
package se.slide.sgu;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class PlayService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_STOP = "STOP";
    
    public static final String EXTRA_FILENAME = "FILENAME";

    MediaPlayer mMediaPlayer = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        initialize();

        if (intent.getAction().equals(ACTION_PLAY)) {
            play(intent.getExtras().getString(EXTRA_FILENAME));
            startAsForeground();
        } else if (intent.getAction().equals(ACTION_STOP)) {

        }

        return START_NOT_STICKY;
    }
    
    

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void initialize() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void startAsForeground() {
        
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        
        Builder builder = new Notification.Builder(this);
        //builder.addAction(R.drawable.stat_notify_chat, "Can you hear the music?", System.currentTimeMillis());
        
        builder.setSmallIcon(R.drawable.ic_action_planet);
        builder.setTicker("Can you hear the music?");
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pi);
        
        Notification note = builder.build();
        
        //Notification note = new Notification(R.drawable.stat_notify_chat, "Can you hear the music?", System.currentTimeMillis());
        

        //note.setLatestEventInfo(this, "Fake Player", "Now Playing: \"Ummmm, Nothing\"", pi);
        note.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(1337, note);
    }

    private void play(String filename) {

        // String filename = intent.getExtras().getString("filename");

        Uri mp3Uri = Uri.parse(filename);

        try {
            mMediaPlayer.setDataSource(getApplicationContext(), mp3Uri);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync(); // prepare async to not block main thread
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!

        // maybe mMediaPlayer = null --> initialize()?

        return true;
    }
}
