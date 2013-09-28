package se.slide.sgu;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import se.slide.sgu.model.Content;

import java.util.List;

public class ContentAdapter extends ArrayAdapter<Content> {
    
    LayoutInflater mInflater;
    
    public ContentAdapter(Context context, int textViewResourceId, List<Content> objects) {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        
        Content content = getItem(position);
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_card, null);
            
            holder = new ViewHolder();
            
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.title.setText(content.title);
        holder.content.setText(content.description);
        
        
        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView title;
        TextView content;
        
    }
}
