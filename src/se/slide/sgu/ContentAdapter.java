package se.slide.sgu;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
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
    
    public ContentAdapter(Context context, int textViewResourceId, List<Content> objects) {
        super(context, textViewResourceId, objects);
        
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mListener = (ContentListener) context;
    }

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
                long id = -1L;
                try {
                    id = ContentDownloadManager.INSTANCE.addToDownloadQueue(content.mp3, content.title, content.description, Utils.formatFilename(content.title));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        holder.play.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mListener.playContent(content);
            }
        });
        
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
            if (holder != null)
                holder.play.setEnabled(result);
        }
        
        
    }
}
