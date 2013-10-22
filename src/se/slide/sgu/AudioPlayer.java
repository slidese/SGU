package se.slide.sgu;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import se.slide.sgu.model.Content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
import com.augusto.mymediaplayer.R;
import com.augusto.mymediaplayer.model.Track;
import com.augusto.mymediaplayer.repositories.MusicRepository;
*/

// https://github.com/augusto/android-sandbox/blob/master/src/com/augusto/mymediaplayer/services/AudioPlayer.java

public class AudioPlayer extends Service implements OnCompletionListener {
    
    public static final String INTENT_BASE_NAME = "se.slide.sgu.AudioPlayer";
    
    public static final String UPDATE_PLAYLIST =    INTENT_BASE_NAME + ".PLAYLIST_UPDATED";
    public static final String QUEUE_TRACK =        INTENT_BASE_NAME + ".QUEUE_TRACK";
    public static final String PAUSE_TRACK =        INTENT_BASE_NAME + ".PAUSE_TRACK";
    public static final String PLAY_TRACK =         INTENT_BASE_NAME + ".PLAY_TRACK";
    public static final String QUEUE_ALBUM =        INTENT_BASE_NAME + ".QUEUE_ALBUM";
    public static final String PLAY_ALBUM =         INTENT_BASE_NAME + ".PLAY_ALBUM";
    public static final String PLAY_PAUSE_TRACK =   INTENT_BASE_NAME + ".PLAY_PAUSE_TRACK";
    
    public static final String EVENT_PLAY_PAUSE =   INTENT_BASE_NAME + ".EVENT_PLAY_PAUSE";
    
    private final String TAG = "AudioPlayer";
    
    private List<Content> tracks = new ArrayList<Content>();
    //private List<Track> tracks = new ArrayList<Track>(); // this collection should be encapsulated in another class.
    //private MusicRepository musicRepository = new MusicRepository();
    private MediaPlayer mediaPlayer;
    private boolean paused = false;
    private AudioPlayerBroadcastReceiver broadcastReceiver = new AudioPlayerBroadcastReceiver();
    PhoneStateListener phoneStateListener;
    private final int NOTIFICATION_ID = 24; // Meaning of life, eh?
    private final int NOTIFICATION_ID_LOW_MEMORY = 11;

    /*
    public class AudioPlayerBinder extends Binder {
        public AudioPlayer getService() {
            Log.v(TAG, "AudioPlayerBinder: getService() called");
            return AudioPlayer.this;
        }
    }
    */

    private final IBinder audioPlayerBinder = new LocalBinder<AudioPlayer>(this);

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "AudioPlayer: onBind() called");
        return audioPlayerBinder; // maybe, return new LocalBinder<AudioPlayer>(this);
    }
    
    @Override
    public void onCreate() {
        Log.v(TAG, "AudioPlayer: onCreate() called");
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY_TRACK);
        intentFilter.addAction(QUEUE_TRACK);
        intentFilter.addAction(PLAY_ALBUM);
        intentFilter.addAction(QUEUE_ALBUM);
        intentFilter.addAction(PAUSE_TRACK);
        intentFilter.addAction(PLAY_PAUSE_TRACK);
        registerReceiver(broadcastReceiver, intentFilter);
        
        // Let's pay attention to incoming calls and pause the player during the call duration
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    pause();
                }
                else if(state == TelephonyManager.CALL_STATE_IDLE) {
                   play();
                }
                else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                   pause();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "AudioPlayer: onStart() called, instance=" + this.hashCode());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "AudioPlayer: onDestroy() called");
        unregisterReceiver(broadcastReceiver);
        release();
        
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    
    @Override
    public void onLowMemory() {
        Notification notification = GlobalContext.INSTANCE.buildNotification(getString(R.string.closing_low_memory_ticker), getString(R.string.closing_low_memory_title), getString(R.string.closing_low_memory_text));
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID_LOW_MEMORY, notification);
        stopSelf();
    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        release();
        nextTrack();
    }
    
    @SuppressLint("NewApi")
    private Notification buildNotification(boolean showTicker) {
        Content track = getCurrentTrack();
        
        if (track == null) {
            release();
            return null;
        }
        
        Intent i = new Intent(this, StartActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        
        Intent pauseIntent = new Intent(PLAY_PAUSE_TRACK);
        PendingIntent pendingPauseIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);
        
        Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_action_planet);
        
        if (showTicker)
            builder.setTicker(track.title);
        
        builder.setContentTitle(track.title);
        builder.setContentText(track.description);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pi);

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            if (isPlaying())
                builder.addAction(R.drawable.ic_action_playback_pause, getString(R.string.pause), pendingPauseIntent);
            else
                builder.addAction(R.drawable.ic_action_playback_play, getString(R.string.play), pendingPauseIntent);
        }
        
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
    
    private void startAsForeground() {
        
        Notification note = buildNotification(true);
        
        if (note == null)
            return;

        startForeground(NOTIFICATION_ID, note);
    }
    
    private void updateNotification() {
        Notification note = buildNotification(false);
        
        if (note == null)
            return;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        mNotificationManager.notify(NOTIFICATION_ID, note);
    }
    
    private void release() {
        if( mediaPlayer == null) {
            return;
        }
        
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
        
        stopForeground(true);
    }

    public void addTrack(Content track) {
        Log.d(TAG, "addTrack " + track);
        tracks.add(track);
        if( tracks.size() == 1) {
            play();
        }
    }
    
    public void play(Content track) {
        stop();
        tracks.clear();
        tracks.add(track);
        play();
    }
    
    public void play() {
        playListUpdated();
        if( tracks.size() == 0) {
            return;
        }
        
        Content track = tracks.get(0);

        if( mediaPlayer != null && paused) {
            mediaPlayer.start();
            paused = false;
            updateNotification();
            playPauseUpdated();
            return;
        } else if( mediaPlayer != null ) {
            release();
        }
        
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, track.asUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            
            track.duration = mediaPlayer.getDuration();
            startAsForeground();
            playPauseUpdated();
        } catch (IOException ioe) {
            Log.e(TAG,"error trying to play " + track , ioe);
            String message = "error trying to play track: " + track + ".\nError: " + ioe.getMessage();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void playListUpdated() {
        Intent updatePlaylistIntent = new Intent(UPDATE_PLAYLIST);
        this.sendBroadcast(updatePlaylistIntent);
    }
    
    private void playPauseUpdated() {
        Intent playPauselistIntent = new Intent(EVENT_PLAY_PAUSE);
        this.sendBroadcast(playPauselistIntent);
    }

    private void nextTrack() {
        tracks.remove(0);
        play();
    }

    public Content[] getQueuedTracks() {
        Content[] tracksArray = new Content[tracks.size()];
        return tracks.toArray(tracksArray);
    }

    public void stop() {
        release();
        playPauseUpdated();
    }

    public boolean isPlaying() {
        if(tracks.isEmpty() || mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        if( mediaPlayer != null) {
            mediaPlayer.pause();
            paused = true;
            updateNotification();
            playPauseUpdated();
        }
    }
    
    public boolean isPaused() {
        if (mediaPlayer != null) {
            return paused;
        }
        else {
            return false;
        }
    }

    public Content getCurrentTrack() {
        if (tracks.isEmpty()) {
            return null;
        }
        
        return tracks.get(0);
    }
    
    public boolean hasTracks() {
        if (tracks.isEmpty() || tracks.size() < 1) {
            return false;
        }
        else {
            return true;
        }
    }

    public int elapsed() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    public void seek(int timeInMillis) {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(timeInMillis);
        }
    }
    
    public void seekAndPlay(int timeInMillis) {
        if(mediaPlayer != null) {
            play();
            mediaPlayer.seekTo(timeInMillis);
        }
    }

    /*
    private void playTrack(long trackId) {
        Track track = musicRepository.findTracksId(this, trackId);
        play(track);
    }
    
    private void queueTrack(long trackId) {
        Track track = musicRepository.findTracksId(this, trackId);
        addTrack(track);
    }

    private void playAlbum(long albumId) {
        Track[] tracks = musicRepository.findTracksByAlbumId(this, albumId);
        this.tracks.clear(); // I DON'T LIKE THIS!!!
        stop();
        for( Track track : tracks) {
            this.tracks.add(track);
        }
        play();
    }

    private void queueAlbum(long albumId) {
        Track[] tracks = musicRepository.findTracksByAlbumId(this, albumId);
        for( Track track : tracks) {
            addTrack(track);
        }
    }
    */
    
    private void playPauseTrack() {
        if (paused)
            play();
        else
            pause();
    }
    
    private class AudioPlayerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            long id = intent.getLongExtra("id", -1);
            Log.d(TAG, "Received intent for action " + intent.getAction() + " for id: " + id);

            if(PLAY_ALBUM.equals(action)) {
                //playAlbum(id);
            } else if(QUEUE_ALBUM.equals(action)) {
                //queueAlbum(id);
            } else if(PLAY_TRACK.equals(action)) {
                //playTrack(id);
            } else if(QUEUE_TRACK.equals(action)) {
                //queueTrack(id);
            } else if(PLAY_PAUSE_TRACK.equals(action)) {
                playPauseTrack();
            } else {
                Log.d(TAG, "Action not recognized: " + action);
            }
        }

    }
}
