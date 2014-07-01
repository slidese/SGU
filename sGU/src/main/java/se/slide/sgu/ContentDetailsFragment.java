package se.slide.sgu;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import se.slide.sgu.db.DatabaseHelper;
import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;
import se.slide.sgu.model.Item;
import se.slide.sgu.model.Quote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContentDetailsFragment extends Fragment {
    
    private final String TAG = "ContentDetailsFragment";
    
    public static final String CONTENT_MP3 = "content_mp3";
    public static final String CONTENT_GUID = "content_guid";
    
    private ImageView                   mIcon;
    private TextView                    mTitle;
    private TextView                    mLenght;
    private TextView                    mDescription;
    private ExpandableHeightGridView    mGridViewProfiles;
    private TextView                    mQuoteText;
    private TextView                    mQuoteBy;
    private LinearLayout                mScienceHolder;
    //private Button                      mDeleteOrDownloadButton;
    //private Button                      mPlayButton;
    private StartActivity               mListener;
    
    private Content             mContent;
    private Episode             mEpisode;
    private Quote               mQuote;
    private List<Item>          mList;

    public ContentDetailsFragment() {
        
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (StartActivity) activity;
            MyLog.v(TAG, "Attached details fragment");
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must be the StartActivity");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.card_details, null);

        // Init views
        
        mIcon = (ImageView) view.findViewById(R.id.icon);
        mTitle = (TextView) view.findViewById(R.id.title);
        mLenght = (TextView) view.findViewById(R.id.length);
        mDescription = (TextView) view.findViewById(R.id.content);
        mGridViewProfiles = (ExpandableHeightGridView) view.findViewById(R.id.gridviewProfiles);
        mQuoteText = (TextView) view.findViewById(R.id.quoteText);
        mQuoteBy = (TextView) view.findViewById(R.id.quoteBy);
        mScienceHolder = (LinearLayout) view.findViewById(R.id.scienceHolder);
        //mDeleteOrDownloadButton = (Button) view.findViewById(R.id.downloadOrDeleteButton);
        //mPlayButton = (Button) view.findViewById(R.id.playButton);
        
        // Setup data
        
        String guid = getArguments().getString(CONTENT_GUID);
        
        mContent = DatabaseManager.getInstance().getContentBy(guid);
        mEpisode = DatabaseManager.getInstance().getEpisodeBy(guid);
        mQuote = DatabaseManager.getInstance().getQuoteBy(guid);
        mList = DatabaseManager.getInstance().getItems(guid);
        
        if (mEpisode != null) {
            mTitle.setText(mEpisode.title);
            mDescription.setText(mEpisode.description);
            
            int[] hosts = Utils.convertToIntArray(mEpisode.hosts);
            mGridViewProfiles.setExpanded(true);
            mGridViewProfiles.setAdapter(new ProfileAdapter(getActivity(), -1, getDrawablesFromHosts(hosts))); // We can use -1 since we don't really have a layout for the rows, we just use the ImageView
            
            if (mQuote != null) {
                mQuoteText.setText(mQuote.text);
                mQuoteBy.setText("- " + mQuote.by);
            }
            
            for (Item item : mList) {
                View child = inflater.inflate(R.layout.scienceorfiction_item, null);
                TextView title = (TextView) child.findViewById(R.id.itemTitle);
                TextView description = (TextView) child.findViewById(R.id.itemDescription);
                
                title.setText(item.title);
                description.setText(item.description);
                
                mScienceHolder.addView(child);
            }
            
            //setupButtons();
        }
        else if (mContent != null) {
            mTitle.setText(mContent.title);
            mDescription.setText(mContent.description);
            
            //setupButtons();
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
    
    /*
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
                try {
                    content.downloadId = ContentDownloadManager.INSTANCE.addToDownloadQueue(content.mp3, content.title, content.description, Utils.formatFilename(content.title));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            return exists;
        }
    }
    */
}
