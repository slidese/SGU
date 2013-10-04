package se.slide.sgu;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.haarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;

import java.util.List;

public class ContentFragment extends Fragment {
    
    public static final int MODE_ADFREE = 0;
    public static final int MODE_PREMIUM = 1;
    
    private int mode = 0;
    
    ListView mListview;
    ImageButton mPlayButton;
    ContentAdapter mAdapter;
    
    public ContentFragment() {
        
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.fragment_content, null);
        
        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                Toast.makeText(view.getContext(), "Click!", Toast.LENGTH_SHORT).show();
            }
        });
        
        /*
        View footerView =  inflater.inflate(R.layout.footer, null, false);
        mListview.addFooterView(footerView);
        */
        
        //LinearLayout playerLinearLayout = (LinearLayout) view.findViewById(R.id.player_linearlayout);
        //playerLinearLayout.setBackground(new ColorDrawable(Color.parseColor("#aa000000")));
        
        /*
        mPlayButton = (ImageButton) view.findViewById(R.id.playButton);
        mPlayButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Click!", Toast.LENGTH_SHORT).show();
            }
        });
        */
        
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        updateAdapter();
    }
    
    public void setMode(int mode) {
        this.mode = mode;
        updateAdapter();
    }
    
    private void updateAdapter() {
        List<Content> listOfContent = null;
        if (mode == MODE_ADFREE)
            listOfContent = DatabaseManager.getInstance().getAdFreeContents();
        else
            listOfContent = DatabaseManager.getInstance().getPremiumContents();
        
        mAdapter = new ContentAdapter(getActivity(), R.layout.list_item_card, listOfContent);
        SwingLeftInAnimationAdapter animationAdapter = new SwingLeftInAnimationAdapter(mAdapter);
        animationAdapter.setAbsListView(mListview);
        mListview.setAdapter(animationAdapter);
    }
}
