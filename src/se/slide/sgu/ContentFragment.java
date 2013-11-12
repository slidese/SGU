
package se.slide.sgu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import se.slide.sgu.ContentAdapter.ViewHolder;
import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener {
    
    private final String TAG = "ContentFragment";

    public static final String CONTENT_MODE = "content_mode";
    
    public static final int MODE_ADFREE = 0;
    public static final int MODE_PREMIUM = 1;

    private StartActivity mListener;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private UpdaterAsyncTask mUpdater;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private boolean mScrolling = false;
    private Resources mResources;

    ListView mListview;
    Button mPlayButton;
    ContentAdapter mAdapter;
    int mMode;
    Map<String, UpdateHolder> mUpdates = new HashMap<String, UpdateHolder>();
    
    private Drawable                    mDrawableWhiteButtonSelector;
    private Drawable                    mDrawableBlueButtonSelector;
    private Drawable                    mDrawableActionDownload;
    private Drawable                    mDrawableActionPlay;

    public ContentFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (StartActivity) activity;
            Log.d(TAG, "Attached podcast list fragment");
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must be the StartActivity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_content, null);

        mResources = getResources();
        
        /*
        mDrawableWhiteButtonSelector = getResources().getDrawable(R.drawable.white_button_selector);
        mDrawableBlueButtonSelector = getResources().getDrawable(R.drawable.blue_button_selector);
        mDrawableActionDownload = getResources().getDrawable(R.drawable.ic_action_download);
        mDrawableActionPlay = getResources().getDrawable(R.drawable.ic_action_playback_play_holo_light);
        */
        
        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setEmptyView(view.findViewById(R.id.empty_list_view));
        mListview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                // This will be implemented in version 2
                //Toast.makeText(view.getContext(), "Click!", Toast.LENGTH_SHORT).show();
                Content content = mAdapter.getItem(position);
                mListener.showContentDetails(content);
            }
        });
        
        mListview.setOnScrollListener(new OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
                
                /*
                if (scrollState == SCROLL_STATE_IDLE) {
                    mAdapter.setUpdateMap(mUpdates);
                }
                */
                
                /*
                if (scrollState == SCROLL_STATE_IDLE) {
                    
                    final int count = view.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View v = view.getChildAt(i);
                        final ViewHolder holder = (ContentAdapter.ViewHolder)view.getChildAt(i).getTag();
                        
                        UpdateHolder update = mMap.get(holder.mp3);
                        //updateView(update, holder);
                    }
                }
                */
            }
            
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                /*
                if (mScrollState == OnScrollListener.SCROLL_STATE_FLING || visibleItemCount < 1)
                    return;
                
                final int count = view.getChildCount();
                if (count == 0)
                    return;
                
                for (int i = 0; i < count; i++) {
                    final ViewHolder holder = (ContentAdapter.ViewHolder)view.getChildAt(i).getTag();
                    
                    UpdateHolder update = mMap.get(holder.mp3);
                    //updateView(update, holder);
                }
                */
            }
            /*
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            private void updateView2(UpdateHolder update, ContentAdapter.ViewHolder holder) {
                if (update != null) {
                    if (!update.exists) {
                        holder.downloadPlay.setImageDrawable(mDrawableActionDownload);
                        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            holder.downloadPlay.setBackgroundDrawable(mDrawableWhiteButtonSelector);
                        } else {
                            holder.downloadPlay.setBackground(mDrawableWhiteButtonSelector);
                        }
                    }
                    else {
                        // Check if pending download!
                        if (update.progress > 0) {
                            holder.downloadPlay.setImageDrawable(mDrawableActionDownload);
                            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                holder.downloadPlay.setBackgroundDrawable(mDrawableBlueButtonSelector);
                            } else {
                                holder.downloadPlay.setBackground(mDrawableBlueButtonSelector);
                            }
                            
                            holder.downloadProgressBar.setProgress(update.progress);
                        }
                        else if (!update.played) {
                            holder.downloadPlay.setImageDrawable(mDrawableActionPlay);
                            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                holder.downloadPlay.setBackgroundDrawable(mDrawableBlueButtonSelector);
                            } else {
                                holder.downloadPlay.setBackground(mDrawableBlueButtonSelector);
                            }
                        }
                        else {
                            holder.downloadPlay.setImageDrawable(mDrawableActionPlay);
                            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                holder.downloadPlay.setBackgroundDrawable(mDrawableWhiteButtonSelector);
                            } else {
                                holder.downloadPlay.setBackground(mDrawableWhiteButtonSelector);
                            }
                        }
                    }
                    
                    
                    Log.d(TAG, "Progress update: onScroll, scrollstate = " + mScrollState + ", update.progress = " + update.progress);
                    Log.d(TAG, "holder.title = " + holder.title.getText() + ", holder.progress = " + holder.downloadProgressBar.getProgress() + ", holder.mp3 = " + holder.mp3);
                }
            }
            */
        });
        
        mPullToRefreshAttacher = ((StartActivity) getActivity()).getPullToRefreshAttacher();
        mPullToRefreshAttacher.addRefreshableView(mListview, this);
        
        mMode = getArguments().getInt(CONTENT_MODE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
    }

    @Override
    public void onPause() {
        super.onPause();
        
        if (mUpdater != null)
            mUpdater.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        
        updateAdapter();
        
        mUpdater = new UpdaterAsyncTask();
        mUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void)null);
    }

    public void refresh() {
        updateAdapter();
    }

    private void updateAdapter() {
        Log.d(TAG, "updateAdapter");
        
        //new FetchContentAsyncTask(mMode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
        List<Content> listOfContent;
        if (mMode == MODE_ADFREE)
            listOfContent = DatabaseManager.getInstance().getAdFreeContents();
        else
            listOfContent = DatabaseManager.getInstance().getPremiumContents();
        
        setAdapter(listOfContent);
    }
    
    private void setAdapter(List<Content> listOfContent) {
        List<Episode> listOfEpisode = DatabaseManager.getInstance().getAllEpisodes();
        Map<String,Episode> episodes = new HashMap<String,Episode>();
        for (Episode episode : listOfEpisode)
            episodes.put(episode.mp3, episode);
        
        Log.d(TAG, "episodes length = " + episodes.size());
        
        Map<String, UpdateHolder> updates = gatherMetadata();
        
        for (Content content : listOfContent) {
            Episode episode = episodes.get(content.mp3);
            if (episode != null) {
                content.friendlyTitle = episode.title;
                content.image = episode.image;
            }
            
            UpdateHolder update = updates.get(content.mp3);
            if (update != null) {
                content.exists = update.exists;
                content.downloadProgress = update.progress;
                content.downloadStatus = update.status;
                content.isPaused = update.isPaused;
                content.isPlaying = update.isPlaying;
            }
        }
        
        mAdapter = new ContentAdapter(getActivity(), R.layout.list_item_card, listOfContent);
        mListview.setAdapter(mAdapter);
    }
    
    @Override
    public void onRefreshStarted(View view) {
        getActivity().startService(new Intent(getActivity(), DownloaderService.class));
    }
    
    public class UpdateHolder {
        public String mp3;
        public int status;
        public boolean played;
        public float progress;
        public boolean exists = false;
        public boolean isPlaying = false;
        public boolean isPaused = false;
        //public int elapsed;
        //public int duration;
    }
    
    private class UpdaterAsyncTask extends AsyncTask<Void, Map<String, UpdateHolder>, Void> {
        
        boolean isRunning = true;

        public void stop() {
            isRunning = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            
            while (isRunning) {
                
                /*
                Map<String, UpdateHolder> map = new HashMap<String, UpdateHolder>();
                
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
                try {
                    Cursor cursor = ContentDownloadManager.INSTANCE.query(q);
                    
                    while (cursor.moveToNext()) {
                        //long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                        String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        
                        float progress = (float)downloaded/(float)total;
                        
                        UpdateHolder holder = new UpdateHolder();
                        holder.progress = progress;
                        holder.status = status;
                        
                        map.put(uri, holder);
                    }
                    
                    cursor.close();
                    
                    List<Content> listOfContent = DatabaseManager.getInstance().getAllContents();
                    for (Content content : listOfContent) {
                        File file = Utils.getFilepath(content.getFilename());
                        
                        UpdateHolder holder = map.get(content.mp3);
                        if (holder == null) {
                            holder = new UpdateHolder();
                        }
                        
                        holder.exists = file.exists();
                        holder.played = content.played;
                        map.put(content.mp3, holder);
                        
                        Log.d(TAG, "map.put = " + content.mp3);
                    }
                    
                    publishProgress(map);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                 */
                
                Map<String, UpdateHolder> map = gatherMetadata();
                publishProgress(map);
                
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            return null;
        }
        
        @Override
        protected void onProgressUpdate(Map<String, UpdateHolder>... values) {
            super.onProgressUpdate(values);
            
            final int c = mAdapter.getCount();
            for (int index = 0; index < c; index++) {
                Content content = mAdapter.getItem(index);
                
                UpdateHolder update = values[0].get(content.mp3);
                if (update != null) {
                    content.exists = update.exists;
                    content.downloadProgress = update.progress;
                    content.isPaused = update.isPaused;
                    content.isPlaying = update.isPlaying;
                }
            }
            
            
            if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                final int count = mListview.getChildCount();
                for (int i = 0; i < count; i++) {
                    final ViewHolder holder = (ContentAdapter.ViewHolder)mListview.getChildAt(i).getTag();
                    UpdateHolder update = values[0].get(holder.mp3);
                    Utils.updateView(mResources, update, holder);
                }
            }
        }
    }
    
    private Map<String, UpdateHolder> gatherMetadata() {
        
        Map<String, UpdateHolder> map = new HashMap<String, UpdateHolder>();
        
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
        try {
            Cursor cursor = ContentDownloadManager.INSTANCE.query(q);
            
            while (cursor.moveToNext()) {
                //long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                
                float progress = (float)downloaded/(float)total;
                
                UpdateHolder holder = new UpdateHolder();
                holder.progress = progress;
                holder.status = status;
                
                map.put(uri, holder);
            }
            
            cursor.close();
            
            final Content currentContent = mListener.getCurrentTrack();
            final boolean isPlaying = mListener.isPlaying();
            final boolean isPaused = mListener.isPaused();
            
            List<Content> listOfContent = DatabaseManager.getInstance().getAllContents();
            for (Content content : listOfContent) {
                File file = Utils.getFilepath(content.getFilename());
                
                UpdateHolder holder = map.get(content.mp3);
                if (holder == null) {
                    holder = new UpdateHolder();
                }

                if (currentContent != null && content.mp3.equals(currentContent.mp3)) {
                    holder.isPlaying = isPlaying;
                    holder.isPaused = isPaused;
                }
                else {
                    holder.isPlaying = false;
                    holder.isPaused = false;
                }
                
                holder.exists = file.exists();
                holder.played = content.played;
                //holder.elapsed = content.elapsed;
                //holder.duration = content.duration;
                map.put(content.mp3, holder);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return map;
    }
    
    private class FetchContentAsyncTask extends AsyncTask<Void, Void, List<Content>> {
        
        private int mode = MODE_ADFREE;
        
        public FetchContentAsyncTask(int mode) {
            this.mode = mode;
        }
        
        @Override
        protected List<Content> doInBackground(Void... params) {
            List<Content> listOfContent = null;
            if (mode == MODE_ADFREE)
                listOfContent = DatabaseManager.getInstance().getAdFreeContents();
            else
                listOfContent = DatabaseManager.getInstance().getPremiumContents();
            
            return listOfContent;
        }

        @Override
        protected void onPostExecute(List<Content> listOfContent) {
            super.onPostExecute(listOfContent);
            
            setAdapter(listOfContent);
        }
    }
}
