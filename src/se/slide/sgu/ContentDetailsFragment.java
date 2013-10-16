package se.slide.sgu;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;

import java.util.List;

public class ContentDetailsFragment extends Fragment {
    
    public static final String CONTENT_MP3 = "content_mp3";
    
    private ImageView           mIcon;
    private TextView            mTitle;
    private TextView            mLenght;
    private TextView            mContent;
    
    private TextView            mContent2;
    private TextView            mContent3;

    public ContentDetailsFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.card_details, null);

        mIcon = (ImageView) view.findViewById(R.id.icon);
        mTitle = (TextView) view.findViewById(R.id.title);
        mLenght = (TextView) view.findViewById(R.id.length);
        mContent = (TextView) view.findViewById(R.id.content);
        
        mContent2 = (TextView) view.findViewById(R.id.content2);
        mContent3 = (TextView) view.findViewById(R.id.content3);
        
        ExpandableHeightGridView gridview = (ExpandableHeightGridView) view.findViewById(R.id.gridview);
        gridview.setExpanded(true);
        gridview.setAdapter(new ImageAdapter(getActivity()));

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
            }
        });
        
        /*
        LinearLayout l = (LinearLayout) view.findViewById(R.id.images);
        
        for (int i = 0; i < 5; i++) {
            ImageView image = new ImageView(getActivity());
            image.setImageDrawable(getResources().getDrawable(R.drawable.profile_bob_novella));
            l.addView(image);
        }
        */
        
        String mp3 = getArguments().getString(CONTENT_MP3);
        
        Content content = getContent(mp3);
        List<Episode> listOfEpisode = getEpisodes(mp3);
        
        mTitle.setText(content.title);
        mContent.setText(content.description);
        
        mContent2.setText(content.description);
        mContent3.setText(content.description);
        
        return view;
    }
    
    private Content getContent(String mp3) {
        List<Content> listOfContent = DatabaseManager.getInstance().getContent(mp3);
        
        if (listOfContent != null && listOfContent.size() > 0)
            return listOfContent.get(0);
        else
            return null;
    }
    
    private List<Episode> getEpisodes(String mp3) {
        /*
        List<Content> listOfContent = DatabaseManager.getInstance().getContent(mp3);
        
        if (listOfContent != null && listOfContent.size() > 0)
            return listOfContent.get(0);
        else
            return null;
            */
        return null;
    }
    
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
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

            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.profile_steven_novella,
                R.drawable.profile_bob_novella,
                R.drawable.profile_rebecca_watson,
                R.drawable.profile_jay_novella,
                R.drawable.profile_evan_bernstein
        };
    }
}
