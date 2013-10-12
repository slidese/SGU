package se.slide.sgu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import se.slide.sgu.model.Content;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class ContentAdapter extends ArrayAdapter<Content> {
    
    private final String TAG = "ContentAdapter";
    
    private LayoutInflater mInflater;
    private ContentListener mListener;
    private Resources mResource;
    
    public ContentAdapter(Context context, int textViewResourceId, List<Content> objects) {
        super(context, textViewResourceId, objects);
        
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mListener = (ContentListener) context;
        mResource = context.getResources();
        
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        
        final Content content = getItem(position);
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_card, null);
            
            holder = new ViewHolder();
            
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.length = (TextView) convertView.findViewById(R.id.length);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.download = (Button) convertView.findViewById(R.id.btnDownload);
            holder.play = (Button) convertView.findViewById(R.id.btnPlay);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.title.setText(content.title);
        holder.length.setText(Formatter.convertBytesToMegabytes(content.length));
        holder.content.setText(content.description);
        
        holder.download.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                new DeleteOrDownloadAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
            }
        });
        
        holder.play.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mListener.playContent(content);
            }
        });
        
        if (content.played) {
            int textColorPlayed = mResource.getColor(R.color.text_white_dark);
            Drawable whiteButtonSelector = mResource.getDrawable(R.drawable.white_button_selector);
            
            holder.play.setText(R.string.play);
            holder.play.setTextColor(textColorPlayed);
            
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                holder.play.setBackgroundDrawable(whiteButtonSelector);
            } else {
                holder.play.setBackground(whiteButtonSelector);
            }
        }
        else {
            int textColorNotPlayed = mResource.getColor(R.color.text_white_full);
            Drawable blueButtonSelector = mResource.getDrawable(R.drawable.blue_button_selector);
            
            holder.play.setText(R.string.play_new);
            holder.play.setTextColor(textColorNotPlayed);
            
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                holder.play.setBackgroundDrawable(blueButtonSelector);
            } else {
                holder.play.setBackground(blueButtonSelector);
            }
            
            
        }
        
        String filename = Utils.formatFilename(content.title);
        final File file = Utils.getFilepath(filename);
        
        FileExistsAsyncTask fileAsyncTask = new FileExistsAsyncTask(holder);
        fileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
        
        /*
        Handler handler = new Handler();
        handler.post( new Runnable() {
            public void run() {
                holder.play.setEnabled(file.exists());
            }
        });
        */
        
         // This should be offloaded somehow since it is being calle each time the view becomes visible
        
        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView title;
        TextView length;
        TextView content;
        Button download;
        Button play;
        
    }
    
    private class DeleteOrDownloadAsyncTask extends AsyncTask<Content, Void, Boolean> {
        
        @Override
        protected Boolean doInBackground(Content... contents) {
            if (contents == null)
                return false;
            
            Content content = contents[0];
            
            String filename = Utils.formatFilename(content.title);
            File file = Utils.getFilepath(filename);
            
            // Delete or download; that's the.......
            boolean exists = file.exists();
            
            if (exists) {
                file.delete();
            }
            else {
                long id = -1L;
                try {
                    id = ContentDownloadManager.INSTANCE.addToDownloadQueue(content.mp3, content.title, content.description, Utils.formatFilename(content.title));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            return exists;
        }
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
