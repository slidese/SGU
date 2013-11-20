
package se.slide.sgu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
        
        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setEmptyView(view.findViewById(R.id.empty_list_view));
        mListview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        
        mListview.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
            
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                
            }
            
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual_menu_content, menu);
                return true;
            }
            
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    
                    SparseBooleanArray checked = mListview.getCheckedItemPositions();
                    Content[] params = new Content[checked.size()];
                    int index = 0;
                    int first = mListview.getFirstVisiblePosition();
                    int last = mListview.getLastVisiblePosition();
                    for (int i = 0; i < mListview.getCount(); i++) {
                        if (checked.get(i)) {
                            params[index++] = (Content)mListview.getItemAtPosition(i);
                            
                            if (i >= first && i <= last) {
                                View view = mListview.getChildAt(i-first);
                                
                                Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
                                animation.setDuration(200);
                                //animation.setFillAfter(true);
                                animation.setStartOffset(100 * (index) );
                                view.startAnimation(animation);    
                            }
                            
                        }
                    }
                    
                    new AsyncTask<Content, Void, Void>() {

                        @Override
                        protected Void doInBackground(Content... params) {
                            for (Content content : params) {
                                File file = Utils.getFilepath(content.getFilename());
                                file.delete();
                            }
                            
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            super.onPostExecute(result);
                            
                            mAdapter.notifyDataSetChanged();
                        }
                        
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                    
                    mode.finish();
                    return true;
                }
                
                return false;
            }
            
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                
            }
        });
        
        mListview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                Content content = mAdapter.getItem(position);
                mListener.showContentDetails(content);
            }
        });
        
        mListview.setOnScrollListener(new OnScrollListener() {
            
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
                
            }
            
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                
            }
            
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
            episodes.put(episode.guid, episode);    
        
        Log.d(TAG, "episodes length = " + episodes.size());
        
        Map<String, UpdateHolder> updates = gatherMetadata();
        
        for (Content content : listOfContent) {
            Episode episode = episodes.get(content.guid);
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
        
        Intent intent = new Intent(getActivity(), DownloaderService.class);
        intent.putExtra(DownloaderService.EXTRA_USER_INITIATED, true);
        getActivity().startService(intent);
        
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
    
    private class UpdaterAsyncTask extends AsyncTask<Void, Void, Void> {
        
        boolean isRunning = true;

        public void stop() {
            isRunning = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            
            while (isRunning) {

                /*
                Map<String, UpdateHolder> map = gatherMetadata();
                publishProgress(map);
                */
                
                updateCurrentAdapterContent();
                publishProgress();
                
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            return null;
        }
        
        @Override
        protected void onProgressUpdate(Void... params) {
            super.onProgressUpdate();
            
            if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                //mAdapter.notifyDataSetChanged();
                
                // http://stackoverflow.com/questions/2123083/android-listview-refresh-single-row
                int start = mListview.getFirstVisiblePosition();
                for(int i = start, j = mListview.getLastVisiblePosition(); i<=j; i++) {
                    /*
                    if(target==list.getItemAtPosition(i)){
                        break;
                    }
                    */
                    
                    View view = mListview.getChildAt(i-start);
                    if (((Content)mListview.getItemAtPosition(i)).dirty) {
                        Log.v(TAG, "Content is dirty");
                        mListview.getAdapter().getView(i, view, mListview);
                    }
                        
                }
            }
            
            /*
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
            */
        }
    }
    
    private void updateCurrentAdapterContent() {
        
        List<Content> listOfContent = mAdapter.getObjects();
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
            
            for (Content content : listOfContent) {
                // First update any download progress we might have for this specific content item
                UpdateHolder holder = map.get(content.mp3);
                if (holder != null) {
                    if (content.downloadProgress != holder.progress) {
                        content.downloadProgress = holder.progress;
                        content.dirty = true;
                    }
                    if (content.downloadStatus != holder.status) {
                        content.downloadStatus = holder.status;
                        content.dirty = true;
                    }
                }
                else {
                    if (content.downloadProgress != 0f) {
                        content.downloadProgress = 0f;
                        content.dirty = true;
                    }
                    if (content.downloadStatus != -1) {
                        content.downloadStatus = -1;
                        content.dirty = true;
                    }
                }
                
                // Update with elapsed (to be done)
                
                // File exists?
                File file = Utils.getFilepath(content.getFilename());
                if (content.exists != file.exists()) {
                    content.exists = file.exists();
                    content.dirty = true;
                }

                // Is this the currently playing content
                if (currentContent != null && content.mp3.equals(currentContent.mp3)) {
                    if (content.isPlaying != isPlaying) {
                        content.isPlaying = isPlaying;
                        content.dirty = true;
                    }
                    if (content.isPaused != isPaused) {
                        content.isPaused = isPaused;
                        content.dirty = true;
                    }
                }
                else {
                    if (content.isPlaying != false) {
                        content.isPlaying = false;
                        content.dirty = true;
                    }
                    if (content.isPaused != false) {
                        content.isPaused = false;
                        content.dirty = true;
                    }
                }
              
                if (content.dirty) {
                    DatabaseManager.getInstance().createOrUpdateContent(content);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
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
