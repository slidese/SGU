package se.slide.sgu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.google.analytics.tracking.android.EasyTracker;
import com.slidinglayer.SlidingLayer;

import org.codechimp.apprater.AppRater;

import se.slide.sgu.db.DatabaseManager;

public class StartActivity extends Activity {
    
    private final String TAG = "StartActivity";
    
    private SlidingLayer mSlidingLayer;
    private Button mCloseButton;

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
        mCloseButton = (Button) findViewById(R.id.buttonClose);
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
        
        mCloseButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSlidingLayer.closeLayer(true);
            }
        });
        
        getActionBar().setTitle("");
    }
}
