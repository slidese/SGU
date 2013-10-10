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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.slidinglayer.SlidingLayer;

import org.codechimp.apprater.AppRater;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Section;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends Activity implements ContentListener {
    
    private final String TAG = "StartActivity";
    
    private SlidingLayer                    mSlidingLayer;
    private ImageButton                     mPlayButton;
    private ImageButton                     mSkipRewButton;
    private ImageButton                     mSkipForButton;
    private TextView                        mPlayerTitle;
    private TextView                        mPlayerDescription;
    private TextView                        mPlayerDurationNow;
    private TextView                        mPlayerDurationTotal;
    private TextView                        mPlayerDate;
    private LinearLayout                    mLinearLayoutPlayer;
    private RelativeLayout                  mRelativeLayoutNothingPlaying;
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
        
        GlobalContext.INSTANCE.init(this);
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
        
        // Bind activity to service
        mAudioPlayerIntent = new Intent(this, AudioPlayer.class);
        bindService(mAudioPlayerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        
        IntentFilter downloadFilter = new IntentFilter(DownloaderService.CONTENT_UPDATED);
        registerReceiver(mDownloadBroadcastReceiver, downloadFilter);
        
        mAudioPlayerBroadcastReceiver = new AudioPlayerBroadCastReceiver();
        IntentFilter audioPlayerFilter = new IntentFilter();
        audioPlayerFilter.addAction(AudioPlayer.UPDATE_PLAYLIST);
        audioPlayerFilter.addAction(AudioPlayer.EVENT_PLAY_PAUSE);
        registerReceiver(mAudioPlayerBroadcastReceiver, audioPlayerFilter);
        
        handleIntent();
        refreshScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        unbindService(mServiceConnection);
        
        unregisterReceiver(mDownloadBroadcastReceiver);
        unregisterReceiver(mAudioPlayerBroadcastReceiver);
        
        mAudioPlayerBroadcastReceiver = null;
        
        if (mUpdateCurrentTrackTask != null) {
            mUpdateCurrentTrackTask.stop();
        }
        
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
        mSkipRewButton = (ImageButton) findViewById(R.id.skipBackButton);
        mSkipForButton = (ImageButton) findViewById(R.id.skipForwardButton);
        mSeeker = (SeekBar) findViewById(R.id.seeker);
        mPlayerTitle = (TextView) findViewById(R.id.playerTitle);
        mPlayerDescription = (TextView) findViewById(R.id.playerDescription);
        mPlayerDurationNow = (TextView) findViewById(R.id.playerDurationNow);
        mPlayerDurationTotal = (TextView) findViewById(R.id.playerDurationTotal);
        mPlayerDate = (TextView) findViewById(R.id.playerDate);
        mLinearLayoutPlayer = (LinearLayout) findViewById(R.id.linearlayout_player);
        mRelativeLayoutNothingPlaying = (RelativeLayout) findViewById(R.id.nothingPlayingView);
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
        
        mSkipForButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.seek(mAudioPlayer.elapsed() + 30000);
                }
            }
        });
        
        mSkipRewButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.seek(mAudioPlayer.elapsed() - 10000);
                }
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
        
        mSeeker.setOnSeekBarChangeListener(new TimeLineChangeListener());
    }
    
    private void handleIntent() {
        
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

    private void loadSections(final Content track) {
        if (track == null)
            return;
        
        LinearLayout sectionLinearLayout = (LinearLayout) findViewById(R.id.section_linearlayout);
        sectionLinearLayout.removeAllViews();
        
        List<Section> listOfSection = DatabaseManager.getInstance().getSection(track.mp3);
        
        if (listOfSection == null)
            return;
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
        for (Section section : listOfSection) {
            View sectionView = inflater.inflate(R.layout.section_layout_item, null);
            
            TextView number = (TextView) sectionView.findViewById(R.id.sectionNumber);
            TextView title = (TextView) sectionView.findViewById(R.id.sectionTitle);
            TextView start = (TextView) sectionView.findViewById(R.id.sectionStart);
            
            final int seek = section.start * 1000;
            
            String txtNumber = String.valueOf(section.number);
            String txtStart = Formatter.formatTimeFromMillis(seek);
            
            number.setText(txtNumber);
            title.setText(section.title);
            start.setText(txtStart);
            
            sectionView.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mAudioPlayer.seekAndPlay(seek); // This need to be improved
                }
            });
            
            sectionLinearLayout.addView(sectionView);
        }
        
    }
    
    public void updatePlayQueue() {
                
        updatePlayPauseButtonState();
        
        if(mUpdateCurrentTrackTask == null) {
            mUpdateCurrentTrackTask = new UpdateCurrentTrackTask();
            mUpdateCurrentTrackTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            /*
            if (mAudioPlayer.isPlaying()) {
                mUpdateCurrentTrackTask.unPause();
            }
            */
            
            Log.e(TAG, "updateCurrentTrackTask is not null" );
        }
    }
    
    private void initPlayerView() {
        if (mAudioPlayer == null || (!mAudioPlayer.isPlaying() && !mAudioPlayer.isPaused())) {
            mLinearLayoutPlayer.setVisibility(View.GONE);
            mRelativeLayoutNothingPlaying.setVisibility(View.VISIBLE);
        }
        else {
            mLinearLayoutPlayer.setVisibility(View.VISIBLE);
            mRelativeLayoutNothingPlaying.setVisibility(View.GONE);
        }
    }
    
    private void updatePlayPanel(final Content track) {
        runOnUiThread(new Runnable() {
            
            public void run() {
                int elapsedMillis = mAudioPlayer.elapsed();
                
                String elapsedMessage = Formatter.formatTimeFromMillis(elapsedMillis);
                String totalMessage = Formatter.formatTimeFromMillis(track.duration);
                
                mSeeker.setMax(track.duration);
                mSeeker.setProgress(elapsedMillis);
                //PlayQueueActivity.this.elapsed.setText(message);
                
                mPlayerTitle.setText(track.title);
                mPlayerDescription.setText(track.description);
                mPlayerDurationNow.setText(elapsedMessage);
                mPlayerDurationTotal.setText(" / " + totalMessage);
                mPlayerDate.setText(GlobalContext.INSTANCE.formatDate(track.published));
            }
        });
    }
    
    private void updatePlayPauseButtonState() {
        Log.d(TAG, "Updating player state");
        
        if (mAudioPlayer.isPlaying() ) {
            mPlayButton.setImageResource(R.drawable.ic_action_playback_pause);
        } else {
            if (mAudioPlayer.getCurrentTrack() == null)
                mPlayButton.setImageResource(R.drawable.ic_action_playback_stop);
            else
                mPlayButton.setImageResource(R.drawable.ic_action_playback_play);
        }
    }
    
    /**
     * Interface implementations
     */
    
    public void playContent(Content content) {
        mAudioPlayer.play(content);
        loadSections(content);
        initPlayerView();
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
    
    private class TimeLineChangeListener implements SeekBar.OnSeekBarChangeListener {
        private Timer delayedSeekTimer;
        
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if( fromUser ) {
                Log.d(TAG,"TimeLineChangeListener progress received from user: "+progress);
                
                scheduleSeek(progress);
                
                String elapsedMessage = Formatter.formatTimeFromMillis(progress);
                mPlayerDurationNow.setText(elapsedMessage);
                
                return;
            }
        }
        
        private void scheduleSeek(final int  progress) {
            if( delayedSeekTimer != null) {
                delayedSeekTimer.cancel();
            }
            delayedSeekTimer = new Timer();
            delayedSeekTimer.schedule(new TimerTask() {
                
                @Override
                public void run() {
                    Log.d(TAG,"Delayed Seek Timer run");
                    
                    
                    
                    mAudioPlayer.seek(progress);
                    
                    //updatePlayPanel(audioPlayer.getCurrentTrack());
                }
            }, 170);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG,"TimeLineChangeListener started tracking touch");
            mUpdateCurrentTrackTask.pause();
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG,"TimeLineChangeListener stopped tracking touch");
            mUpdateCurrentTrackTask.unPause();
        }
        
    }
    
    private class AudioPlayerBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"AudioPlayerBroadCastReceiver.onReceive action=" + intent.getAction());
            
            if(AudioPlayer.UPDATE_PLAYLIST.equals( intent.getAction())) {
                updatePlayQueue();
            }
            else if (AudioPlayer.EVENT_PLAY_PAUSE.equals(intent.getAction())) {
                updatePlayQueue();
            }
        }
    }
    
    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"DownloadBroadcastReceiver.onReceive action = " + intent.getAction());
            
            if(intent.getAction().equals(DownloaderService.CONTENT_UPDATED)) {
                ContentFragment fragment = (ContentFragment) getFragmentManager().findFragmentById(R.id.adfree_content_list_container);
                
                if (fragment != null) {
                    fragment.refresh();
                }
            }
        }
    }
    
    private final class AudioPlayerServiceConnection implements ServiceConnection {
        
        @SuppressWarnings("unchecked")
        public void onServiceConnected(ComponentName className, IBinder baBinder) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service connected");
            
            mAudioPlayer = ((LocalBinder<AudioPlayer>) baBinder).getService();
            startService(mAudioPlayerIntent);
            loadSections(mAudioPlayer.getCurrentTrack());
            updatePlayQueue();
            initPlayerView();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service disconnected");
            
            mAudioPlayer = null;
        }
    }
}
