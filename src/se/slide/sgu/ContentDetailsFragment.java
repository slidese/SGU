package se.slide.sgu;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;
import se.slide.sgu.model.Item;
import se.slide.sgu.model.Quote;

import java.util.ArrayList;
import java.util.List;

public class ContentDetailsFragment extends Fragment {
    
    private final String TAG = "ContentDetailsFragment";
    
    public static final String CONTENT_MP3 = "content_mp3";
    
    private ImageView                   mIcon;
    private TextView                    mTitle;
    private TextView                    mLenght;
    private TextView                    mDescription;
    private ExpandableHeightGridView    mGridViewProfiles;
    
    private TextView            mContent2;
    private TextView            mContent3;
    
    private Content             mContent;
    private Episode             mEpisode;

    public ContentDetailsFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.card_details, null);

        mIcon = (ImageView) view.findViewById(R.id.icon);
        mTitle = (TextView) view.findViewById(R.id.title);
        mLenght = (TextView) view.findViewById(R.id.length);
        mDescription = (TextView) view.findViewById(R.id.content);
        mGridViewProfiles = (ExpandableHeightGridView) view.findViewById(R.id.gridviewProfiles);
        
        mContent2 = (TextView) view.findViewById(R.id.content2);
        mContent3 = (TextView) view.findViewById(R.id.content3);
        
        /*
        gridview.setExpanded(true);
        gridview.setAdapter(new ImageAdapter(getActivity()));

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
            }   
        });
        */
        
        String mp3 = getArguments().getString(CONTENT_MP3);
        
        mContent = getContent(mp3);
        mEpisode = getEpisode(mp3);
        
        if (mEpisode != null) {
            int[] hosts = Utils.convertToIntArray(mEpisode.hosts);
            mGridViewProfiles.setExpanded(true);
            mGridViewProfiles.setAdapter(new ProfileAdapter(getActivity(), -1, getDrawablesFromHosts(hosts))); // We can use -1 since we don't really have a layout for the rows, we just use the ImageView
            
        }
        
        return view;
    }
    
    
    
    @Override
    public void onResume() {
        super.onResume();
        
        
    }
    
    /**
     * Convert our database-friendly hosts array to a List<Integer> object we can supply our adapter with.
     * 
     * @param hosts
     * @return
     */
    private List<Integer> getDrawablesFromHosts(int[] hosts) {
        List<Integer> drawables = new ArrayList<Integer>();
        
        for (int i : hosts) {
            if (i == 1)
                drawables.add(R.drawable.profile_steven_novella);
            else if (i == 2)
                drawables.add(R.drawable.profile_bob_novella);
            else if (i == 3)
                drawables.add(R.drawable.profile_jay_novella);
            else if (i == 4)
                drawables.add(R.drawable.profile_rebecca_watson);
            else if (i == 5)
                drawables.add(R.drawable.profile_evan_bernstein);
        }
        
        return drawables;
    }

    private Content getContent(String mp3) {
        List<Content> listOfContent = DatabaseManager.getInstance().getContent(mp3);
        
        if (listOfContent != null && listOfContent.size() > 0)
            return listOfContent.get(0);
        else
            return null;
    }
    
    private Episode getEpisode(String mp3) {
        List<Episode> listOfEpisodes = DatabaseManager.getInstance().getEpisode(mp3);
        
        if (listOfEpisodes != null && listOfEpisodes.size() > 0)
            return listOfEpisodes.get(0);
        else
            return null;
    }
    
    private Quote getQuote(String mp3) {
        List<Quote> listOfQuotes = DatabaseManager.getInstance().getQuote(mp3);
        
        if (listOfQuotes != null && listOfQuotes.size() > 0)
            return listOfQuotes.get(0);
        else
            return null;
    }
    
    private List<Item> getItems(String mp3) {
        return DatabaseManager.getInstance().getItem(mp3);
    }
    
    private class ProfileAdapter extends ArrayAdapter<Integer> {
        private Context mContext;

        public ProfileAdapter(Context context, int resource, List<Integer> drawables) {
            super(context, resource, drawables);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attribute
                int size = (int) getResources().getDimension(R.dimen.image_size);
                
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            
            Integer drawable = getItem(position);
            imageView.setImageResource(drawable);
            
            return imageView;
        }
        
    }
    
}
