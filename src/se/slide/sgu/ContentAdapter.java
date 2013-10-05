package se.slide.sgu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.download = (Button) convertView.findViewById(R.id.btnDownload);
            holder.play = (Button) convertView.findViewById(R.id.btnPlay);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.title.setText(content.title);
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
        File file = Utils.getFilepath(filename);
        
        holder.play.setEnabled(file.exists()); // This should be offloaded somehow since it is being calle each time the view becomes visible
        
        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView title;
        TextView content;
        Button download;
        Button play;
        
    }
}
