package se.slide.sgu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    public ContentDetailsFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.card_details, null);

        mIcon = (ImageView) view.findViewById(R.id.icon);
        mTitle = (TextView) view.findViewById(R.id.title);
        mLenght = (TextView) view.findViewById(R.id.length);
        mContent = (TextView) view.findViewById(R.id.content);
        
        LinearLayout l = (LinearLayout) view.findViewById(R.id.images);
        
        for (int i = 0; i < 5; i++) {
            ImageView image = new ImageView(getActivity());
            image.setImageDrawable(getResources().getDrawable(R.drawable.profile_bob_novella));
            l.addView(image);
        }
        
        String mp3 = getArguments().getString(CONTENT_MP3);
        
        Content content = getContent(mp3);
        List<Episode> listOfEpisode = getEpisodes(mp3);
        
        mTitle.setText(content.title);
        mContent.setText(content.description);
        
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
}
