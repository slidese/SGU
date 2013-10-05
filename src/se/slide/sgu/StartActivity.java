package se.slide.sgu;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.slidinglayer.SlidingLayer;

import org.codechimp.apprater.AppRater;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends Activity implements ContentListener {
    
    private final String TAG = "StartActivity";
    
    private SlidingLayer                    mSlidingLayer;
    private ImageButton                     mPlayButton;
    private TextView                        mPlayerTitle;
    private TextView                        mPlayerDescription;
    private BroadcastReceiver               mDownloadBroadcastReceiver = new DownloadBroadcastReceiver();
    private ServiceConnection               mServiceConnection = new AudioPlayerServiceConnection();
    private AudioPlayer                     mAudioPlayer;
    private Intent                          mAudioPlayerIntent;
    private UpdateCurrentTrackTask          mUpdateCurrentTrackTask;
    private BroadcastReceiver               mAudioPlayerBroadcastReceiver = new AudioPlayerBroadCastReceiver();
    private Timer                           mWaitForAudioPlayertimer = new Timer();
    private Handler                         mHandler = new Handler();
    private SeekBar                         mSeeker;
    
    final String[] actions = new String[] {
            "Ad Free",
            "Premium"
    };
    
    static final int UPDATE_INTERVAL = 250;

    /**
     * Life-cycle methods
     */
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        
        bindViews();
        initState();
        
        DatabaseManager.init(this);
        ContentDownloadManager.INSTANCE.init(getApplicationContext()); // Use application context since the download manager should live during the app's entire life
        AppRater.app_launched(this);
    }
    
    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance(this).activityStop(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        IntentFilter downloadFilter = new IntentFilter(DownloaderService.CONTENT_UPDATED);
        registerReceiver(mDownloadBroadcastReceiver, downloadFilter);
        
        mAudioPlayerBroadcastReceiver = new AudioPlayerBroadCastReceiver();
        IntentFilter audioPlayerFilter = new IntentFilter(AudioPlayer.UPDATE_PLAYLIST);
        registerReceiver(mAudioPlayerBroadcastReceiver, audioPlayerFilter);
        
        refreshScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        unregisterReceiver(mDownloadBroadcastReceiver);
        unregisterReceiver(mAudioPlayerBroadcastReceiver);
        
        mAudioPlayerBroadcastReceiver = null;
        
        mUpdateCurrentTrackTask.stop();
        mUpdateCurrentTrackTask = null;
    }
    
    /**
     * Menu methods
     */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.action_reload) {
            
            startService(new Intent(this, DownloaderService.class));
            
            /*
            String username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
            String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", "");
            
            new ReloadTask(username, password).execute();
            */
            
            return true;
        }
        else if (item.getItemId() == R.id.action_show_player) {
            if (mSlidingLayer.isOpened())
                mSlidingLayer.closeLayer(true);
            else
                mSlidingLayer.openLayer(true);
        }
        else if (item.getItemId() == R.id.action_settings) {
            
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
    
    /**
     * View binding
     */
    private void bindViews() {
        mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mSeeker = (SeekBar) findViewById(R.id.seeker);
        mPlayerTitle = (TextView) findViewById(R.id.playerTitle);
        mPlayerDescription = (TextView) findViewById(R.id.playerDescription);
    }
    
    /**
     * Initializes the origin state of the layer
     */
    private void initState() {
        LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        
        mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_RIGHT);
        mSlidingLayer.setOffsetWidth(0);
        mSlidingLayer.setLayoutParams(rlp);
        
        mPlayButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.pause();
                    //mUpdateCurrentTrackTask.pause();
                }
                else {
                    if (mAudioPlayer.hasTracks()) {
                        mAudioPlayer.play();
                        //mUpdateCurrentTrackTask.pause();
                    }
                }
                
                refreshScreen();
            }
        });
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, actions);
        
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(adapter, new OnNavigationListener() {
            
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                ContentFragment fragment = (ContentFragment) getFragmentManager().findFragmentById(R.id.adfree_content_list_container);
                
                if (fragment == null)
                    return false;
                
                if (itemPosition == 0) {
                   fragment.setMode(ContentFragment.MODE_ADFREE);
                    
                    
                    /*
                    Bundle args = new Bundle();
                    args.putInt(ContentFragment.CONTENT_KEY, ContentFragment.CONTENT_TYPE_ADFREE);
                    
                    ContentFragment fragment = new ContentFragment();
                    fragment.setArguments(args);
                    
                    getFragmentManager().beginTransaction()
                        .replace(R.id.adfree_content_list_container, fragment)
                        .commit();
                    */
                }
                else {
                    fragment.setMode(ContentFragment.MODE_PREMIUM);
                    
                    /*
                    Bundle args = new Bundle();
                    args.putInt(ContentFragment.CONTENT_KEY, ContentFragment.CONTENT_TYPE_PREMIUM);
                    
                    ContentFragment fragment = new ContentFragment();
                    fragment.setArguments(args);
                    
                    getFragmentManager().beginTransaction()
                        .replace(R.id.adfree_content_list_container, fragment)
                        .commit();
                    */
                }
                
                return false;
            }
        });
        
        getActionBar().setTitle("");
        
        // Bind activity to service
        mAudioPlayerIntent = new Intent(this, AudioPlayer.class);
        bindService(mAudioPlayerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void refreshScreen() {
        if(mAudioPlayer == null) {
            updateScreenAsync();
        } else {
            updatePlayQueue();
        }
    }
    
    private void updateScreenAsync() {
        mWaitForAudioPlayertimer.scheduleAtFixedRate( new TimerTask() {
            
            public void run() {
                Log.d(TAG,"updateScreenAsync running timmer");
                if(mAudioPlayer != null) {
                    mWaitForAudioPlayertimer.cancel();
                    mHandler.post( new Runnable() {
                        public void run() {
                            updatePlayQueue();
                        }
                    });
                }
            }
            }, 10, UPDATE_INTERVAL);
    }
    
    public void updatePlayQueue() {
                
        updatePlayPauseButtonState();
        
        if(mUpdateCurrentTrackTask == null) {
            mUpdateCurrentTrackTask = new UpdateCurrentTrackTask();
            mUpdateCurrentTrackTask.execute();
        } else {
            /*
            if (mAudioPlayer.isPlaying()) {
                mUpdateCurrentTrackTask.unPause();
            }
            */
            
            Log.e(TAG, "updateCurrentTrackTask is not null" );
        }
    }
    
    private void updatePlayPanel(final Content track) {
        runOnUiThread(new Runnable() {
            
            public void run() {
                int elapsedMillis = mAudioPlayer.elapsed();
                //String message = track.getTitle() + " - " + Formatter.formatTimeFromMillis(elapsedMillis);
                mSeeker.setMax(track.duration);
                mSeeker.setProgress(elapsedMillis);
                //PlayQueueActivity.this.elapsed.setText(message);
                
                mPlayerTitle.setText(track.title);
                mPlayerDescription.setText(track.description);
            }
        });
    }
    
    private void updatePlayPauseButtonState() {
        if(mAudioPlayer.isPlaying() ) {
            mPlayButton.setImageResource(R.drawable.ic_action_playback_pause);
        } else {
            mPlayButton.setImageResource(R.drawable.ic_action_playback_play);
        }
    }
    
    /**
     * Interface implementations
     */
    
    public void playContent(Content content) {
        mAudioPlayer.play(content);
    }
    
    /**
     * Inner classes
     */
    
    private class UpdateCurrentTrackTask extends AsyncTask<Void, Content, Void> {

        public boolean stopped = false;
        public boolean paused = false;
        
        @Override
        protected Void doInBackground(Void... params) {
            while( ! stopped ) {
                if( ! paused) {
                    Content currentTrack = mAudioPlayer.getCurrentTrack();
                    if( currentTrack != null ) {
                        publishProgress(currentTrack);
                    }
                }
                
                try {
                    Thread.sleep(250);
                }
                catch (InterruptedException e) {
                    // Do nothing, we just need to sleep...
                }
                
            }
            
            Log.d(TAG,"AsyncTask stopped");
            
            return null;
        }
        
        @Override
        protected void onProgressUpdate(Content... track) {
            if( stopped || paused ) {
                return; //to avoid glitches
            }
            
            updatePlayPanel(track[0]);
        }

        public void stop() {
            stopped = true;
        }
        
        public void pause() {
            this.paused = true;
        }

        public void unPause() {
            this.paused = false;
        }
    }
    
    private class AudioPlayerBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"AudioPlayerBroadCastReceiver.onReceive action=" + intent.getAction());
            
            if( AudioPlayer.UPDATE_PLAYLIST.equals( intent.getAction())) {
                updatePlayQueue();
            }
        }
    }
    
    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"DownloadBroadcastReceiver.onReceive action = " + intent.getAction());
            
            if(intent.getAction().equals(DownloaderService.CONTENT_UPDATED)) {
                // update playlist
            }
        }
    }
    
    private final class AudioPlayerServiceConnection implements ServiceConnection {
        
        @SuppressWarnings("unchecked")
        public void onServiceConnected(ComponentName className, IBinder baBinder) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service connected");
            
            mAudioPlayer = ((LocalBinder<AudioPlayer>) baBinder).getService();
            startService(mAudioPlayerIntent);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service disconnected");
            
            mAudioPlayer = null;
        }
    }
}
