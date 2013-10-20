package se.slide.sgu;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class LinkAdapter extends ArrayAdapter<Link> {
    
private final String TAG = "ContentAdapter";
    
    private LayoutInflater mInflater;

    public LinkAdapter(Context context, int resource, List<Link> objects) {
        super(context, resource, objects);
        
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        
        Link link = getItem(position);
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_item, null);
            
            holder = new ViewHolder();
            
            holder.title = (TextView) convertView.findViewById(R.id.spinnerItemTitle);
            holder.url = (TextView) convertView.findViewById(R.id.spinnerItemUrl);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.title.setText(link.title);
        holder.url.setText(link.url);
        
        return convertView;
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView);
    }
    
    private View initView(int position, View convertView) {
        if(convertView == null)
            convertView = View.inflate(getContext(), R.layout.spinner_item, null);
        
        TextView title = (TextView) convertView.findViewById(R.id.spinnerItemTitle);
        TextView url = (TextView) convertView.findViewById(R.id.spinnerItemUrl);
        
        Link link = getItem(position);
        
        title.setText(link.title);
        url.setText(link.url);
        
        return convertView;
    }

    private class ViewHolder {
        TextView title;
        TextView url;
    }
}
