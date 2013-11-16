package se.slide.sgu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import de.passsy.holocircularprogressbar.HoloCircularProgressBar;
import se.slide.sgu.model.Content;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class ContentAdapter extends ArrayAdapter<Content> {
    
    private final String TAG = "ContentAdapter";
    
    private LayoutInflater mInflater;
    private ContentListener mListener;
    private Resources mResource;
    private List<Content> mObjects;
    
    public ContentAdapter(Context context, int textViewResourceId, List<Content> objects) {
        super(context, textViewResourceId, objects);
        
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mListener = (ContentListener) context;
        mResource = context.getResources();
        mObjects = objects;
    }

    public List<Content> getObjects() {
        return mObjects;
    }
    
    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public Content getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_card, null);
            
            holder = new ViewHolder();
            
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.length = (TextView) convertView.findViewById(R.id.length);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.download = (Button) convertView.findViewById(R.id.btnDownload);
            holder.play = (Button) convertView.findViewById(R.id.btnPlay);
            holder.downloadPlay = (ImageButton) convertView.findViewById(R.id.downloadOrPlayButton);
            holder.progressAndButtonHolder = (RelativeLayout) convertView.findViewById(R.id.progressAndButtonHolder);
            holder.downloadProgressBar = (HoloCircularProgressBar) convertView.findViewById(R.id.holoCircularProgressBar);
            holder.elapsedProgressBar = (ProgressBar) convertView.findViewById(R.id.elapsedProgressBar);
            holder.elapsedTotal = (TextView) convertView.findViewById(R.id.elapsedTotal);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        final Content content = getItem(position);
        
        // First: set the mp3
        holder.mp3 = content.mp3;
        
        String podcastImage = "placeholderSGU.png";
        if (content.image != null && !content.image.isEmpty())
            podcastImage = content.image;
        
        ImageLoader imageLoader = VolleyHelper.getImageLoader();
        imageLoader.get(Utils.HTTP_PODCAST_IMAGES + podcastImage, ImageLoader.getImageListener(holder.icon, R.drawable.placeholder_sgu, R.drawable.placeholder_sgu));    
        
        String title = content.title;
        if (content.friendlyTitle != null && !content.friendlyTitle.isEmpty())
            title = content.friendlyTitle;
        
        holder.title.setText(title);
        holder.length.setText(Formatter.convertBytesToMegabytes(content.length));
        holder.content.setText(content.description);
        holder.elapsedProgressBar.setMax(100);
        holder.elapsedProgressBar.setProgress(0);
        
        float progressValue = 0f;
        
        holder.downloadProgressBar.setProgress(progressValue);
        
        final File file = Utils.getFilepath(content.getFilename());
        
        holder.downloadPlay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (file.exists())
                    mListener.playContent(content);
                else {
                    //new DeleteOrDownloadAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
                    
                    try {
                        content.downloadId = ContentDownloadManager.INSTANCE.addToDownloadQueue(content.mp3, content.title, content.description, Utils.formatFilename(content.title));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                }
            }
        });
        
        Utils.updateView(mResource, content, holder);
        
        content.dirty = false;
        
        return convertView;
    }

    public class ViewHolder {
        String mp3;
        ImageView icon;
        TextView title;
        TextView length;
        TextView content;
        Button download;
        Button play;
        ImageButton downloadPlay;
        ProgressBar elapsedProgressBar;
        TextView elapsedTotal;
        RelativeLayout progressAndButtonHolder;
        HoloCircularProgressBar downloadProgressBar;
    }
    
    private class FileExistsAsyncTask extends AsyncTask<File, Void, Boolean> {
        
        private WeakReference<ViewHolder> weakHolder;
        
        public FileExistsAsyncTask(ViewHolder holder) {
            weakHolder = new WeakReference<ViewHolder>(holder);
        }
        
        @Override
        protected Boolean doInBackground(File... files) {
            return files[0].exists();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            
            ViewHolder holder = weakHolder.get();
            if (holder != null) {
                //holder.play.setEnabled(result);
                if (result) {
                    holder.play.setVisibility(View.VISIBLE);
                    holder.download.setText(R.string.delete);
                }
                else {
                    holder.play.setVisibility(View.GONE);
                    holder.download.setText(R.string.download);
                }
            }
            
        }
        
        
    }
}
