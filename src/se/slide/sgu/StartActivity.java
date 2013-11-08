package se.slide.sgu;

import android.app.ActionBar;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.slidinglayer.SlidingLayer;

import org.codechimp.apprater.AppRater;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;
import se.slide.sgu.model.Section;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends FragmentActivity implements ContentListener, ActionBar.OnNavigationListener {
    
    private final String TAG = "StartActivity";
    
    private SlidingLayer                    mSlidingLayer;
    private ImageButton                     mPlayButton;
    private ImageButton                     mShowSections;
    private ImageButton                     mSkipRewButton;
    private ImageButton                     mSkipForButton;
    private ImageButton                     mPlayNextSectionButton;
    private ImageButton                     mPlayPreviousSectionButton;
    private TextView                        mPlayerTitle;
    private TextView                        mPlayerDescription;
    private TextView                        mPlayerDurationNow;
    private TextView                        mPlayerDurationTotal;
    private TextView                        mPlayerDate;
    private LinearLayout                    mLinearLayoutPlayer;
    private LinearLayout                    mLinearLayout;
    private LinearLayout                    mLinearLayoutIndicator;
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
    private int                             mMode;
    private boolean                         mShowingBack = false;
    private List<Section>                   mLatestLoadedSections;
    private Content                         mLatestLoadedTrack;
    private Episode                         mLatestLoadedEpisode;
    private PullToRefreshAttacher           mPullToRefreshAttacher;
    
    static final int UPDATE_INTERVAL = 250;

    /**
     * Life-cycle methods
     */
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Utils.setStrictMode();
        
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_start);
        
        bindViews();
        initState(savedInstanceState);
        
        GlobalContext.INSTANCE.init(this);
        DatabaseManager.init(this);
        ContentDownloadManager.INSTANCE.init(this);
        AppRater.app_launched(this);
        VolleyHelper.init(this);
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
        
        IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(DownloaderService.DOWNLOAD_FINISHED);
        downloadFilter.addAction(DownloaderService.DOWNLOAD_STARTED);
        registerReceiver(mDownloadBroadcastReceiver, downloadFilter);
        
        mAudioPlayerBroadcastReceiver = new AudioPlayerBroadCastReceiver();
        IntentFilter audioPlayerFilter = new IntentFilter();
        audioPlayerFilter.addAction(AudioPlayer.UPDATE_PLAYLIST);
        audioPlayerFilter.addAction(AudioPlayer.EVENT_PLAY_PAUSE);
        registerReceiver(mAudioPlayerBroadcastReceiver, audioPlayerFilter);
        
        handleIntent();
        initPlayerView();
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
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt("mode", mMode);
    }

    /**
     * Menu methods
     */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
        if (fragment instanceof MainPodcastFragment) {
            // should make sure
            
        }
        else {
            // MainDetailsFragment
            MenuItem reload = menu.findItem(R.id.action_reload);
            reload.setVisible(false);
        }
        
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.action_reload) {
            
            startService(new Intent(this, DownloaderService.class));
            
            return true;
        }
        /*
        else if (item.getItemId() == R.id.action_show_player) {
            if (mSlidingLayer.isOpened())
                mSlidingLayer.closeLayer(true);
            else
                mSlidingLayer.openLayer(true);
            
        }
        */
        else if (item.getItemId() == R.id.action_settings) {
            
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_about) {
            
            Intent intent = new Intent(this, AboutActivity.class);
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
        mShowSections = (ImageButton) findViewById(R.id.showSections);
        mSkipRewButton = (ImageButton) findViewById(R.id.skipBackButton);
        mSkipForButton = (ImageButton) findViewById(R.id.skipForwardButton);
        mPlayNextSectionButton = (ImageButton) findViewById(R.id.playNextSectionButton);
        mPlayPreviousSectionButton = (ImageButton) findViewById(R.id.playPrevSectionButton);
        mSeeker = (SeekBar) findViewById(R.id.seeker);
        mPlayerTitle = (TextView) findViewById(R.id.playerTitle);
        mPlayerDescription = (TextView) findViewById(R.id.playerDescription);
        mPlayerDurationNow = (TextView) findViewById(R.id.playerDurationNow);
        mPlayerDurationTotal = (TextView) findViewById(R.id.playerDurationTotal);
        mPlayerDate = (TextView) findViewById(R.id.playerDate);
        mLinearLayoutPlayer = (LinearLayout) findViewById(R.id.linearlayout_player);
        mLinearLayout = (LinearLayout) findViewById(R.id.player_linearlayout);
        mLinearLayoutIndicator = (LinearLayout) findViewById(R.id.player_linearlayout_indicators);
        mRelativeLayoutNothingPlaying = (RelativeLayout) findViewById(R.id.nothingPlayingView);
    }
    
    /**
     * Initializes the origin state of the layer
     */
    private void initState(Bundle savedInstanceState) {
        LayoutParams rlp = (LayoutParams) mSlidingLayer.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        
        mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_BOTTOM);
        mSlidingLayer.setOffsetWidth(0);
        mSlidingLayer.setLayoutParams(rlp);
        mSlidingLayer.setCloseOnTapEnabled(false);
        mSlidingLayer.setSlidingEnabled(false);
        
        mShowSections.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mSlidingLayer.isOpened())
                    mSlidingLayer.closeLayer(true);
                else
                    mSlidingLayer.openLayer(true);
            }
        });
        
        mPlayButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.pause();
                    //mUpdateCurrentTrackTask.pause();
                }
                else if (mAudioPlayer.isPaused()) {
                    if (mAudioPlayer.hasTracks()) {
                        mAudioPlayer.play();
                        //mUpdateCurrentTrackTask.pause();
                    }
                }
                else {
                    if (mLatestLoadedTrack != null)
                        mAudioPlayer.play(mLatestLoadedTrack);
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
        
        mPlayNextSectionButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                skipToNextSection();
            }
        });
        
        mPlayPreviousSectionButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                skipToPreviousSection();
            }
        });
        
        //getActionBar().setSelectedNavigationItem(mMode);
        getActionBar().setTitle("");
        
        
        

        // Create a PullToRefreshAttacher instance
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

        mSeeker.setOnSeekBarChangeListener(new TimeLineChangeListener());
        
        if (savedInstanceState != null) {
            mMode = savedInstanceState.getInt("mode"); // default is 0
        }
        else {
            Fragment fragment = new MainPodcastFragment();
            //fragment.set1RetainInstance(true);
            
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_1, fragment)
                .commit();    
        }
        
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
                Log.v(TAG, "Waiting for bind to service");
                
                if(mAudioPlayer != null) {
                    Log.v(TAG, "Bind to service completed");
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
        
        mLatestLoadedSections = DatabaseManager.getInstance().getSection(track.mp3);
        
        if (mLatestLoadedSections == null)
            return;
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
        for (Section section : mLatestLoadedSections) {
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
            
            View sectionDivider = inflater.inflate(R.layout.section_divider, sectionLinearLayout, false);
            
            sectionLinearLayout.addView(sectionView);
            sectionLinearLayout.addView(sectionDivider);
        }
        
    }
    
    private void initPlayerCard(final Content content) {
        if (content == null)
            return;
        
        // Before trying to load a new, reset the latest episode variable
        mLatestLoadedEpisode = null;
        List<Episode> listOfEpisode = DatabaseManager.getInstance().getEpisode(content.mp3);
        if (listOfEpisode != null && listOfEpisode.size() > 0)
            mLatestLoadedEpisode = listOfEpisode.get(0);
        
        String title = content.title;
        String description = content.description;
        Date date = content.published;
        
        if (mLatestLoadedEpisode != null) {
            if (mLatestLoadedEpisode.title != null)
                title = "The Skeptic's Guide: " + mLatestLoadedEpisode.title;
        }
        
        mPlayerTitle.setText(title);
        mPlayerDescription.setText(description);
        mPlayerDate.setText(GlobalContext.INSTANCE.formatDate(date));
    }
    
    private void skipToNextSection() {
        if (mLatestLoadedSections == null || mAudioPlayer == null)
            return;
        
        int currentElapsed = mAudioPlayer.elapsed();
        
        for (Section section : mLatestLoadedSections) {
            final int seek = section.start * 1000;
            if (seek > currentElapsed) {
                mAudioPlayer.seekAndPlay(seek);
                return;
            }
        }
        
    }
    
    private void skipToPreviousSection() {
        if (mLatestLoadedSections == null || mAudioPlayer == null)
            return;
        
        int currentElapsed = mAudioPlayer.elapsed();
        
        Section prev = null;
        Section skipToMe = null;
        for (Section section : mLatestLoadedSections) {
            final int seek = section.start * 1000;
            if (currentElapsed < seek && prev != null && (prev.start*1000) < currentElapsed) {
                if (skipToMe != null) {
                    final int prevSeek = skipToMe.start * 1000; 
                    mAudioPlayer.seekAndPlay(prevSeek);
                    return;    
                }
            }
            
            skipToMe = prev;
            prev = section;
        }
        
        if (skipToMe != null) {
            final int prevSeek = skipToMe.start * 1000; 
            mAudioPlayer.seekAndPlay(prevSeek);
            return;
        }
    }
    
    private Section getCurrentlyPlayingSection() {
        if (mLatestLoadedSections == null || mAudioPlayer == null)
            return null;
        
        int currentElapsed = mAudioPlayer.elapsed();
        
        for (Section section : mLatestLoadedSections) {
            final int seek = section.start * 1000;
            if (seek > currentElapsed) {
                return section;
            }
        }
        
        return null;
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
        }
    }
    
    private void initPlayerView() {
        if (mAudioPlayer == null || (mAudioPlayer.getCurrentTrack() == null) || (!mAudioPlayer.isPlaying() && !mAudioPlayer.isPaused())) {
            mLinearLayoutPlayer.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.GONE);
            mLinearLayoutIndicator.setVisibility(View.GONE);
            mSlidingLayer.setVisibility(View.GONE);
            mRelativeLayoutNothingPlaying.setVisibility(View.VISIBLE);
            
            mSlidingLayer.closeLayer(false);
        }
        else {
            mLinearLayoutPlayer.setVisibility(View.VISIBLE);
            mLinearLayout.setVisibility(View.VISIBLE);
            mLinearLayoutIndicator.setVisibility(View.VISIBLE);
            mSlidingLayer.setVisibility(View.VISIBLE);
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
                mPlayerDurationNow.setText(elapsedMessage);
                mPlayerDurationTotal.setText(" / " + totalMessage);
            }
        });
    }
    
    private void updatePlayPauseButtonState() {
        Log.d(TAG, "Updating player state");
        
        if (mAudioPlayer.isPlaying() ) {
            mPlayButton.setImageResource(R.drawable.ic_action_playback_pause);
        } else {
            if (mAudioPlayer.getCurrentTrack() == null)
                mPlayButton.setImageResource(R.drawable.ic_action_playback_repeat);
            else
                mPlayButton.setImageResource(R.drawable.ic_action_playback_play);
        }
    }
    
    /**
     * Interface implementations
     */
    
    public void playContent(Content content) {
        final Content currentContent = mAudioPlayer.getCurrentTrack();
        if (currentContent != null && currentContent.mp3.equals(content.mp3) && mAudioPlayer.isPlaying())
            mAudioPlayer.pause();
        else if (currentContent != null && currentContent.mp3.equals(content.mp3) && mAudioPlayer.isPaused())
            mAudioPlayer.play();
        else {
            mAudioPlayer.play(content);
            mLatestLoadedTrack = content;
            loadSections(content);
            initPlayerView();
            initPlayerCard(content);
            
            // Update played state
            content.played = true;
            DatabaseManager.getInstance().createOrUpdateContent(content);
        }
    }
    
    public int getMode() {
        return mMode;
    }
    
    public void showContentDetails(Content content) {
        /*
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.
        mShowingBack = true;
        
        .setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
        
        */
        
        Bundle args = new Bundle();
        args.putString(MainDetailsFragment.CONTENT_MP3, content.mp3);
        //args.putInt(ContentFragment.CONTENT_KEY, ContentFragment.CONTENT_TYPE_ADFREE);
        
        List<Episode> listOfEpisodes = DatabaseManager.getInstance().getEpisode(content.mp3);
        if (listOfEpisodes == null || listOfEpisodes.isEmpty()) {
            
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            SimpleDialogFragment dialog = SimpleDialogFragment.newInstance(R.string.no_details_title, R.string.no_details_message);
            dialog.show(ft, "dialog");
            
            return;
        }
        
        MainDetailsFragment fragment = new MainDetailsFragment();
        fragment.setArguments(args);
        
        /*
        .setCustomAnimations(
            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
            R.animator.card_flip_left_in, R.animator.card_flip_left_out)
        */
        
        
        
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.frame_1, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss();
    }
    
    public Content getCurrentTrack() {
        if (mAudioPlayer == null)
            return null;
        
        return mAudioPlayer.getCurrentTrack();
    }
    
    public boolean isPlaying() {
        if (mAudioPlayer != null)
            return mAudioPlayer.isPlaying();
        else
            return false;
    }
    
    public boolean isPaused() {
        if (mAudioPlayer != null)
            return mAudioPlayer.isPaused();
        else
            return false;
    }
    
    PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }
    
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        ContentFragment fragment = (ContentFragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
        
        if (fragment == null)
            return false;
        
        if (itemPosition == 0) {
            mMode = ContentFragment.MODE_ADFREE;
        }
        else {
            mMode = ContentFragment.MODE_PREMIUM;
        }
        
        fragment.refresh();
        
        return false;
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
            
            Log.d(TAG,"UpdateCurrentTrackTask AsyncTask stopped");
            
            return null;
        }
        
        @Override
        protected void onProgressUpdate(Content... content) {
            if( stopped || paused ) {
                return; //to avoid glitches
            }
            
            updatePlayPanel(content[0]);
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
            
            if(intent.getAction().equals(DownloaderService.DOWNLOAD_FINISHED)) {
                
                mPullToRefreshAttacher.setRefreshComplete();
                
                Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.frame_1);
                if (fragment instanceof MainPodcastFragment) {
                    ((MainPodcastFragment) fragment).downloadCompleted();
                }
                else if (fragment instanceof MainDetailsFragment) {
                    // Nothing to do here
                }
                
            }
            else if (intent.getAction().equals(DownloaderService.DOWNLOAD_STARTED)) {
                mPullToRefreshAttacher.setRefreshing(true);
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
            initPlayerCard(mAudioPlayer.getCurrentTrack());
            
            updatePlayQueue();
            initPlayerView();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service disconnected");
            
            mAudioPlayer = null;
        }
    }

    
}
